package fr.abes.sudoqual.rule_engine.features;

import java.util.Arrays;
import java.util.Collection;

import org.json.JSONObject;

public class PubDateAggregateFeature extends AComputedFeatureImpl<Interval> {

	public PubDateAggregateFeature() {
		super("pubDateAggregate",  Arrays.asList("pubDate"));
	}
	
	@Override
	public boolean checkValue(Object o) {
		return true;
	}


	@Override
	public Interval compute(Collection<JSONObject> selectedData) {
		int min = 0;
		int max = 0;
		boolean isFirst = true;
		for(JSONObject e : selectedData) {
			int val = ((JSONObject)e).getInt("pubDate");
			if(isFirst) {
				min = max = val;
				isFirst = false;
			}
			if(val < min) {
				min = val;
			} else if (val > max) {
				max = val;
			}			
		}
		if(isFirst) {
			return null;
		} else {
			return new Interval(min, max);
		}
	}

}
