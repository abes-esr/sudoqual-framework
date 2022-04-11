/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.heuristic.Candidate;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.rule_engine.Reference;

/**
 * A Link represents a sameAs, diffFrom or suggestedSameAs between two references. It
 * can also contains metadata about this link.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 * @see Reference
 */
public interface Link {
	
	static Link create(Type type, Reference source, Reference target) {
		return new LinkImpl(type, source, target);
	}
	
	static Link create(Type type, Reference source, Candidate candidate) {
		return new LinkImpl(type, source, candidate);
	}
	
	static Link create(Type type, Reference source, Candidate candidate, String heuristicSameAsWhy, String heuristicDiffFromWhy) {
		return new LinkImpl(type, source, candidate, heuristicSameAsWhy, heuristicDiffFromWhy);
	}
	
	static Link create(Type type, Reference source, Reference target, int step, int confidence, JSONObject whySameAs, JSONObject whyDiffFrom, String downgradeCause) {
		whySameAs.append("heuristic", downgradeCause);
		return new LinkImpl(type, source, target, step, confidence, whySameAs, whyDiffFrom);
	}
	
	enum Type {
		SAME_AS,
		SUGGESTED,
		DIFF_FROM;
		
		@Override
		public String toString() {
			switch(this) {
				case SAME_AS:
					return "sameAs";
				case SUGGESTED:
					return "suggestedSameAs";
				case DIFF_FROM:
					return "diffFrom";
				default:
					throw new Error("Should never happen");
			}
		}
	}

	Reference getSource();

	Reference getTarget();

	JSONObject getWhySameAs();
	
	JSONObject getWhyDiffFrom();

	Type getType();

	/**
	 * The aggregation of the sameAs and diffFrom rule weight triggered for this couple of reference.
	 * @return
	 * @see {@link LinkHeuristic#computeProximity(Reference, fr.abes.sudoqual.rule_engine.RuleEngine.Result, Reference, fr.abes.sudoqual.rule_engine.RuleEngine.Result)
	 */
	int getConfidence();

	/**
	 * Gets the step of the {@link LinkingModuleAlgorithm} in which this link was computed
	 * @return
	 */
	int getStep();
	
	/**
	 * Sets the information about the step of the {@link LinkingModuleAlgorithm} in which this link was computed.
	 * @param step
	 */
	void setStep(int step);
	
}
