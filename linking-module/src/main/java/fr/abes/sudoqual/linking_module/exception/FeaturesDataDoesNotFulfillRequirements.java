/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class FeaturesDataDoesNotFulfillRequirements extends LinkingModuleException {

	private static final long serialVersionUID = 6954574689900409754L;
	
	public FeaturesDataDoesNotFulfillRequirements() {
		super("Features data provided in JSON input doesn't fulfill requirements.");
	}
	
	public FeaturesDataDoesNotFulfillRequirements(String msg) {
		super("Features data provided in JSON input doesn't fulfill requirements: " + msg);
	}

}
