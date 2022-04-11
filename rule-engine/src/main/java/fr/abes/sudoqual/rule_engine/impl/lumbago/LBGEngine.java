package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.che.commons.annotation.Nullable;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.Rule;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;
import fr.abes.sudoqual.rule_engine.impl.RuleEngineResultImpl;
import fr.abes.sudoqual.rule_engine.impl.lumbago.AOCPosetUtils.AOCPosetData;
import fr.abes.sudoqual.rule_engine.impl.lumbago.LBGEngineBuilder.LBGEngineData;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGAtom;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.rule_engine.predicate.exception.InconsistentException;
import fr.abes.sudoqual.rule_engine.predicate.exception.NotComparableException;
import fr.abes.sudoqual.rule_engine.predicate.exception.PredicateException;
import fr.abes.sudoqual.util.DlpUtils;
import fr.lirmm.marel.gsh2.core.IMySet;
import fr.lirmm.marel.gsh2.core.MyBitSet;
import fr.lirmm.marel.gsh2.core.MyConcept;
import fr.lirmm.marel.gsh2.io.DotWriter.DisplayFormat;

/**
 * This class implements a rule-engine based on an AOCPoset structure.
 * @see <a href="http://www.lirmm.fr/AOC-poset-Builder/">http://www.lirmm.fr/AOC-poset-Builder/</a>
 * 
 * @author Alain Gutierrez (LIRMM)
 * @author Clément Sipieter {@literal csipieter@6pi.fr}
 */import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;

public class LBGEngine {
	
	private static final RuleEngine.Result NOT_FOUND = new RuleEngineResultImpl(0, Collections.emptyList());


	/* The qualinca ressources: filters and criterions. */
	private final Map<String, Filter> filters;
	private final Map<String, Criterion> criterions;

	/** This list is used to recover Atoms from their index in a BitSet */
	private final List<LBGAtom> computables;
	/** This list is used to recover original rules used to deduce an atom 
	 * (to produce the why part of RuleEngineResult)*/
	private final List<LBGCombination> combinations;
	
	private final Map<LBGAtom, MyBitSet> combinationsByAtom;
	
	/** The AOC poset resulting of compile() function or loaded from xml */
	private final AOCPosetData aocPoset;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////
	
	public static LBGEngine create(Reader dlpReader, Collection<Predicate> predicates) throws LBGEngineException {
		return LBGEngineBuilder.INSTANCE.create(dlpReader, predicates);
	}

