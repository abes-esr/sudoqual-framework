/*
* This file is part of SudoQual project.
* Created in 2018.
*/
package fr.abes.sudoqual.rule_engine.feature;

import fr.abes.sudoqual.util.json.JSONValidationReport;
import fr.abes.sudoqual.util.json.JSONValidator;
import org.apache.commons.lang3.StringUtils;

import fr.abes.sudoqual.rule_engine.exception.FeatureConfigurationException;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.util.ConfigurationProperties;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;


/**
 * A feature is a kind of data entry that can be attached to a {@link Reference} and
 * then used by {@link Criterion} and {@link Filter}.
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Feature {

	/**
	 * This methods will be called by SudoQual, it
	 * allows to configure the feature from the scenario properties file.
	 * @param properties it is a representation of the scenario properties file, so
	 * you can access to entry of this file and do some processing as load a proximity
	 * model contained in a specific file.
	 */
	default void configure(ConfigurationProperties properties) throws FeatureConfigurationException {
		// do nothing
	}

	/**
	 * Gets the feature key. This key is used to refer to this feature in data sets.
	 *
	 * @return the feature key
	 */
	default String getKey() {
		String name = this.getClass().getSimpleName();
		if (name.endsWith("Feature")) {
			name = name.substring(0, name.length() - "Feature".length());
		}
		name = StringUtils.uncapitalize(name);
		return name;
	}

	/**
	 * Checks if the provided value is what is expected.
	 * Default implementation is this.validate(value).isSuccess().
	 *
	 * @param value
	 * @return true if the provided value fulfill requirements. False, otherwise.
     * @deprecated use {@link #validate(Object)} instead.
	 */
	@Deprecated
	default boolean checkValue(Object value) {
        return this.validate(value).isSuccess();
	}

    /**
     * Check value against JsonSchema provided by {@link #getValidationSchema()}.
     * @param value
     * @return
     */
	default JSONValidationReport validate(Object value) {
	    return JSONValidator.validate(this.getValidationSchema(), value);
    }

    /**
     * Gets a JsonSchema on which input should be validate.
     * Schema must be in accordance with the draft-07 version.
     *
     * @return a JSONObject representing a JSONSchema.
     */
	default JSONObject getValidationSchema() {
        return new JSONObject("{'not':{'type':'null'}}");
    }

}
