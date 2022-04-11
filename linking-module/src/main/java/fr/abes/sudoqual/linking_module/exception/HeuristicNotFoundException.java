/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class HeuristicNotFoundException extends LinkingModuleException {

	private static final long serialVersionUID = -600017367672775271L;

	public HeuristicNotFoundException(String message) {
		super(message);
	}

	public HeuristicNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
