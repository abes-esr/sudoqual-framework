package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.SimpleDirectedGraph;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGAtom;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGRule;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm.TERM_TYPE;

final class LBGUtils {

	private LBGUtils() {}
	
	/**
	 * This method extract the "value" attached to the specified atom. The "value"
	 * is the weight given to the relation represented by the atom. This "value" is
	 * either an integer or a special constant "never", "always" or "neutral" which
	 * must be placed at the last term position of the atom.
	 * 
	 * @param atom
	 * @return an int representing the value attached to the specified atom. If the
	 *         last term of the atom is neither an integer nor one of the allowed
	 *         constants, this method will return 0.
	 * @throws LBGEngineException
	 */
	public static int getValueOf(LBGAtom atom) throws LBGEngineException {
		return getValueOf(atom.getTerm(atom.getArity() - 1));
	}
	
	public static Integer getValueOf(LBGTerm term) throws LBGEngineException {
		Integer res;
		if (term.getType() == LBGTerm.TERM_TYPE.CONSTANT) {
			if ("never".equals(term.getValue())) {
				res = DiscretCompType.NEVER;
			} else if ("always".equals(term.getValue())) {
				res = DiscretCompType.ALWAYS;
			} else if ("neutral".equals(term.getValue())) {
				res = DiscretCompType.NEUTRAL;
			} else
				throw new LBGEngineException("unknown constant " + term);
		} else if (term.getType() == LBGTerm.TERM_TYPE.INTEGER) {
			res = (Integer) term.getValue();
		} else {
			throw new LBGEngineException("wrong value for " + term);
		}
		return res;
	}
	
	public  static void addRule(SimpleDirectedGraph<Object, Object> ruleGraph, LBGRule rule) throws LBGEngineException {
		LBGRule aRule = anonimizeRule(rule);
		ruleGraph.addVertex(aRule);
		if (!ruleGraph.containsVertex(aRule.getConclusion()))
			ruleGraph.addVertex(aRule.getConclusion());
		if (!ruleGraph.containsEdge(aRule.getConclusion(), aRule))
			ruleGraph.addEdge(aRule.getConclusion(), aRule);
		for (LBGAtom atomHyp : aRule.getHypothesis()) {
			if (!ruleGraph.containsVertex(atomHyp))
				ruleGraph.addVertex(atomHyp);
			if (!ruleGraph.containsEdge(atomHyp, aRule))
				ruleGraph.addEdge(aRule, atomHyp);
		}
	}
	
	public  static LBGRule anonimizeRule(LBGRule rule) throws LBGEngineException {
		LBGAtom conclusion = rule.getConclusion();
		LBGTerm[] newTerms = new LBGTerm[conclusion.getArity()]; 
		Map<LBGTerm, LBGTerm> map = new HashMap<>();
		for(int i = 0; i < conclusion.getArity(); ++i) {
			LBGTerm t = conclusion.getTerm(i);
			if(t.getType() == TERM_TYPE.VARIABLE) {
				newTerms[i] = new LBGTerm(TERM_TYPE.VARIABLE, "X" + i);
				map.put(t, newTerms[i]);
			} else {
				newTerms[i] = t;
			}
		}
		LBGRule newRule = new LBGRule(rule.getName(), new LBGAtom(conclusion.getPredicate(), newTerms));
		for(LBGAtom atom : rule.getHypothesis()) {
			try {
				newRule.addHypAtom(anomimizeAtom(atom, map));
			} catch (LBGEngineException e) {
				throw new LBGEngineException("Invalid rule " + rule);
			}
		}
		return newRule;
	}

	public static LBGAtom anomimizeAtom(LBGAtom atom, Map<LBGTerm, LBGTerm> map) throws LBGEngineException {
		LBGTerm[] newTerms = new LBGTerm[atom.getArity()]; 
		for(int i = 0; i < atom.getArity(); ++i) {
			LBGTerm t = atom.getTerm(i);
			if(t.getType() == TERM_TYPE.VARIABLE) {
				newTerms[i] = map.get(t);
				if(newTerms[i] == null) {
					throw new LBGEngineException("Unknown variable " + t + " in " + atom);
				}
			} else {
				newTerms[i] = t;
			}
		}
		return new LBGAtom(atom.getPredicate(), newTerms);
	}

}
