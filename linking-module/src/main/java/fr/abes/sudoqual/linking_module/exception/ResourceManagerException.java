/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.exception;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ResourceManagerException extends LinkingModuleException {

	private static final long serialVersionUID = -3072360311356774984L;

	public ResourceManagerException(String message) {
		super(message);
	}

	public ResourceManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
