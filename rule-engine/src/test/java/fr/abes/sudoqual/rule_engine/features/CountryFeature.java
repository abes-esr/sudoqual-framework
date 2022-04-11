package fr.abes.sudoqual.rule_engine.features;

import java.util.Arrays;
import java.util.Collection;

import org.json.JSONObject;


public class CountryFeature extends AComputedFeatureImpl<String> {
	

	public CountryFeature() {
		super("country", Arrays.asList("country"));
	}

	@Override
	public String compute(Collection<JSONObject> selectedData) {
		return ((JSONObject)selectedData.iterator().next()).getString(this.getRelatedFeatures().iterator().next());
	}

}
