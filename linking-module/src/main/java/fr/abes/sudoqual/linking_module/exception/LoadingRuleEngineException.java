/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class LoadingRuleEngineException extends LinkingModuleException {

	private static final long serialVersionUID = 459746020735767156L;

	public LoadingRuleEngineException(Throwable cause) {
		super("Error when loading rule engine", cause);
	}
	
	public LoadingRuleEngineException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LoadingRuleEngineException(String message) {
		super(message);
	}
}
