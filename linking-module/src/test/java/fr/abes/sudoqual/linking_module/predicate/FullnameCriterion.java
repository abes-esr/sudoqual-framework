package fr.abes.sudoqual.linking_module.predicate;

import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.util.CollectionUtils;
import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class FullnameCriterion implements Criterion {
	
	public static final Set<String> featureSet = CollectionUtils.setFrom("firstname", "lastname");

	@Override
	public DiscretCompType getComparisonType() {
		return null;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return featureSet;
	}

	@Override
	public Set<String> targetFeatureSet() {
		return featureSet;
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		if( ref1.get("firstname").equals(ref2.get("firstname"))
				&& ref1.get("lastname").equals(ref2.get("lastname")))
			return 2;
		return 0;
	}

}
