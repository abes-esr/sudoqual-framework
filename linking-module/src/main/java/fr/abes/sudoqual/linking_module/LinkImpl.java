/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;
import fr.abes.sudoqual.linking_module.heuristic.Candidate;
import fr.abes.sudoqual.rule_engine.Atom;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.Rule;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class LinkImpl implements Link {

	private final Type type;
	private final Reference source;
	private final Reference target;
	private final JSONObject whySameAs;
	private final JSONObject whyDiffFrom;
	private final int confidence;
	private int step;

	public LinkImpl(Type type, Reference source, Reference target) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.whySameAs = this.whyDiffFrom = null;
		this.step = -1;
		this.confidence = Integer.MAX_VALUE;
	}

	public LinkImpl(Type type, Reference source, Candidate candidate) {
		this.type = type;
		this.source = source;
		this.target = candidate.target;
		this.whySameAs = new JSONObject();
		this.whySameAs.put("rules", whyToListOfString(candidate.sameAsResult.why()));
		this.whySameAs.put("predicates", whyToListOfPredicates(candidate.sameAsResult.why()));

		this.whyDiffFrom = new JSONObject();
		this.whyDiffFrom.put("rules", whyToListOfString(candidate.diffFromResult.why()));
		this.whyDiffFrom.put("predicates", whyToListOfPredicates(candidate.diffFromResult.why()));
		if(type == Type.DIFF_FROM) {
			this.confidence = -candidate.proximityClue;
		} else {
			this.confidence = candidate.proximityClue;
		}
		this.step = -1;
	}


	public LinkImpl(Type type, Reference source, Candidate candidate, String heuristicSameAsWhy,
			String heuristicDiffFromWhy) {
		this(type, source, candidate);
		if(heuristicSameAsWhy != null) {
			this.whySameAs.append("heuristic", heuristicSameAsWhy);
		}
		if(heuristicDiffFromWhy != null) {
			this.whyDiffFrom.append("heuristic", heuristicDiffFromWhy);
		}
	}

	public LinkImpl(Type type, Reference source, Reference target, int step, int confidence, JSONObject whySameAs, JSONObject whyDiffFrom) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.step = step;
		this.confidence = confidence;
		this.whySameAs = whySameAs;
		this.whyDiffFrom = whyDiffFrom;
	}



	@Override
	public Reference getSource() {
		return this.source;
	}

	@Override
	public Reference getTarget() {
		return this.target;
	}

	@Override
	public JSONObject getWhySameAs() {
		return this.whySameAs;
	}

	@Override
	public JSONObject getWhyDiffFrom() {
		return this.whyDiffFrom;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public int getConfidence() {
		return this.confidence;
	}

	@Override
	public int getStep() {
		return this.step;
	}

	@Override
	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Link//")
		.append("type: ").append(this.type)
		.append(", source: ").append(this.source)
		.append(", target: ").append(this.target)
		.append(", confidence: ").append(this.confidence);
		if(step >= 0) {
			sb.append(", step: ").append(this.step);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || !(o instanceof Link)) {
			return false;
		}
		Link other = (Link)o;
        return Objects.equals(this.getSource(), other.getSource())
            && Objects.equals(this.getTarget(), other.getTarget())
            && Objects.equals(this.getType(), other.getType());
    }

	@Override
	public int hashCode() {
		return Objects.hash(this.getSource(), this.getTarget(), this.getType());
	}


	public static List<String> whyToListOfString(List<Rule> why) {
		List<String> res = new LinkedList<>();
		for(Rule r : why) {
			res.add(r.toString());
		}
		return res;
	}

	private static List<String> whyToListOfPredicates(List<Rule> why) {
		List<String> res = new LinkedList<>();
		for(Rule r : why) {
			for(Atom a : r.getHypothesis()) {
				res.add(a.getPredicate());
			}
		}
		return res;
	}
}
