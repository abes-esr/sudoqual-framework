/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine.predicate;

import fr.abes.sudoqual.rule_engine.exception.PredicateConfigurationException;
import fr.abes.sudoqual.util.ConfigurationProperties;

/**
 * Predicate is a super interface for {@link Criterion} and {@link Filter}.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Predicate {
	
	/**
	 * Gets the predicate key.
	 *
	 * @return the predicate key
	 */
	String getKey();
	
	/**
	 * Allows to configure the predicate
	 * @param properties
	 */
	default void configure(ConfigurationProperties properties) throws PredicateConfigurationException {
		// do nothing
	}

}
