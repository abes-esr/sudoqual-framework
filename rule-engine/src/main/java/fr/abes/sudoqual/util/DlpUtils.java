package fr.abes.sudoqual.util;

public class DlpUtils {
	
	/**
	 * Remove "not_" prefix if there is, otherwise returns name itself.
	 * @param name
	 * @return
	 */
	public static String removeNotFromPredicateName(String name) {
		if (name.startsWith("not_")) {
			return name.substring(4);
		} else {
			return name;
		}
	}
}
