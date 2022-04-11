/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class BusinessClassException extends LinkingModuleException {

	private static final long serialVersionUID = -1605533967917172261L;

	public BusinessClassException(String message) {
		super(message);
	}

	public BusinessClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
