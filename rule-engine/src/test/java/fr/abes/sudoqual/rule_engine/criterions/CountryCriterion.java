package fr.abes.sudoqual.rule_engine.criterions;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class CountryCriterion implements Criterion {

	@Override
	public String getKey() {
		return "countryCriterion";
	}

	private static final DiscretCompType compType = new DiscretCompTypeImpl(true, 0, true, 1, true);

	@Override
	public DiscretCompType getComparisonType() {
		return compType;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return Collections.singleton("country");
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Collections.singleton("country");
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		String town1 = ref1.getString("country");
		String town2 = ref2.getString("country");
		if (town1.equals(town2)) {
			return 1;
		} else {
			return 0;
		}

	}

}
