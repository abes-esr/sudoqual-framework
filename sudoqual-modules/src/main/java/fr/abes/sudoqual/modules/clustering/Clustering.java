package fr.abes.sudoqual.modules.clustering;

import java.util.LinkedList;

import java.util.List;

import fr.abes.sudoqual.api.SudoqualModule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Partitionner;


public enum Clustering implements SudoqualModule {
    INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(Clustering.class);

	private static final String CLUSTERS = "clusters";
	private static final String COMPUTED_LINKS = "computedLinks";
	private static final String TYPE_KEY = "type";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
    private static final String CLUSTER_KEY = "cluster";
    private static final String SAME_AS_KEY = "sameAs";
	private static final String DIFF_FROM_KEY = "diffFrom";

	public JSONObject execute(JSONObject rcrcOutput) {
		JSONArray computedLinks = rcrcOutput.getJSONArray(COMPUTED_LINKS);
		JSONArray clusterLinks = rcrc2cluster(computedLinks);

		JSONObject output = new JSONObject();
		output.put(CLUSTERS, clusterLinks);
		return output;
	}

	private static JSONArray rcrc2cluster(JSONArray computedLinks) {
	    Partitionner<String> part = new Partitionner<>();
		List<JSONObject> toRemove = new LinkedList<>();

		int cpt = 0;
	    for(Object o : computedLinks) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject)o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case SAME_AS_KEY:
	    				++cpt;
	    				String source = link.getString(SOURCE_KEY);
		    			String target = link.getString(TARGET_KEY);
		    			part.add(source, target);
		    			break;
	    			case DIFF_FROM_KEY:
	    				toRemove.add(link);
	    				break;
	    			default:
	    				// do nothing
	    		}
	    	}
	    }
	    if(logger.isInfoEnabled()) {
	    	logger.info("nbLink: {}", cpt);
	    }

	    // remove clazz containing diffFrom link
	    for(JSONObject link : toRemove) {
	    	List<String> clazz = part.getClass(link.getString(SOURCE_KEY));
	    	if(logger.isInfoEnabled()) {
	 	    	logger.info("remove class due to diffFrom: {}", clazz);
	 	    }
	    	if(clazz != null) {
	    		part.removeClass(clazz);
	    	}
	    }

	    cpt = 0;

	    // constructs output
	    JSONArray outputLinks = new JSONArray();
	    int i = 0;
	    for(List<String> clazz: part) {
	    	++cpt;
	    	if(logger.isInfoEnabled()) {
	    		clazz.sort((x, y) -> x.compareTo(y));
	 	    	logger.info("class: {}", clazz);
	 	    }
	        String clusterURI = "_:cluster" + ++i;
	        for(String ref : clazz)
	        {
	        	outputLinks.put(createLink(ref, clusterURI));
	        }
	    }

	    if(logger.isInfoEnabled()) {
 	    	logger.info("nbClazz: {}", cpt);
 	    }

	    return outputLinks;
	}

	private static JSONObject createLink(String ref, String clusterURI) {
		JSONObject link = new JSONObject();
		link.put(TYPE_KEY, SAME_AS_KEY);
		link.put(SOURCE_KEY, ref);
		link.put(CLUSTER_KEY, clusterURI);
		return link;
	}
}
