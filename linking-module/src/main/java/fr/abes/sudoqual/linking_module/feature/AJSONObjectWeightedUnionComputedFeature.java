/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.feature.UpdateableComputedFeature;

/**
 * This class products a JSONObject feature containing the weighted union of all related features
 * from sameAs references as "weightedValues", the number of truly checked elements
 * ({@link AJSONArrayUnionComputedFeature#check(JSONObject)}) as
 * "representativeness" and the number of occurrences of each encountered elements as "data".
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public abstract class AJSONObjectWeightedUnionComputedFeature implements UpdateableComputedFeature<JSONObject> {

	/**
	 * Process elements of JSONArray to concat
	 *
	 * @param o
	 *              an element of a JSONArray to concat
	 * @return an Object to add in the concatened array
	 */
	public abstract String process(Object o);

	@Override
	public JSONObject compute(Collection<JSONObject> selectedData) {
		return update(null, selectedData);
	}

	@Override
	public JSONObject update(JSONObject old, Collection<JSONObject> selectedData) {
		JSONObject res = (old != null)? old : new JSONObject();

		JSONObject data = res.optJSONObject("data");
		if(data == null) {
			data = new JSONObject();
			res.put("data", data);
		}
		int rep = res.optInt("representativeness");
		if(rep == 0) {
			res.put("representativeness", 0);
		}

		for (JSONObject json : selectedData) {
			if (json.has(this.getRelatedFeature()) && this.check(json)) {
				res.increment("representativeness");
				Object o = json.get(this.getRelatedFeature());
				String toAdd = process(o);
				data.increment(toAdd);
			}
		}

		JSONArray weightedLang = new JSONArray();
		int representativeness = res.optInt("representativeness");
		Iterator<String> it = data.keys();
		while(it.hasNext()) {
			String lang = it.next();
			JSONObject json = new JSONObject();
			json.put("value", lang);
			json.put("weight",data.getDouble(lang)/representativeness);
			weightedLang.put(json);
		}

		res.put("weightedValues", weightedLang);
		return res;
	}

	public abstract String  getRelatedFeature();

	@Override
	public Set<String> getRelatedFeatures() {
		return Collections.singleton(this.getRelatedFeature());
	}

	public boolean check(JSONObject json) {
		return json.getJSONArray(this.getRelatedFeature()).length() > 0;
	}

	@Override
	public boolean checkValue(Object o) {
		return o instanceof JSONObject;
	}

}
