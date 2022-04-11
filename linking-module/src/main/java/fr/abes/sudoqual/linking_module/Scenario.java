/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import fr.abes.sudoqual.util.ConfigurationProperties;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Scenario extends ConfigurationProperties {
	
	String VERSION_KEY = "version";
	String RULESET_KEY = "ruleset";
	String HEURISTIC_NAME_KEY = "heuristic";
	String HEURISTIC_MODE_KEY = "heuristicMode";
	String BUSINESS_CLASS_PACKAGE = "businessClassPackage";
	String VALIDATED_SAME_AS_THRESHOLD_KEY = "validatedSameAsThreshold";
	String SUGGESTED_SAME_AS_THRESHOLD_KEY = "suggestedSameAsThreshold";
	String VALIDATED_DIFF_FROM_THRESHOLD_KEY = "validatedDiffFromThreshold";
	String SUGGESTED_ENABLED_KEY = "suggestedEnabled";
	String KEEP_ONLY_BEST_SUGGESTIONS_KEY = "keepOnlyBestSuggestions";
	String EXPORT_CRITERION_VALUES_KEY = "exportCriterionValues";
	String UNDECLARED_RAW_FEATURE_ENABLED = "undeclaredRawFeaturesEnabled";
	String DATA_VALIDATION_ENABLED = "dataValidationEnabled";
	
	String getRuleSetFileName();
	
	String getHeuristicName();

	int getValidatedSameAsThreshold();

	int getSuggestedSameAsThreshold();

	int getValidatedDiffFromThreshold();

	boolean isSuggestedEnabled();

	boolean isKeepOnlyBestSuggestionsEnabled();

	boolean isExportCriterionValuesEnabled();
	
	boolean isUndeclaredRawFeatureEnabled();
	
	boolean isDataValidationEnabled();

	String getHeuristicMode();

	String getBusinessClassPackageName();

}
