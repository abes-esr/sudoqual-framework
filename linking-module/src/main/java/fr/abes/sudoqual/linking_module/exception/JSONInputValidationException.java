/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class JSONInputValidationException extends LinkingModuleException {

	private static final long serialVersionUID = 3300040602028602327L;

	public JSONInputValidationException(String message) {
		super(message);
	}

	public JSONInputValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
