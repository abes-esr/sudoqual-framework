/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ScenarioNotFoundException extends ScenarioException {

	private static final long serialVersionUID = 3731262831153219689L;

	public ScenarioNotFoundException(String scenarioName) {
		this(scenarioName, null);
	}
	
	public ScenarioNotFoundException(String scenarioName, Throwable cause) {
		super("The scenario " + scenarioName + " was not found.", cause);
	}
}
