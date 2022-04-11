/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.exception;

/**
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class FeatureManagerException extends RuntimeException {

	private static final long serialVersionUID = 3633666413622707664L;

	public FeatureManagerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public FeatureManagerException(String msg) {
		super(msg);
	}

}
