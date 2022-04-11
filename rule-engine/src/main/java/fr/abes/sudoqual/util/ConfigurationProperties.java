/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.util;

import java.util.Set;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface ConfigurationProperties {
	
	Set<String> getLookupPaths();
	
	/**
	 * Get the value associated to the specified key. This method returns null if the 
	 * property is not found.
	 * @param key the property name
	 * @return the value associated to the specified key.
	 */
	String get(String key);
	
	/**
	 * Get the value associated to the specified key. This method returns the
	 * specified defaultValue if the property is not found.
	 * @param key the property name
	 * @param defaultValue the default value to return if the property is not found.
	 * @return the value associated to the specified key.
	 */
	String get(String key, String defaultValue);
	
	int getInteger(String key) throws ConfigurationPropertiesException;
	
	int getInteger(String key, Integer defaultValue) throws ConfigurationPropertiesException;
	
	boolean getBoolean(String key) throws ConfigurationPropertiesException;
	
	boolean getBoolean(String key, Boolean defaultValue) throws ConfigurationPropertiesException;

}
