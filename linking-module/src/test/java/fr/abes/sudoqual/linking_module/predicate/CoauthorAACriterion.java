package fr.abes.sudoqual.linking_module.predicate;

import static fr.abes.sudoqual.linking_module.impl.CriterionUtils.*;
import java.util.Collection;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.util.CollectionUtils;
import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class CoauthorAACriterion implements Criterion {
	
	private static final String FIRST_FEAT_NAME = "coauthorSA";
	private static final Set<String> firstFeatureSet = CollectionUtils.setFrom(FIRST_FEAT_NAME);
	private static final String SECOND_FEAT_NAME = "coauthorSA";
	private static final Set<String> secondFeatureSet = CollectionUtils.setFrom(SECOND_FEAT_NAME);

	@Override
	public DiscretCompType getComparisonType() {
		return null;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return firstFeatureSet;
	}

	@Override
	public Set<String> targetFeatureSet() {
		return secondFeatureSet;
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		if(!ref1.has(FIRST_FEAT_NAME) || !ref2.has(SECOND_FEAT_NAME)) {
			return 0;
		}
		Iterable<String> firstArray = (Iterable<String>) ref1.get(FIRST_FEAT_NAME);
		Iterable<String> secondArray = (Iterable<String>) ref2.get(SECOND_FEAT_NAME);

		Collection<?> c = intersection(firstArray, secondArray);
		return c.size() - 1;
	}

}
