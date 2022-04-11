package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.SimpleDirectedGraph;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.impl.lumbago.AOCPosetUtils.AOCPosetData;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGAtom;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGRule;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.util.DlpUtils;
import fr.lirmm.marel.gsh2.core.MyBitSet;

enum LBGEngineBuilder {
	INSTANCE;

	public LBGEngine create(Reader dlpReader, Collection<Predicate> predicates)
	    throws LBGEngineException {
		LBGEngineData data = new LBGEngineData();
		loadPredicates(data, predicates);
		loadRules(data, dlpReader);
		data.aocPoset = AOCPosetUtils.buildAOCPoset(data.combinations, data.computables);

		return new LBGEngine(data);
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	private static void loadPredicates(LBGEngineData data, Collection<Predicate> predicates) {
		for (Predicate pred : predicates) {
			if (pred instanceof Filter) {
				data.filters.put(pred.getKey(), (Filter) pred);
			} else if (pred instanceof Criterion) {
				data.criterions.put(pred.getKey(), (Criterion) pred);
			}
		}
	}

	private static void loadRules(LBGEngineData data, Reader reader) throws LBGEngineException {
		/** the rule analysis graph */
		final SimpleDirectedGraph<Object, Object> ruleGraph = new SimpleDirectedGraph<Object, Object>(Object.class);
		final Map<String, SortedSet<Integer>> dimensionValues = new HashMap<>();
		final Map<String, Integer> dimensionArity = new HashMap<>();

		MyLBGParser parser = new MyLBGParser(ruleGraph, dimensionValues, dimensionArity, data.filters, data.criterions);
		parser.parse(reader);
		addDimensions(ruleGraph, dimensionValues, dimensionArity);

		List<LBGAtom> roots = findRoots(ruleGraph);
		buildCombinationsAndDeclareComputable(data, ruleGraph, roots);
	}


	/**
	 * from each root explore deeply the hierarchy to build all available
	 * combination and declare each computable
	 * 
	 * @param data
	 * @param ruleGraph 
	 * @param roots
	 * @throws LBGEngineException 
	 */
	private static void buildCombinationsAndDeclareComputable(LBGEngineData data, SimpleDirectedGraph<Object, Object> ruleGraph, List<LBGAtom> roots) throws LBGEngineException {
		HashMap<LBGAtom, LBGCombinationSet> mByAtom = new HashMap<LBGAtom, LBGCombinationSet>();
		for (LBGAtom root : roots) {
			explore(data, root, null, root, mByAtom, ruleGraph);
		}

		for (LBGAtom atom : mByAtom.keySet()) {
			LBGCombinationSet m = mByAtom.get(atom);
			BitSet objectNums = new BitSet();
			for (LBGCombination combi : m.setObjects) {
				int numobj = declCombination(data, combi);
				objectNums.set(numobj);
			}
			data.combinationsByAtom.put(atom, new MyBitSet(objectNums) {
				@Override
				public void changeImplementation(Impl arg0) {					
				}});
		}

	}

	/**
	 * Find root vertices (always atoms)
	 * 
	 * @param ruleGraph
	 * @return
	 */
	private static List<LBGAtom> findRoots(SimpleDirectedGraph<Object, Object> ruleGraph) {
		try {
			ensureNoCycles(ruleGraph);
		} catch (LBGEngineException e) {
			throw new IllegalArgumentException("A cyclic graph can't be explored in depth.", e);
		}

		// rule graph has no cycle it can be explored in depth to build the different
		// conjunctions
		ArrayList<LBGAtom> roots = new ArrayList<LBGAtom>();
		for (Object vertex : ruleGraph.vertexSet()) {
			if (ruleGraph.inDegreeOf(vertex) == 0)
				roots.add((LBGAtom) vertex);
		}

		return roots;
	}

	private static void ensureNoCycles(SimpleDirectedGraph<Object, Object> ruleGraph) throws LBGEngineException {
		// rule analysis (cycle in a rule)
		for (Object vertex : ruleGraph.vertexSet()) {
			if (vertex instanceof LBGRule) {
				if (((LBGRule) vertex).isCyclic()) {
					throw new LBGEngineException("Cyclic rule detected: " + vertex);
				}
			}
		}

		// rule graph analysis (cycle between rules)
		CycleDetector<Object, Object> cycleDetector = new CycleDetector<Object, Object>(ruleGraph);
		if (cycleDetector.detectCycles()) {
			Set<Object> cycle = cycleDetector.findCycles();
			String s = "";
			for (Object vertex : cycle) {
				if (vertex instanceof LBGRule)
					s += (LBGRule) vertex + ",";
			}
			throw new LBGEngineException("Cyclic references detected between rules " + s);
		}
	}

	private static void addDimensions(SimpleDirectedGraph<Object, Object> ruleGraph,
	    Map<String, SortedSet<Integer>> dimensionValues, Map<String, Integer> dimensionArity)
	    throws LBGEngineException {
		for (String dimensionName : dimensionValues.keySet()) {
			assert dimensionArity.containsKey(dimensionName);

			addDimension(ruleGraph, dimensionName, dimensionValues.get(dimensionName),
			    dimensionArity.get(dimensionName));
		}
	}

	private static void addDimension(SimpleDirectedGraph<Object, Object> ruleGraph, String dimensionName,
	    SortedSet<Integer> values, int arity)
	    throws LBGEngineException {
		assert values != null;

		Iterator<Integer> it = values.iterator();
		if (it.hasNext()) {
			int previous = it.next();
			while (it.hasNext()) {
				int current = it.next();
				if ((previous >= 0 && current > 0) || (previous < 0 && current < 0)) {
					LBGRule rule = createDimensionRule(dimensionName, arity, previous, current);
					LBGUtils.addRule(ruleGraph, rule);
				}
				previous = current;
			}
		}

	}

	private static LBGRule createDimensionRule(String dimensionName, int arity, int previous, int current) {
		assert (previous >= 0 && current > 0) || (previous < 0 && current < 0);

		LBGTerm[] previousTerms = createTermArrayForDimensionRule(arity, previous);
		LBGTerm[] currentTerms = createTermArrayForDimensionRule(arity, current);

		LBGAtom previousAtom = new LBGAtom(dimensionName, previousTerms);
		LBGAtom currentAtom = new LBGAtom(dimensionName, currentTerms);
		if (current < 0) {
			return new LBGRule("", currentAtom, previousAtom);
		} else {
			return new LBGRule("", previousAtom, currentAtom);
		}
	}

	private static LBGTerm[] createTermArrayForDimensionRule(int arity, int value) {
		LBGTerm[] array = new LBGTerm[arity];
		for (int i = 0; i < arity - 1; ++i) {
			array[i] = new LBGTerm(LBGTerm.TERM_TYPE.VARIABLE, "Y" + i);
		}
		array[arity - 1] = createTermForDimensionRule(value);
		return array;
	}

	private static LBGTerm createTermForDimensionRule(int value) {
		if (DiscretCompType.ALWAYS == value) {
			return new LBGTerm(LBGTerm.TERM_TYPE.CONSTANT, "always");
		} else if (DiscretCompType.NEVER == value) {
			return new LBGTerm(LBGTerm.TERM_TYPE.CONSTANT, "never");
		} else {
			return new LBGTerm(LBGTerm.TERM_TYPE.INTEGER, value);
		}
	}

	private static LBGCombinationSet explore(LBGEngineData data, LBGAtom parent, LBGRule rule,
	    HashMap<LBGAtom, LBGCombinationSet> mByAtom, SimpleDirectedGraph<Object, Object> ruleGraph)
	    throws LBGEngineException {
		LBGCombinationSet m = null;
		Set<Object> hypAtoms = ruleGraph.outgoingEdgesOf(rule);
		for (Object edge : hypAtoms) {
			LBGAtom atom = (LBGAtom) ruleGraph.getEdgeTarget(edge);
			LBGCombinationSet m2 = explore(data, parent, rule, atom, mByAtom, ruleGraph);
			if (m2 != null) {
				if (m == null)
					m = m2;
				else
					m = m.and(m2);
			}
		}
		return m;
	}

	private static LBGCombinationSet explore(LBGEngineData data, LBGAtom parent, LBGRule origin_rule, LBGAtom atom,
	    HashMap<LBGAtom, LBGCombinationSet> mByAtom, SimpleDirectedGraph<Object, Object> ruleGraph)
	    throws LBGEngineException {
		LBGCombinationSet m;
		if (ruleGraph.outDegreeOf(atom) == 0) {
			m = new LBGCombinationSet();
			int numAttr = declComputable(data, atom);
			LBGCombination b = new LBGCombination();
			b.add(numAttr);
			b.addRuleTrace(origin_rule);
			m.add(b);
		} else {
			m = mByAtom.get(atom);
			if (m == null) {
				m = new LBGCombinationSet();
				for (Object edge : ruleGraph.outgoingEdgesOf(atom)) {
					// explore disjunction of rules
					LBGRule rule = (LBGRule) ruleGraph.getEdgeTarget(edge);
					LBGCombinationSet m2 = explore(data, parent, rule, mByAtom, ruleGraph);
					if (m2 != null) {
						m = m.or(m2);
					}
				}
				mByAtom.put(atom, m);
			}
		}
		return m;
	}

	private static int declCombination(LBGEngineData data, LBGCombination b) {
		for (int numobj = 0; numobj < data.combinations.size(); numobj++) {
			if (b.combination.equals(data.combinations.get(numobj).combination))
				return numobj;
		}
		data.combinations.add(b);
		return data.combinations.size() - 1;
	}

	private static int declComputable(LBGEngineData data, LBGAtom atom) throws IllegalArgumentException {
		checkComputablePredicate(atom, data.criterions, data.filters);
		int i = data.computables.indexOf(atom);
		if (i < 0) {
			i = data.computables.size();
			data.computables.add(atom);
		}
		return i;
	}

	private static void checkComputablePredicate(LBGAtom atom, Map<String, Criterion> criterions,
	    Map<String, Filter> filters)
	    throws IllegalArgumentException {
		String predicate = DlpUtils.removeNotFromPredicateName(atom.getPredicate());

		if (!criterions.containsKey(predicate) && !filters.containsKey(predicate)) {
			throw new IllegalArgumentException("Undeclared computable predicate or unreachable dimension value: "
			                                   + atom);
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	PACKAGE PRIVATE CLASS
	// /////////////////////////////////////////////////////////////////////////

	class LBGEngineData {
		final Map<String, Filter> filters = new HashMap<String, Filter>();
		final Map<String, Criterion> criterions = new HashMap<String, Criterion>();
		
		final List<LBGAtom> computables = new ArrayList<LBGAtom>();
		final List<LBGCombination> combinations = new ArrayList<LBGCombination>();
		final Map<LBGAtom, MyBitSet> combinationsByAtom = new HashMap<LBGAtom, MyBitSet>();
		/** The AOC poset resulting of compile() function or loaded from xml */
		AOCPosetData aocPoset;
	}
}
