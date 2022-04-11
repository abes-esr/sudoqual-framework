package fr.abes.sudoqual.linking_module.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

import fr.abes.sudoqual.linking_module.Config;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.json.JSONValidationReport;
import fr.abes.sudoqual.util.json.JSONValidator;

/**
 * This class is a utility class used for validation. <br>
 * 
 * @author Clément SIPIETER {@literal csipieter@6pi.fr}
 */
public final class JSONValidatorUtils {
	
	private static final JSONValidator inputValidator = getValidator("schema-input.json");
	private static final JSONValidator outputValidator = getValidator("schema-output.json");

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	private JSONValidatorUtils() {
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////

	public static JSONValidationReport validateInput(Object content)  {
		return inputValidator.validate(content);
	}

	public static JSONValidationReport validateOutput(Object content)  {
		return outputValidator.validate(content);
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE
	// /////////////////////////////////////////////////////////////////////////

	private static JSONValidator getValidator(String filename) {
		URL linkInputSchemaURL;
		JSONValidator validator = null;
		try {
			linkInputSchemaURL = ResourceUtils.getResource(LinkingModule.class, Config.RESOURCE_DIR, filename);
			try (InputStream stream = linkInputSchemaURL.openStream()) {
				JSONObject jsonSchemaObject = new JSONObject(new JSONTokener(new InputStreamReader(stream,
				                                                                                   Config.CHARSET)));
				validator = JSONValidator.instance(jsonSchemaObject);
			}
			
		} catch (ResourceNotFoundException | IOException e) {
			throw new Error("Error during validator creation. ", e);
		}
		return validator;
	}
}
