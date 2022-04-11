/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class PredicateConfigurationException extends RuleEngineException {

	private static final long serialVersionUID = 656682302172326276L;

	public PredicateConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public PredicateConfigurationException(String msg) {
		super(msg);
	}

}
