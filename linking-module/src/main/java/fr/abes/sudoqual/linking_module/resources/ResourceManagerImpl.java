/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Charsets;

import fr.abes.sudoqual.linking_module.exception.ResourceManagerException;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
enum ResourceManagerImpl implements ResourceManager {
	INSTANCE;

	@Override
	public Map<String, Integer> loadMap(Set<String> lookupPaths, String filename) throws ResourceManagerException {
		try {
			URL dicoURL = ResourceUtils.getResource(getClass(), lookupPaths, filename);
			return readColonSeperatedEntries(dicoURL);
		} catch (IOException | ResourceNotFoundException | URISyntaxException e) {
			throw new ResourceManagerException("An error occured when loading following dictionary: " + filename, e);
		}
	}
	
	@Override
	public List<String> loadList(Set<String> lookupPaths, String filename) throws ResourceManagerException {
		try {
			URL resource = ResourceUtils.getResource(getClass(), lookupPaths, filename);
			return readCarriageReturnSeperatedEntries(resource);
		} catch (IOException | ResourceNotFoundException | URISyntaxException e) {
			throw new ResourceManagerException("An error occured when loading following dictionary: " + filename, e);
		}
	}
	
	private static Map<String, Integer> readColonSeperatedEntries(URL resource) throws IOException, URISyntaxException {
		try (Stream<String> stream = new BufferedReader(new InputStreamReader(resource.openStream(), Charsets.UTF_8)).lines()) {
			return stream.map(Util::process).filter(array -> array.length >= 2)
					.collect(Collectors.toMap(Util::collectKey, Util::collectValue));
		}
	}

	private static List<String> readCarriageReturnSeperatedEntries(URL resource) throws IOException, URISyntaxException {
		try (Stream<String> stream = new BufferedReader(new InputStreamReader(resource.openStream(), Charsets.UTF_8)).lines()) {
			return stream.filter(line -> !line.isBlank()).collect(Collectors.toList());
		}
	}

	private static class Util {
		private static int lastIndexOf(String s, char c, int startIdx) {
			for (int i = startIdx; i >= 0; --i) {
				if (s.charAt(i) == c) {
					return i;
				}
			}
			return -1;
		}

		static String[] process(String s) {
			// we start with an offset of 1 (at s.length() - 2) because we want at least one
			// char after the colon "a:1"
			int i = lastIndexOf(s, ':', s.length() - 2);
			if (i < 1) {
				return new String[0];
			} else {
				return new String[] { s.substring(0, i), s.substring(i + 1) };
			}
		}

		static String collectKey(String[] array) {
			return array[0];
		}

		static int collectValue(String[] array) {
			return Integer.parseInt(array[1]);
		}
	}

}
