package fr.abes.sudoqual.linking_module.util;

import java.util.Properties;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class PropertiesUtils {

	private PropertiesUtils() {
	}

	public static boolean readBoolean(Properties prop, String key) throws PropertiesException {
		String stringValue = prop.getProperty(key);
		if ("true".equalsIgnoreCase(stringValue)) {
			return true;
		} else if ("false".equalsIgnoreCase(stringValue)) {
			return false;
		}
		throw new PropertiesException("The property value of " + key + " is not a boolean : " + stringValue);
	}
	
	public static boolean readBoolean(Properties prop, String key, boolean defaultValue) throws PropertiesException {
		if(prop.containsKey(key)) {
			return readBoolean(prop, key);
		} else {
			return defaultValue;
		}
	}

	public static int readInteger(Properties prop, String key) throws PropertiesException {
		String stringValue = prop.getProperty(key);
		try {
			return Integer.valueOf(stringValue);
		} catch (NumberFormatException e) {
			throw new PropertiesException("The property value of " + key + " is not an integer : " + stringValue, e);
		}
	}
	
	public static int readInteger(Properties prop, String key, Integer defaultValue) throws PropertiesException {
		if(prop.contains(key)) {
			return readInteger(prop, key);
		} else {
			return defaultValue;
		}
	}

}
