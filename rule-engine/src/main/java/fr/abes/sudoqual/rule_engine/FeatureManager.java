/*
 * This file is part of SudoQual project.
 * Created in 2018-08.
 */
package fr.abes.sudoqual.rule_engine;

import java.util.Collection;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;


/**
 * Manages and stores values of the features.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface FeatureManager {
	
	public static final String URI_KEY = "uri";
	
	 /**
     * Check if the given reference exist
     * @param ref
     * @return true if the reference exist, false otherwise.
     */
	boolean exist(Reference ref);
	
	/**
	 * Return all features for a given reference
	 * @param ref
	 * @return a JsonObject containing all features values stored by this DataManager.
	 */
	JSONObject get(Reference ref);
	
	/**
	 * Return all features for a given reference
	 * @param refName the name of a reference
	 * @return a JsonObject containing all features values stored by this DataManager.
	 */
 	JSONObject get(String refName);
	
	/**
	 * Check if the given feature exist on the given reference.
	 * 
	 * @param ref
	 * @param featName
	 * @return true if the given reference and the given feature on this reference
	 *         exist, false otherwise.
	 */
	boolean exist(Reference ref, String featName);
	
	/**
	 * Gets a set of feature values for a reference
	 * @param ref the reference for which we ask for feature values
	 * @param features a collection of requested features 
	 * @return a JsonObject containing each requested features ("key": "value").
	 */
	JSONObject getFeaturesValue(Reference ref, Collection<String> features);
 	
	
	/**
	 * Updates the values of the given list of {@link ComputedFeature} for the given {@link Reference} with the
	 * given set of sameAs references.
	 * @param ref the reference for which data must be updated
	 * @param featList the list of attributes to be updated
	 * @param newSameAs the set of references newly declared as sameAs
	 * @param allSameAs the set of all references declared as sameAs (previously declared sameAs + newly declared sameAs)
	 */
	void updateComputedFeatures(Reference ref, Collection<ComputedFeature<?>> featList, Set<Reference> newSameAs, Set<Reference> allSameAs);


    

}
