package fr.abes.sudoqual.linking_module.util;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class PropertiesException extends Exception {
	
	private static final long serialVersionUID = 8971969542497737225L;

	public PropertiesException(String message) {
		super(message);
	}

	public PropertiesException(String message, Throwable cause) {
		super(message, cause);
	}

}
