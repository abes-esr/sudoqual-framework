/*
 * This file is part of SudoQual project.
 * Created in 2018-08.
 */
package fr.abes.sudoqual.rule_engine.feature;

import java.util.Collection;
import java.util.Set;

import org.json.JSONObject;

/**
 * A Feature which can be computed from other features of related references.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface ComputedFeature<T> extends Feature {
	
	/**
	 * Gets feature names that are needed to compute this one.
	 * @return a collection of needed feature names.
	 */
	Set<String> getRelatedFeatures();
	
	/**
	 * The computation function
	 * @param json 
	 * @param store
	 * @return the computed value of this feature
	 */
	T compute(Collection<JSONObject> selectedData);

}
