package fr.abes.sudoqual.util.json;

import org.json.JSONObject;

public interface JSONValidator {

	static JSONValidator instance(JSONObject schema) {
		return new JSONValidatorImpl(schema);
	}

	// /////////////////////////////////////////////////////////////////////////
	//
	// /////////////////////////////////////////////////////////////////////////


	/**
	 * This private method is called by every public validation method of this
	 * class.<br>
	 * Given a JSON object or array, checks if it is valid against the
	 * schema and returns a ProcessingReport.<br>
	 *
	 * @param content
	 * @param schema
	 * @return
	 */
	JSONValidationReport validate(Object content);

	static JSONValidationReport validate(JSONObject schema, Object content) {
        return new JSONValidatorImpl(schema).validate(content);
    }

}
