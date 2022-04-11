package fr.abes.sudoqual.modules.diagnostic;

import java.util.Objects;

import org.json.JSONObject;

class TargetWithWhy {
	private final String target;
	private final int confidence;
	private final JSONObject why;

	public static TargetWithWhy instance(String target, int confidence, JSONObject why) {
		return new TargetWithWhy(target, confidence, why);
	}

	private TargetWithWhy(String ref, int confidence, JSONObject why) {
		this.target = ref;
		this.why = why;
		this.confidence = confidence;
	}

	public String getTarget() {
		return target;
	}

	public JSONObject getWhy() {
		return why;
	}

	public int getConfidence() {
		return this.confidence;
	}

	@Override
	public int hashCode() {
		return Objects.hash(target, confidence, why);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TargetWithWhy other = (TargetWithWhy) obj;
		return Objects.equals(target, other.target) && confidence == other.confidence && Objects.equals(why, other.why);
	}

	@Override
	public String toString() {
		return "RefWithWhy [ref=" + target + ", confidence=" + confidence + ", why=" + why + "]";
	}


}
