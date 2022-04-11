/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.util.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public abstract class SudoqualException extends Exception {

	private static final long serialVersionUID = 1914565352678471180L;

	public SudoqualException(String message) {
		super(message);
	}

	public SudoqualException(String message, Throwable cause) {
		super(message, cause);
	}

}
