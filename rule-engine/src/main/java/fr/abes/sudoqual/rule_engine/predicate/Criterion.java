/*
 * This file is part of SudoQual project.
 * Created in 2018-08.
 */
package fr.abes.sudoqual.rule_engine.predicate;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.predicate.exception.InconsistentException;
import fr.abes.sudoqual.rule_engine.predicate.exception.NotComparableException;

/**
 * A criterion represents a comparison function between two {@link Reference} based
 * on a set of features (see {@link Feature}) from the source reference and a set of 
 * features from the target reference.
 * The admissible values of this function is defined by {@link Criterion#getComparisonType()}.
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Criterion extends Predicate {
	
	@Override
	default String getKey() {
		String name = this.getClass().getSimpleName();
		if(name.endsWith("Criterion")) {
			name = name.substring(0, name.length() - "Criterion".length());
		}
		name = StringUtils.uncapitalize(name);
		return name;
	}

	/**
	 * Get a DiscretCompType representing the co-domain of this
	 * criterion.
	 * @return a DiscretCompType representing the co-domain
	 */
	DiscretCompType getComparisonType();

	/**
	 * Set of needed features of the source reference given 
	 * as a parameter to be able to evaluate the filter
	 * @return a Set of needed features
	 */
	Set<String> sourceFeatureSet();

	/**
	 * Set of needed features of the target reference given 
	 * as a parameter to be able to evaluate the filter
	 * @return a Set of needed features
	 */
	Set<String> targetFeatureSet();

	/**
	 * The evaluation function.
	 * @param source a JsonObject containing all needed features the first reference
	 * @param target a JsonObject containing all needed features the second reference
	 * @return an integer consistent with the DiscretCompType attached to this criterion.
	 */
	int compare(JSONObject source, JSONObject target);
	
}
