package fr.abes.sudoqual.util;

import java.net.URL;


/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class ResourceUtils {

	private ResourceUtils() {}

	// /////////////////////////////////////////////////////////////////////////
	//	METHODS
	// /////////////////////////////////////////////////////////////////////////

	public static URL getResource(Class<?> contextClass, String resourceDir, String resourceName) throws ResourceNotFoundException {
        URL url = contextClass.getResource(normalize(resourceDir, resourceName));
		if(url == null) {
			throw new ResourceNotFoundException("The resource " + resourceName + " was not found in " + resourceDir + ".");
		}
        return url;
	}

	public static URL getResource(Class<?> contextClass, Iterable<String> resourceDirs, String resourceName) throws ResourceNotFoundException {
		URL url = null;
		for(String dir : resourceDirs) {
			url = contextClass.getResource(normalize(dir, resourceName));
			if(url != null) {
				break;
			}
		}

		if(url == null) {
			throw new ResourceNotFoundException("The resource " + resourceName + " was not found in " + resourceDirs + ".");
		}
		return url;
	}

	private static String normalize(String dir, String filename) {
		if(dir == null || filename == null) {
			throw new IllegalArgumentException("dir and filename must be not null.");
		}
		StringBuilder sb = new StringBuilder(dir);
		if(!dir.endsWith("/") && !filename.startsWith("/")) {
			sb.append("/");
		} else if(dir.endsWith("/")  && filename.startsWith("/")) {
			filename = filename.substring(1);
		}
		sb.append(filename);
		return sb.toString();
	}
}
