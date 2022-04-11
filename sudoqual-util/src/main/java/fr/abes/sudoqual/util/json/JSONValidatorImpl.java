package fr.abes.sudoqual.util.json;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

/**
 * This class is a utility class used for JSONSchema validation. <br>
 * 
 * @author Clément SIPIETER {@literal csipieter@6pi.fr}
 */
class JSONValidatorImpl implements JSONValidator {

	//private static final Logger LOGGER = LoggerFactory.getLogger(JSONValidator.class);
	
	private final Schema schema;
	
	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public JSONValidatorImpl(JSONObject schema) {
			this.schema = SchemaLoader.load(schema);
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see fr.abes.sudoqual.util.JSONValidator#validate(java.lang.Object)
	 */
	@Override
	public JSONValidationReport validate(Object content)  {
		try {
			this.schema.validate(content);
		} catch (ValidationException e) {
			return new JSONValidationReportImpl(e);
		}
		return JSONValidationReport.SUCCESS_REPORT;
	}

}
