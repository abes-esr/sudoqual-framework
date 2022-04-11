/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class LinkHeuristicException extends LinkingModuleException {

	private static final long serialVersionUID = 1748695478983093484L;

	public LinkHeuristicException(String message) {
		super(message);
	}

	public LinkHeuristicException(String message, Throwable cause) {
		super(message, cause);
	}

}
