/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.abes.sudoqual.linking_module.exception.ResourceManagerException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface ResourceManager {
	
	static ResourceManager instance() {
		return ResourceManagerImpl.INSTANCE;
	}
		
	Map<String, Integer> loadMap(Set<String> lookupPaths, String filename) throws ResourceManagerException;
	
	List<String> loadList(Set<String> lookupPaths, String filename) throws ResourceManagerException;

}
