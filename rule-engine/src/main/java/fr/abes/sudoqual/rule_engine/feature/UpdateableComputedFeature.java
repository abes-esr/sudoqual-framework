/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.feature;

import java.util.Collection;

import org.json.JSONObject;

/**
 * A computed feature which can be updated without recompute everything from scratch.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface UpdateableComputedFeature<T> extends ComputedFeature<T> {

	/**
	 * Update the old computed value with the new data from newly related references given
	 * by the parameter newlySelectedData.
	 * @param oldValue
	 * @param newlySelectedData
	 * @return the computed value of this feature
	 */
	T update(T oldValue, Collection<JSONObject> newlySelectedData);

}
