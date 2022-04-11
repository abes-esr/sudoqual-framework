/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.exception;

/**
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class FeatureConfigurationException extends RuleEngineException {

	private static final long serialVersionUID = 829722954687669707L;

	public FeatureConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public FeatureConfigurationException(String msg) {
		super(msg);
	}

}
