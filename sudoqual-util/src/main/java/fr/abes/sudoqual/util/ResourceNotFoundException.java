/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.util;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 92354004344439222L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
