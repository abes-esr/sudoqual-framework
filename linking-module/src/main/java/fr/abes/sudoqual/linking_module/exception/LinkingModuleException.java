/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

import fr.abes.sudoqual.api.exception.SudoqualModuleException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class LinkingModuleException extends SudoqualModuleException {

	private static final long serialVersionUID = -1418703415745673837L;

	public LinkingModuleException(String message) {
		super(message);
	}

	public LinkingModuleException(String message, Throwable cause) {
		super(message, cause);
	}

}
