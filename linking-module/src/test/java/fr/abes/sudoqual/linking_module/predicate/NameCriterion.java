package fr.abes.sudoqual.linking_module.predicate;

import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.util.CollectionUtils;
import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class NameCriterion implements Criterion {
	
	private static final String FEAT_NAME = "name";
	private static final Set<String> featureSet = CollectionUtils.setFrom(FEAT_NAME);

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
		String name1 = ref1.getString(FEAT_NAME);
		String name2 = ref2.getString(FEAT_NAME);

		return Objects.equals(name1, name2) ? 1 : 0;
	}

}
