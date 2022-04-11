/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.modules.diagnostic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class Config {

	//private static final Logger logger = LoggerFactory.getLogger(Config.class);
	//private static final String CONFIG_FILE_NAME = "linking-module.properties";

	public static Charset CHARSET = StandardCharsets.UTF_8;
	public static String RESOURCE_DIR = "/fr/abes/sudoqual/modules/resources/";
	public static String VERSION = "undefined";



	private Config() {
	}

}
