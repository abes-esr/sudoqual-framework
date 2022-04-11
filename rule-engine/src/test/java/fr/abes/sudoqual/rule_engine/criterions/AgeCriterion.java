package fr.abes.sudoqual.rule_engine.criterions;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class AgeCriterion implements Criterion {
	

	private static final DiscretCompType compType = new DiscretCompTypeImpl(true, -1, true, 2, true);
	
	@Override
	public String getKey() {
		return "ageCriterion";
	}
	

	@Override
	public DiscretCompType getComparisonType() {
		return compType;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return Collections.singleton("age");
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Collections.singleton("age");
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		int age1 = ref1.getInt("age");
		int age2 = ref2.getInt("age");
		int diff = Math.abs(age1 - age2);
		if (diff > 10) {
			return -1;
		}
		if (diff > 5) {
			return 0;
		}
		if (diff <= 1) {
			return 2;
		}
		return 1;

	}

}
