/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The Reference interface is an important piece of SudoQual as
 * it allows to refer to an entity from the real world and
 * its data set. It is between references that SudoQual will 
 * try to create sameAs or diffFrom links.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Reference {

	/**
	 * Gets the integer id of this reference.
	 *
	 * @return the integer id
	 */
	default int getId() {
		return new HashCodeBuilder(21, 73).
			       append(this.getName()).toHashCode();
	}
	
	/**
	 * Gets the name (URI) of this reference.
	 * 
	 * @return the string identifier of this reference. 
	 */
	String getName();
	
	/**
	 * Compute a reference id from a reference name.
	 * Must be used to implements Reference.getId().
	 * @param name
	 * @return the corresponding reference id
	 */
	static int nameToId(String name) {
		return new HashCodeBuilder(21, 73).
			       append(name).toHashCode();
	}

}
