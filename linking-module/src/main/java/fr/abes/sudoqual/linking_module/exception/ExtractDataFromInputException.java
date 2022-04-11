/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ExtractDataFromInputException extends LinkingModuleException {

	private static final long serialVersionUID = -5237158692009396439L;

	public ExtractDataFromInputException(String message) {
		super(message);
	}

	public ExtractDataFromInputException(String message, Throwable cause) {
		super(message, cause);
	}

}
