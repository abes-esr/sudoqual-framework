package fr.abes.sudoqual.linking_module.util;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class OutputComparator {
	
	private static final String TYPE_KEY = "type";
	private static final String STEP_KEY = "step";
	private static final String TARGET_KEY = "target";
	private static final String SOURCE_KEY = "source";
	private static final String EXPECTED_LINKS_KEY = "expectedLinks";
	private static final String COMPUTED_LINKS_KEY = "computedLinks";

	private OutputComparator() {
		// default constructor
	}
	
	public static String cmp(JSONObject output, JSONObject outputExpected) {
		assert output.has(COMPUTED_LINKS_KEY);
		assert outputExpected.has(EXPECTED_LINKS_KEY);
		
		JSONArray links = output.optJSONArray(COMPUTED_LINKS_KEY);
		JSONArray linksExpected = outputExpected.optJSONArray(EXPECTED_LINKS_KEY);
		if(linksExpected == null) {
			linksExpected = outputExpected.optJSONArray(COMPUTED_LINKS_KEY);
		}
		
		return cmp(links, linksExpected);
	}
	
	public static String cmp(JSONArray actualLinks, JSONArray expectedLinks) {
		StringBuilder sb = new StringBuilder();

		if(actualLinks == null || expectedLinks == null) {
			if(actualLinks == null) {
				sb.append("no ").append(COMPUTED_LINKS_KEY).append(" key found in actual json.\n");
			}
			if(expectedLinks == null) {
				sb.append("no ").append(EXPECTED_LINKS_KEY).append(" or ").append(COMPUTED_LINKS_KEY)
				.append(" keys found in expected json.\n");
			}
			return sb.toString();
		}
		
		for(Object searchedLinkObj : expectedLinks) {
			JSONObject searchedLink = (JSONObject) searchedLinkObj;
			boolean found = false;
			for(Object lObj: actualLinks) {
				JSONObject l = (JSONObject) lObj;
				if(Objects.equals(l.getString(SOURCE_KEY), searchedLink.getString(SOURCE_KEY))
						&& Objects.equals(l.getString(TARGET_KEY), searchedLink.getString(TARGET_KEY))
						&& Objects.equals(l.getString(TYPE_KEY), searchedLink.getString(TYPE_KEY))) {
					if(!Objects.equals(l.getInt(STEP_KEY), searchedLink.getInt(STEP_KEY))) {
    					sb.append(searchedLink.getString(SOURCE_KEY))
    					.append(' ')
    					.append(searchedLink.getString(TYPE_KEY))
    					.append(' ')
    					.append(searchedLink.getString(TARGET_KEY))
    					.append(' ')
    					.append(" - steps does not match: expected ")
    					.append(searchedLink.getInt(STEP_KEY))
    					.append(", found ")
    					.append(l.getInt(STEP_KEY))
    					.append('\n');
					}
					found = true;
					break;
				}
			}
			if(!found) {
    			sb.append("expected link ")
    			.append(searchedLink.getString(SOURCE_KEY))
    			.append(' ')
    			.append(searchedLink.getString(TYPE_KEY))
    			.append(' ')
    			.append(searchedLink.getString(TARGET_KEY))
    			.append(" (step  ")
    			.append(searchedLink.getInt(STEP_KEY))
    			.append(")  not found, \n");
			}
		}
		for(Object searchedLinkObj : actualLinks) {
			JSONObject searchedLink = (JSONObject) searchedLinkObj;
			boolean found = false;
			for(Object lObj: expectedLinks) {
				JSONObject l = (JSONObject) lObj;
				if(Objects.equals(l.getString(SOURCE_KEY), searchedLink.getString(SOURCE_KEY))
						&& Objects.equals(l.getString(TARGET_KEY), searchedLink.getString(TARGET_KEY))
						&& Objects.equals(l.getString(TYPE_KEY), searchedLink.getString(TYPE_KEY))) {
					found = true;
					break;
				}
			}
			if(!found) {
    			sb.append("computed link ")
    			.append(searchedLink.getString(SOURCE_KEY))
    			.append(' ')
    			.append(searchedLink.getString(TYPE_KEY))
    			.append(' ')
    			.append(searchedLink.getString(TARGET_KEY))
    			.append(" (step  ")
    			.append(searchedLink.getInt(STEP_KEY))
    			.append(") not expected, \n");
			}

		}
		return sb.toString();
	}
	
}
