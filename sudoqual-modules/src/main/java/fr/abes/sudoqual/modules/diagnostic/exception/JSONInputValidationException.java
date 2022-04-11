/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.modules.diagnostic.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class JSONInputValidationException extends DiagnosticianException {

	private static final long serialVersionUID = 4800109957145543387L;

	public JSONInputValidationException(String message) {
		super(message);
	}

	public JSONInputValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
