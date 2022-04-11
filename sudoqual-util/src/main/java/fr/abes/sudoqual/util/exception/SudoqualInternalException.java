/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.util.exception;

/**
 * Thrown to indicate that an unexpected internal behavior has occurred in Sudoqual.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class SudoqualInternalException extends SudoqualException {

	private static final long serialVersionUID = 2656186099563429788L;

	public SudoqualInternalException(String message) {
		super(message);
	}

	public SudoqualInternalException(String message, Throwable cause) {
		super(message, cause);
	}

}
