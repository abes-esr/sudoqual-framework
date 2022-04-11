/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ScenarioException extends LinkingModuleException {

	private static final long serialVersionUID = -1000503501736526989L;

	public ScenarioException(String message) {
		super(message);
	}

	public ScenarioException(String message, Throwable cause) {
		super(message, cause);
	}

}
