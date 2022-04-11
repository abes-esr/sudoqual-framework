/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.feature.UpdateableComputedFeature;
import fr.abes.sudoqual.util.json.JSONArrays;

/**
 * This class products a JSONArray feature as the union of all JSONArray related features from sameAs
 * references.
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public abstract class AJSONArrayUnionComputedFeature implements UpdateableComputedFeature<JSONArray> {

	/**
	 * Process elements of JSONArray to concat
	 *
	 * @param o
	 *              an element of a JSONArray to concat
	 * @return an Object to add in the concatened array
	 */
	public Object process(Object o) {
		return o;
	}

	@Override
	public JSONArray compute(Collection<JSONObject> selectedData) {
		return update(null, selectedData);
	}

	@Override
	public JSONArray update(JSONArray old, Collection<JSONObject> selectedData) {
		JSONArray res = (old != null)? old : new JSONArray();
		for (JSONObject json : selectedData) {
			if (json.has(this.getRelatedFeature()) && this.check(json)) {
				Object value = json.get(this.getRelatedFeature());
				if(value instanceof JSONArray) {
					for (Object o : (JSONArray) value) {
						Object toAdd = process(o);
						if (!JSONArrays.contains(res, toAdd)) {
							res.put(toAdd);
						}
					}
				} else {
					Object toAdd = process(value);
					if (!JSONArrays.contains(res, toAdd)) {
						res.put(toAdd);
					}
				}
			}
		}
		return res;
	}

	public abstract String  getRelatedFeature();

	@Override
	public Set<String> getRelatedFeatures() {
		return Collections.singleton(this.getRelatedFeature());
	}

	public boolean check(JSONObject json) {
		return true;
	}

	@Override
	public boolean checkValue(Object o) {
		return o instanceof JSONArray;
	}

}