	LBGEngine(LBGEngineData data)
	    throws LBGEngineException {
		this.filters = data.filters;
		this.criterions = data.criterions;
		this.computables = data.computables;
		this.combinations = data.combinations;
		this.combinationsByAtom = data.combinationsByAtom;
		this.aocPoset = data.aocPoset;
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * This method will return the maximum weight deductible for the specified
	 * predicate and the specified values of variables.
	 */
	public RuleEngine.Result getMax(PredicateManager store, String predicate, Reference from, Reference to, int minThreshold)
	    throws LBGEngineException {
		return getFirstValue(store, predicate, from, to, MaxBeforeLBGAtomValueComparator.INSTANCE, minThreshold, Integer.MAX_VALUE);
	}

	/**
	 * This method will return the minimum weight deductible for the specified
	 * predicate and the specified values of variables.
	 */
	public RuleEngine.Result getMin(PredicateManager store, String predicate, Reference from, Reference to, int maxThreshold)
	    throws LBGEngineException {
		return getFirstValue(store, predicate, from, to, MinBeforeLBGAtomValueComparator.INSTANCE, Integer.MIN_VALUE, maxThreshold);
	}
	
	public RuleEngine.Result query(PredicateManager store, LBGAtom atom, Reference from, Reference to) throws LBGEngineException {
		Map<Integer, Boolean> checkComputableAtomCache = new HashMap<>();
		return query(store, atom, from, to, checkComputableAtomCache);
	}
	
	
	
	/**
	 * Write a representation of the underlying AOCPoset used by this rule engine as a Dot File.
	 * @see <a href="https://graphviz.org/">https://graphviz.org/</a>
	 * @param f
	 * @throws IOException
	 */
	public void writeUnderlyingAOCPosetAsDot(File f) throws IOException {
		AOCPosetUtils.writeDotFile(f, this.aocPoset, DisplayFormat.FULL);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	PRIVATE
	// /////////////////////////////////////////////////////////////////////////

	private RuleEngine.Result query(PredicateManager store, LBGAtom atom, Reference from, Reference to, Map<Integer, Boolean> checkComputableAtomCache) throws LBGEngineException {
		Map<String, Reference> varMap = new HashMap<>();
		varMap.put(atom.getTerm(0).toString(), from);
		varMap.put(atom.getTerm(1).toString(), to);

		BitSet b = query(store, atom, varMap, checkComputableAtomCache);
		if (b != null) {
			ArrayList<Rule> why = new ArrayList<Rule>();
			why.addAll(combinations.get(b.nextSetBit(0)).getRuleTrace());
			return new RuleEngineResultImpl(LBGUtils.getValueOf(atom), why);
		}
		return NOT_FOUND;
	}
	
	private RuleEngine.Result getFirstValue(PredicateManager store, String predicate, Reference from, Reference to,
	    Comparator<LBGAtom> comparator, int minThreshold, int maxThreshold)
	    throws LBGEngineException {
		
		List<LBGAtom> sortedCandidates = computeSortedCandidates(predicate, comparator, minThreshold, maxThreshold);
		Map<Integer, Boolean> checkComputableAtomCache = new HashMap<>();
		
		for (LBGAtom candidate : sortedCandidates) {
			RuleEngine.Result result = query(store, candidate, from, to, checkComputableAtomCache);
			if(result != NOT_FOUND) {
				return result;
			}
		}
		return NOT_FOUND;
	}

	private Map<ComputeSortedCandidatesData, List<LBGAtom>> computeSortedCandidatesMemoizationMap = new ConcurrentHashMap<>();
	/**
	 * This method is a memoized version of _computeSortedCandidates
	 */
	private List<LBGAtom> computeSortedCandidates(String predicate, Comparator<LBGAtom> comparator, int minThreshold,
	    int maxThreshold)
	    throws LBGEngineException {
		ComputeSortedCandidatesData data = new ComputeSortedCandidatesData(predicate, comparator, minThreshold,
		                                                                   maxThreshold);
		synchronized (computeSortedCandidatesMemoizationMap) {
			List<LBGAtom> candidates = computeSortedCandidatesMemoizationMap.get(data);
			if (candidates == null) {
				candidates = _computeSortedCandidates(predicate, comparator, minThreshold, maxThreshold);
				computeSortedCandidatesMemoizationMap.put(data, candidates);
			}
			return candidates;
		}
	}
	
	private List<LBGAtom> _computeSortedCandidates(String predicate, Comparator<LBGAtom> comparator, int minThreshold,
	    int maxThreshold)
	    throws LBGEngineException {
		List<LBGAtom> candidates = new ArrayList<LBGAtom>();
		for (LBGAtom atom : combinationsByAtom.keySet()) {
			if (atom.getPredicate().equals(predicate)
			    && atom.getArity() == 3
			    && LBGTerm.TERM_TYPE.VARIABLE == atom.getTerm(0).getType()
			    && LBGTerm.TERM_TYPE.VARIABLE == atom.getTerm(1).getType()
			    && LBGTerm.TERM_TYPE.VARIABLE != atom.getTerm(2).getType()) {
				int value = LBGUtils.getValueOf(atom);
				if (value >= minThreshold && value <= maxThreshold) {
					candidates.add(atom);
				}
			}
		}
		Collections.sort(candidates, comparator);
		return candidates;
	}
	
	/**
	 * Check if there exists a combination of rules that validates this atom with this specific
	 * variable values.
	 * 
	 * @param store PredicateManager to check computable atoms
	 * @param atom the atom for which we want to check if it is true.
	 * @param varMap the values of the atom variables .
	 * @return a non-empty BitSet if there exists a combination of rules that validates the atom, 
	 * null otherwise.
	 * @throws LBGEngineException
	 */
	private @Nullable BitSet query(PredicateManager store, LBGAtom atom, Map<String, Reference> varMap, Map<Integer, Boolean> checkComputableAtomCache)
	    throws LBGEngineException {
		// See http://www.lirmm.fr/AOC-poset-Builder/
		// objSet represents the set of AOCPoset objects that validate the atom.
		// Validate only one of them is sufficient
		MyBitSet objSet = combinationsByAtom.get(atom);
		if (objSet == null)
			throw new LBGEngineException("unknown atom in query: " + atom);

		return exploreConceptList(store, objSet, aocPoset.getMaximals(), varMap, checkComputableAtomCache);
	}
	
	private @Nullable BitSet exploreConceptList(PredicateManager store, MyBitSet objSet, List<MyConcept> conceptList,
	    Map<String, Reference> varMap, Map<Integer, Boolean> checkComputableAtomCache)
	    throws LBGEngineException {
		for (MyConcept concept : conceptList) {
			// Extents represent rules (use this.combinations.getRuleTrace to retrieve them)
			if (!objSet.newIntersectBitSet(concept.getExtent()).isEmpty()) {
				BitSet b = exploreConcept(store, objSet, concept, varMap, checkComputableAtomCache);
				if (b != null) {
					return b;
				}
			}
		}
		return null;
	}

	private @Nullable BitSet exploreConcept(PredicateManager store, MyBitSet objSet, MyConcept concept,
	    Map<String, Reference> varMap, Map<Integer, Boolean> checkComputableAtomCache)
	    throws LBGEngineException {
		BitSet res = null;

		// Intents represent computable atoms
		if (checkAllComputables(store, concept.getIntent(), varMap, checkComputableAtomCache)) {
			// if the current concept introduces an object that validate the query, return a
			// BitSet representing it.
			res = objSet.newIntersectBitSet(concept.getReducedExtent());
			if (res.isEmpty()) {
				// else explore lower concepts to try to complete requirements
				List<MyConcept> lowerConcepts = this.aocPoset.getLowerCovers().get(concept);
				res = exploreConceptList(store, objSet, lowerConcepts, varMap, checkComputableAtomCache);
			}
		}
		return res;
	}

	private boolean checkAllComputables(PredicateManager store, IMySet intents, Map<String, Reference> varMap, Map<Integer, Boolean> checkComputableAtomCache) throws LBGEngineException {
		// Intents represent computable atoms
		for (Iterator<Integer> it = intents.iterator(); it.hasNext();) {
			// so we iterate over computable atoms
			Integer idxComputable = it.next();
			Boolean value = checkComputableAtomCache.get(idxComputable);
			if(value == null) {
				LBGAtom atom = computables.get(idxComputable);
				value = checkComputableAtom(store, atom, varMap);
				checkComputableAtomCache.put(idxComputable, value);
			}
			if (!value) { 
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the specified variables fulfill the condition attached to the
	 * specified atom. This condition is defined by the name of this atom which
	 * should be either a name of a filter either a name of a criterion. eventually
	 * preceded by "not_" to reverse the result of this condition.
	 * 
	 * @param store
	 * @param atom
	 * @param varMap
	 * @return
	 * @throws LBGEngineException
	 */
	private boolean checkComputableAtom(PredicateManager store, LBGAtom atom, Map<String, Reference> varMap) throws LBGEngineException {
		boolean isNegated = false;
		if (atom.getPredicate().startsWith("not_")) {
			String predName = DlpUtils.removeNotFromPredicateName(atom.getPredicate());
			atom = new LBGAtom(predName, atom.getTerms());
			isNegated = true;
		}
		
		try {
			boolean res = _checkComputableAtom(store, atom, varMap);
    		return (isNegated)? !res: res;
		} catch (PredicateException e) {
			return false; // return always false;
		}
	}

	/** Should be used only by {@link checkComputableAtom} which handles negation */
	private boolean _checkComputableAtom(PredicateManager store, LBGAtom atom, Map<String, Reference> varMap) throws LBGEngineException, PredicateException {
		if (criterions.containsKey(atom.getPredicate())) {
			return checkCriterion(store, atom, varMap);
		} else if (filters.containsKey(atom.getPredicate())) {
			return checkFilter(store, atom, varMap);
		} else {
			throw new LBGEngineException("Unknown computable predicate (Criterion or Filter): " + atom);
		}
	}
	
	private boolean checkCriterion(PredicateManager store, LBGAtom atom, Map<String, Reference> varMap) throws LBGEngineException, PredicateException {
		String var1 = atom.getTerm(0).getValue().toString();
		Reference ref1 = varMap.get(var1);
		if (ref1 == null)
			throw new LBGEngineException("unknown variable " + var1);
		
		String var2 = atom.getTerm(1).getValue().toString();
		Reference ref2 = varMap.get(var2);
		if (ref2 == null)
			throw new LBGEngineException("unknown variable " + var2);

		Criterion crit = criterions.get(atom.getPredicate());
		if(crit == null) {
			throw new IllegalArgumentException("There should be a Criterion corresponding to the predicate name.");
		}
		int val = store.compare(crit, ref1, ref2);
		return check(crit.getComparisonType(), atom, val);
	}
	
	private boolean checkFilter(PredicateManager store, LBGAtom atom, Map<String, Reference> varMap) throws LBGEngineException {
		String var1 = atom.getTerm(0).getValue().toString();
		Reference ref1 = varMap.get(var1);
		if (ref1 == null)
			throw new LBGEngineException("unknown variable " + var1);

		Filter filter = filters.get(atom.getPredicate());
		if(filter == null) {
			throw new IllegalArgumentException("There should be a Filter corresponding to the predicate name.");
		}
		return store.check(filter, ref1);
	}

	private static final PredicateException notComparableException = new NotComparableException();
	private static final PredicateException inconsitentException = new InconsistentException();
	private static boolean check(DiscretCompType compType, LBGAtom atom, int computedValue) throws LBGEngineException, PredicateException {
		if (computedValue == DiscretCompType.NOT_COMPARABLE) {
			throw notComparableException;
		}
		if(computedValue == DiscretCompType.INCOHERENT) {
			throw inconsitentException;
		}

		int expectedValue;
		LBGTerm term = atom.getTerm(2);
		switch(term.getType()) {
			case CONSTANT:
    			try {
    				expectedValue = compType.toInt((String)term.getValue());
    			} catch (RuleEngineException e) {
    				throw new LBGEngineException("unknown constant for criterion value " + atom, e);
    			}
    			break;
			case INTEGER:
				expectedValue = ((Integer) term.getValue()).intValue();
				break;
			default:
				throw new LBGEngineException("unknown parameter for criterion value " + atom);
		}

		if (expectedValue < 0) {
			return computedValue <= expectedValue;
		} else if (expectedValue > 0) {
			return computedValue >= expectedValue;
		} else {
			return computedValue == expectedValue;
		}
	}
	
    // /////////////////////////////////////////////////////////////////////////
	//	PRIVATE STATIC CLASSES
	// /////////////////////////////////////////////////////////////////////////
	
	private static enum MaxBeforeLBGAtomValueComparator implements Comparator<LBGAtom> {
		INSTANCE;
		
		@Override
		public int compare(LBGAtom a1, LBGAtom a2) {
			try {
				int a1Val = LBGUtils.getValueOf(a1);
				int a2Val = LBGUtils.getValueOf(a2);

				if (a1Val > a2Val) {
					return -1;
				}
				if (a1Val < a2Val) {
					return 1;
				}
				return 0;

			} catch (LBGEngineException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public String toString() {
			return "maxBefore";
		}
	}
	
	private static enum MinBeforeLBGAtomValueComparator implements Comparator<LBGAtom> {
		INSTANCE;
		
		@Override
		public int compare(LBGAtom a1, LBGAtom a2) {
			return -(MaxBeforeLBGAtomValueComparator.INSTANCE.compare(a1, a2));
		}
		
		@Override
		public String toString() {
			return "minBefore";
		}
	}
	
	/**
	 * Use for memoization
	 * @author Clément Sipieter {@literal <clement@6pi.fr>}
	 */
	private static class ComputeSortedCandidatesData {
		private final String predicate;
		private final Comparator<LBGAtom> comparator;
		private final int minThreshold;
		private final int maxThreshold;
		private int hashCode = 0;
		
		public ComputeSortedCandidatesData(String predicate, Comparator<LBGAtom> comparator, int minThreshold, int maxThreshold) {
			this.predicate = predicate;
			this.comparator = comparator;
			this.minThreshold = minThreshold;
			this.maxThreshold = maxThreshold;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == this) {
				return true;
			}
			if(o == null || o.getClass() != this.getClass()) {
				return false;
			}
			ComputeSortedCandidatesData other = (ComputeSortedCandidatesData) o;
			return this.predicate.equals(other.predicate)
					&& this.comparator.equals(other.comparator)
					&& this.minThreshold == other.minThreshold
					&& this.maxThreshold == other.maxThreshold;
		}
		
		@Override
		public int hashCode() {
			int result = hashCode;
			if(result == 0) {
				result = predicate.hashCode();
				result = 31*result + comparator.hashCode();
				result = 31*result + minThreshold;
				result = 31*result + maxThreshold;
				this.hashCode = result;
			}
			return result;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[ComputeSortedCandidatesData predicate=")
			.append(predicate)
			.append(" comparator=").append(comparator)
			.append(" min=").append(minThreshold)
			.append(" max=").append(maxThreshold)
			.append("]");
			return sb.toString();
		}
	}
	
}
