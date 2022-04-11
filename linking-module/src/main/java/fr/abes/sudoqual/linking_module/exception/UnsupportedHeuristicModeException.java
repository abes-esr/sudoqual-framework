/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class UnsupportedHeuristicModeException extends LinkingModuleException {

	private static final long serialVersionUID = 2801273183149430649L;

	public UnsupportedHeuristicModeException(String message) {
		super(message);
	}

	public UnsupportedHeuristicModeException(String message, Throwable cause) {
		super(message, cause);
	}
}
