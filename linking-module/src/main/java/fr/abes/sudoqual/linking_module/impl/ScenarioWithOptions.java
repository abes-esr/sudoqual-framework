/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.impl;

import java.net.URL;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.exception.ScenarioException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ScenarioWithOptions extends ScenarioFromProperties {

	protected boolean debug;

	public ScenarioWithOptions(URL propertiesFile, JSONObject options, Set<String> lookupPath) throws ScenarioException {
		super(propertiesFile, lookupPath);
		if(options != null) {
    		this.debug = options.has("debug") && options.getBoolean("debug");
    		if(options.has(VALIDATED_SAME_AS_THRESHOLD_KEY)) {
    			this.validatedSameAsThreshold = options.optInt(VALIDATED_SAME_AS_THRESHOLD_KEY);
    			this.properties.setProperty(VALIDATED_SAME_AS_THRESHOLD_KEY, Integer.toString(this.validatedSameAsThreshold));
    		}
    		if(options.has(SUGGESTED_SAME_AS_THRESHOLD_KEY)) {
    			this.suggestedSameAsThreshold = options.getInt(SUGGESTED_SAME_AS_THRESHOLD_KEY);
    			this.properties.setProperty(SUGGESTED_SAME_AS_THRESHOLD_KEY, Integer.toString(this.suggestedSameAsThreshold));
    		}
    		if(options.has(VALIDATED_DIFF_FROM_THRESHOLD_KEY)) {
    			this.validatedDiffFromThreshold = options.getInt(VALIDATED_SAME_AS_THRESHOLD_KEY);
    			this.properties.setProperty(VALIDATED_DIFF_FROM_THRESHOLD_KEY, Integer.toString(this.validatedDiffFromThreshold));
    		}
    		if(options.has(SUGGESTED_ENABLED_KEY)) {
    			this.suggestedEnabled = options.getBoolean(SUGGESTED_ENABLED_KEY);
    			this.properties.setProperty(SUGGESTED_ENABLED_KEY, Boolean.toString(this.suggestedEnabled));
    		}
    		if(options.has(KEEP_ONLY_BEST_SUGGESTIONS_KEY)) {
    			this.keepOnlyBestSuggestionsEnabled = options.getBoolean(KEEP_ONLY_BEST_SUGGESTIONS_KEY);
    			this.properties.setProperty(KEEP_ONLY_BEST_SUGGESTIONS_KEY, Boolean.toString(this.keepOnlyBestSuggestionsEnabled));
    		}
    		if(options.has(EXPORT_CRITERION_VALUES_KEY)) {
    			this.exportCriterionValues = options.getBoolean(EXPORT_CRITERION_VALUES_KEY);
    			this.properties.setProperty(EXPORT_CRITERION_VALUES_KEY, Boolean.toString(this.exportCriterionValues));
    		}
    		if(options.has(DATA_VALIDATION_ENABLED)) {
    			this.dataValidationEnabled = options.getBoolean(DATA_VALIDATION_ENABLED);
    			this.properties.setProperty(DATA_VALIDATION_ENABLED, Boolean.toString(this.dataValidationEnabled));
    		}
		} else {
			this.debug = false;
		}
	}
	
	public boolean isDebugEnabled() {
		return this.debug;
	}



}
