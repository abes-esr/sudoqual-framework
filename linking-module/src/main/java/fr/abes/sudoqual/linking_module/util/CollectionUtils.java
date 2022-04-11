package fr.abes.sudoqual.linking_module.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class CollectionUtils {

	private CollectionUtils() {}
	
	public static <T> Set<T> setFrom(T... elements) {
		Set<T> set = new HashSet<>();
		for(T e : elements) {
			set.add(e);
		}
		return set;
	}
}
