/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.Scenario;
import fr.abes.sudoqual.linking_module.exception.ScenarioException;
import fr.abes.sudoqual.linking_module.heuristic.BasicLinkHeuristic;
import fr.abes.sudoqual.linking_module.util.PropertiesException;
import fr.abes.sudoqual.linking_module.util.PropertiesUtils;
import fr.abes.sudoqual.util.ConfigurationPropertiesException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ScenarioFromProperties implements Scenario {

	protected Properties properties;
	
	protected String version;
	protected String ruleSet;
	protected String heuristicMode; 
	protected int validatedSameAsThreshold;
	protected int suggestedSameAsThreshold;
	protected int validatedDiffFromThreshold;
	protected boolean suggestedEnabled;
	protected boolean keepOnlyBestSuggestionsEnabled;
	protected boolean exportCriterionValues;
	protected boolean undeclaredRawFeaturesEnabled;
	protected boolean dataValidationEnabled;
	protected String heuristicName;
	protected String businessClassPackageName;

	private final Set<String> lookupPaths;


	public ScenarioFromProperties(URL propertiesFile, Set<String> lookupPaths) throws ScenarioException {
		this.lookupPaths = Collections.unmodifiableSet(lookupPaths);
		
		properties = new Properties();

		try (InputStream stream = propertiesFile.openStream()) {
			if (stream == null) {
				throw new ScenarioException("Scenario file '"
				                            + propertiesFile.getPath()
				                            + "' not found in the classpath");
			}
			properties.load(stream);

			this.version = properties.getProperty(VERSION_KEY, "?");
			this.ruleSet = properties.getProperty(RULESET_KEY);
			this.heuristicName = properties.getProperty(HEURISTIC_NAME_KEY, BasicLinkHeuristic.NAME);
			this.heuristicMode = properties.getProperty(HEURISTIC_MODE_KEY);
			this.businessClassPackageName = properties.getProperty(BUSINESS_CLASS_PACKAGE, LinkingModule.class.getPackage().getName());
			this.validatedSameAsThreshold = PropertiesUtils.readInteger(properties, VALIDATED_SAME_AS_THRESHOLD_KEY);
			this.suggestedSameAsThreshold = PropertiesUtils.readInteger(properties, SUGGESTED_SAME_AS_THRESHOLD_KEY);
			this.validatedDiffFromThreshold = PropertiesUtils.readInteger(properties, VALIDATED_DIFF_FROM_THRESHOLD_KEY);
			this.suggestedEnabled = PropertiesUtils.readBoolean(properties, SUGGESTED_ENABLED_KEY);
			this.keepOnlyBestSuggestionsEnabled = PropertiesUtils.readBoolean(properties, KEEP_ONLY_BEST_SUGGESTIONS_KEY);
			this.exportCriterionValues = PropertiesUtils.readBoolean(properties, EXPORT_CRITERION_VALUES_KEY);
			this.undeclaredRawFeaturesEnabled = PropertiesUtils.readBoolean(properties, UNDECLARED_RAW_FEATURE_ENABLED, false);
			this.dataValidationEnabled = PropertiesUtils.readBoolean(properties, DATA_VALIDATION_ENABLED, true);

		} catch (IOException | PropertiesException e) {
			throw new ScenarioException("An error occured when reading scenario file: " + propertiesFile.getPath(), e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	//
	// /////////////////////////////////////////////////////////////////////////
	
	@Override
	public Set<String> getLookupPaths() {
		return this.lookupPaths;
	}
	
	@Override
	public String get(String key) {
		return properties.getProperty(key);
	}
	
	@Override
	public String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	@Override
	public int getInteger(String key) throws ConfigurationPropertiesException {
		try {
			return PropertiesUtils.readInteger(this.properties, key);
		} catch (PropertiesException e) {
			throw new ConfigurationPropertiesException("Error when reading following property: " + key, e);
		}
	}
	
	@Override
	public int getInteger(String key, Integer defaultValue) throws ConfigurationPropertiesException {
		try {
			return PropertiesUtils.readInteger(this.properties, key, defaultValue);
		} catch (PropertiesException e) {
			throw new ConfigurationPropertiesException("Error when reading following property: " + key, e);
		}
	}
	
	@Override
	public boolean getBoolean(String key) throws ConfigurationPropertiesException {
		try {
			return PropertiesUtils.readBoolean(this.properties, key);
		} catch (PropertiesException e) {
			throw new ConfigurationPropertiesException("Error when reading following property: " + key, e);
		}
	}
	
	@Override
	public boolean getBoolean(String key, Boolean defaultValue) throws ConfigurationPropertiesException {
		try {
			return PropertiesUtils.readBoolean(this.properties, key, defaultValue);
		} catch (PropertiesException e) {
			throw new ConfigurationPropertiesException("Error when reading following property: " + key, e);
		}
	}

	@Override
	public String getBusinessClassPackageName() {
		return businessClassPackageName;
	}

	@Override
	public String getRuleSetFileName() {
		return ruleSet;
	}
	
	@Override
	public String getHeuristicMode() {
		return heuristicMode;
	}

	@Override
	public int getValidatedSameAsThreshold() {
		return validatedSameAsThreshold;
	}

	@Override
	public int getSuggestedSameAsThreshold() {
		return suggestedSameAsThreshold;
	}

	@Override
	public int getValidatedDiffFromThreshold() {
		return validatedDiffFromThreshold;
	}

	@Override
	public boolean isSuggestedEnabled() {
		return suggestedEnabled;
	}

	@Override
	public boolean isKeepOnlyBestSuggestionsEnabled() {
		return keepOnlyBestSuggestionsEnabled;
	}

	@Override
	public boolean isExportCriterionValuesEnabled() {
		return exportCriterionValues;
	}

	@Override
	public String getHeuristicName() {
		return heuristicName;
	}

	@Override
	public boolean isUndeclaredRawFeatureEnabled() {
		return undeclaredRawFeaturesEnabled;
	}

	@Override
	public boolean isDataValidationEnabled() {
		return this.dataValidationEnabled;
	}

}
