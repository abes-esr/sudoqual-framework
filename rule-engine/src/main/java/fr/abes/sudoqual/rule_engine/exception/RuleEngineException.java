/*
* This file is part of SudoQual project.
* Created in 2018-08 by Clément SIPIETER.
*/
package fr.abes.sudoqual.rule_engine.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class RuleEngineException extends Exception {

	private static final long serialVersionUID = 7125515492935508743L;
	
	public RuleEngineException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RuleEngineException(String msg) {
		super(msg);
	}

}
