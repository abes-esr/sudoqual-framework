/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.modules.diagnostic.exception;

import fr.abes.sudoqual.api.exception.SudoqualModuleException;

/**
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class DiagnosticianException extends SudoqualModuleException {

	private static final long serialVersionUID = -7373492748981067214L;


	public DiagnosticianException(String message) {
		super(message);
	}


	public DiagnosticianException(String message, Throwable cause) {
		super(message, cause);
	}

}
