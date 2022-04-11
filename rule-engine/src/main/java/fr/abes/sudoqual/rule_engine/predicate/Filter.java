/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine.predicate;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.feature.Feature;

/**
 * A filter represents a boolean function based on a 
 * set of features (see {@link Feature}) from related to a {@link Reference}.
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Filter extends Predicate {
	
	@Override
	default String getKey() {
		String name = this.getClass().getSimpleName();
		if(name.endsWith("Filter")) {
			name = name.substring(0, name.length() - "Filter".length());
		}
		name = StringUtils.uncapitalize(name);
		return name;
	}

	/**
	 * Set of needed features to be able to evaluate the filter
	 * @return a Set of needed features
	 */
	Set<String> featureSet();

	/**
	 * The evaluation function
	 * @param data a JsonObject containing all needed features
	 * @return true if the features fulfill the filter requirement, false otherwise.
	 */
	boolean check(JSONObject data);
}
