package fr.abes.sudoqual.rule_engine.criterions;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.features.Interval;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class PubDateCriterion implements Criterion {
	
	private static final DiscretCompType compType = new DiscretCompTypeImpl(true, 0, true, 1, true);


	@Override
	public String getKey() {
		return "pubDateCriterion";
	}

	@Override
	public DiscretCompType getComparisonType() {
		return compType;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return Collections.singleton("pubDateAggregate");
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Collections.singleton("pubDate");
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		Interval interval = (Interval) ref1.get("pubDateAggregate");
		int year = ref2.getInt("pubDate");
		if(interval != null && year >= interval.min && year <= interval.max) {
			return 1;
		}
		return 0;
	}

}
