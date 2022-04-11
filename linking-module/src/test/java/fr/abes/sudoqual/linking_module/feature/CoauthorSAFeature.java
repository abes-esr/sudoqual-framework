package fr.abes.sudoqual.linking_module.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.feature.UpdateableComputedFeature;

public class CoauthorSAFeature implements UpdateableComputedFeature<Set> {

	private static final String COAUTHORS_KEY = "coauthors";

	@Override
	public Set<String> getRelatedFeatures() {
		return Collections.singleton(COAUTHORS_KEY);
	}

	@Override
	public Set update(Set old, Collection<JSONObject> selectedData) {
		Set res;
		if(old == null) {
			res = new HashSet();
		} else {
			res = old;
		}

		for(Object o : selectedData) {
			if(((JSONObject)o).has(COAUTHORS_KEY)) {
    			JSONArray array = ((JSONObject)o).getJSONArray(COAUTHORS_KEY);
				for (Object e : array) {
					res.add(e);
				}
			}
		}
		return res;
	}

	@Override
	public Set compute(Collection<JSONObject> selectedData) {
		assert selectedData != null : "'data' argument  is null";

		Set res = new HashSet();
		for(Object o : selectedData) {
			if(o instanceof Iterable) {
				Iterable array = (Iterable)o;
				for (Object e : array) {
					res.add(e);
				}
			}
		}
		return res;
	}

}
