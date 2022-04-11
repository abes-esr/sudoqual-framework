package fr.abes.sudoqual.modules.clusterRaOverlap;

import java.util.HashMap;
import java.util.Map;

import fr.abes.sudoqual.api.SudoqualModule;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum ClusterRaOverlap implements SudoqualModule {
    INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(ClusterRaOverlap.class);
	private static final String LINKS = "links";
	private static final String CLUSTERS = "clusters";
	private static final String TYPE_KEY = "type";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
	private static final String CLUSTER_KEY = "cluster";
	private static final String SAME_AS_KEY = "sameAs";
	private static final String WHY_KEY = "why";
	private static final String SUGGESTED_SAME_AS_KEY = "suggestedSameAs";
	private static int DEFAULT_THRESHOLD_SAME_AS = 90;
	private static int DEFAULT_THRESHOLD_SUGGESTED_SAME_AS = 60;

	public JSONObject execute(JSONObject input) {
		JSONArray links = input.getJSONArray(LINKS);
		JSONArray clusters = input.getJSONArray(CLUSTERS);
		JSONArray computedLinks = process(links, clusters, DEFAULT_THRESHOLD_SAME_AS, DEFAULT_THRESHOLD_SUGGESTED_SAME_AS);

		JSONObject output = new JSONObject();
		output.put("computedLinks", computedLinks);
		return output;
	}

    JSONObject execute(JSONObject input, int thresholdSameAs, int thresholdSuggestedSameAs) {
        DEFAULT_THRESHOLD_SAME_AS = thresholdSameAs;
        DEFAULT_THRESHOLD_SUGGESTED_SAME_AS = thresholdSuggestedSameAs;
        JSONArray links = input.getJSONArray(LINKS);
        JSONArray clusters = input.getJSONArray(CLUSTERS);
        JSONArray computedLinks = process(links, clusters, thresholdSameAs, thresholdSuggestedSameAs);

        JSONObject output = new JSONObject();
        output.put("computedLinks", computedLinks);
        return output;
    }

    private static JSONArray process(JSONArray links, JSONArray clusters, int thresholdSameAs, int thresholdSuggestedSameAs) {
		// constructs output
	    JSONArray outputLinks = new JSONArray();

	    HashMap<String, String> mapCluster = new HashMap<String, String>();
		HashMap<String, HashMap<String, JSONObject>> mapRA = new HashMap<String, HashMap<String, JSONObject>>();
		HashMap<String, JSONObject> mapRAtotalRc = new HashMap<String, JSONObject>();

		for(Object o : clusters) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject) o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case SAME_AS_KEY:
	    				String source = link.getString(SOURCE_KEY);
		    			String target = link.getString(CLUSTER_KEY);
	    				mapCluster.put(source,target);
	    				break;
	    			default:
	    				// do nothing
	    		}
	    	}
    	}
	    for (Map.Entry<String, String> entry : mapCluster.entrySet()) {
	    	logger.debug("MapCluster : Key = " + entry.getKey() + ", Value = " + entry.getValue());
    	}

	    for(Object o : links) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject) o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case SAME_AS_KEY:
	    				String source = link.getString(SOURCE_KEY);//RC
		    			String target = link.getString(TARGET_KEY);//RA

		    			String cluster = mapCluster.get(source);//Cluster
		    			if (cluster == null) {
		    				cluster = "noCluster";
		    			}

		    			if (mapRA.get(target)!=null && mapRAtotalRc.get(target)!=null) {
		    				mapRAtotalRc.get(target).increment("totalNbRc");

		    				if (mapRA.get(target).get(cluster)!=null)
		    					mapRA.get(target).get(cluster).increment("nbRc"); //If RA and cluster present, then nbRc+1
		    				else {
		    					mapRA.get(target).put(cluster,new JSONObject().increment("nbRc")); //else, creation of cluster and nbRc=1
		    				}
		    			} else {
		    				mapRAtotalRc.put(target,new JSONObject().increment("totalNbRc"));

		    				HashMap<String, JSONObject> h = new <String,JSONObject>HashMap();
	    					h.put(cluster,new JSONObject().increment("nbRc"));
	    					mapRA.put(target,h); //else, creation of Ra, cluster and nbRc=1
		    			}
		    			break;
	    			default:
	    				// do nothing
	    		}
	    	}
	    }

	    //For each RA
	    for (Map.Entry<String, HashMap<String,JSONObject>> ra : mapRA.entrySet()) {
	    	logger.debug("RA : " + ra.getKey() );
	    	int totalNbRc = mapRAtotalRc.get(ra.getKey()).getInt("totalNbRc");

	    	//For each RA cluster : get RC number, to count the total number of RC in this RA
	    	/*for (Map.Entry<String,JSONObject> rcCluster : ra.getValue().entrySet()) {
	    		//totalNbRc+=rcCluster.getValue().getInt("nbRc");
	    		logger.debug("Cluster : " + rcCluster.getKey() + " "+ rcCluster.getValue());
	    	}*/
	    	logger.debug("Total number of RC in this RA : "+totalNbRc);

	    	for (Map.Entry<String,JSONObject> rcCluster : ra.getValue().entrySet()) {
	    		int pourcentage = (rcCluster.getValue().getInt("nbRc")*100) / totalNbRc;
	    		logger.debug("Percentage of cluster " + rcCluster.getKey()+ " : " +pourcentage);
	    		//If threshold of the percentage is passed and is really a cluster (not noCluster)
	    		if (rcCluster.getKey().compareTo("noCluster")!=0) {
		    		if (pourcentage>= thresholdSameAs) {
		    			outputLinks.put(createLink(rcCluster.getKey(), ra.getKey(), SAME_AS_KEY));
		    		}
		    		else if (thresholdSuggestedSameAs > 0 && pourcentage>= thresholdSuggestedSameAs) {
		    			outputLinks.put(createLink(rcCluster.getKey(), ra.getKey(), SUGGESTED_SAME_AS_KEY));
		    		}
	    		}
	    	}
    	}

	    return outputLinks;
	}

	private static JSONObject createLink(String source, String target, String type) {
		JSONObject link = new JSONObject();
		link.put(TYPE_KEY, type);
		link.put(CLUSTER_KEY, source);
		link.put(TARGET_KEY, target);
		link.put(WHY_KEY, new JSONObject("{\"clusterRaOverlap\": {}}"));
		return link;
	}
}
