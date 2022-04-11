/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import fr.abes.sudoqual.linking_module.util.PropertiesException;
import fr.abes.sudoqual.linking_module.util.PropertiesUtils;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class Config {

	//private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private static final String CONFIG_FILE_NAME = "linking-module.properties";

	public static Charset CHARSET = StandardCharsets.UTF_8;
	public static String SCENARIO_DIR = "scenarios/";
	public static String RESOURCE_DIR = "/fr/abes/sudoqual/linking_module/resources/";
	public static String VERSION = "undefined";

	static {
		URL configFile;
		try {
			configFile = ResourceUtils.getResource(Config.class, RESOURCE_DIR, CONFIG_FILE_NAME);
		} catch (ResourceNotFoundException e) {
			throw new Error("Unable to find resource directory.", e);
		}

		try (InputStream stream = configFile.openStream()) {
			Properties prop = new Properties();
			prop.load(stream);
			VERSION = prop.getProperty("project.version", VERSION);
			SCENARIO_DIR = prop.getProperty("scenarioDirectory", SCENARIO_DIR);
		} catch (IOException e) {
			throw new Error("An error occured when reading configuration file: " + configFile.getPath(), e);
		}
	}

	private Config() {
	}

}
