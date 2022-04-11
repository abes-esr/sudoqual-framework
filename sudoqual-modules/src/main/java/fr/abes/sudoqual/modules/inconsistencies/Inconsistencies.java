package fr.abes.sudoqual.modules.inconsistencies;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.abes.sudoqual.api.SudoqualModule;
import org.jgrapht.*;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Partitionner;


public enum Inconsistencies implements SudoqualModule {
    INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(Inconsistencies.class);
	private static final String COMPUTED_LINKS = "computedLinks";
	private static final String INCONSTISTENCIES = "inconsistencies";
	private static final String TYPE_KEY = "type";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
	private static final String CLUSTER_KEY = "cluster";
	private static final String SAME_AS_KEY = "sameAs";
	private static final String SUGGESTED_SAME_AS_KEY = "suggestedSameAs";
	private static final String DIFF_FROM_KEY = "diffFrom";

	public JSONObject execute(JSONObject input) {
		JSONArray computedLinks = input.getJSONArray(COMPUTED_LINKS);
		JSONObject outputLinks = incoherentLinks(computedLinks);

		return outputLinks;
	}

	static JSONObject incoherentLinks(JSONArray computedLinks) {
		Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);

		int cpt = 0;
		//Create a graph of computedLinks
	    for(Object o : computedLinks) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject)o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case SAME_AS_KEY:
	    			case DIFF_FROM_KEY:
	    				++cpt;
	    				String source = link.optString(SOURCE_KEY);
		    			String target = link.optString(TARGET_KEY);
		    			String cluster = link.optString(CLUSTER_KEY);

		    			if (!source.isEmpty() && !target.isEmpty()) {
		    				graph.addVertex(source);
		    				graph.addVertex(target);
		    				graph.addEdge(source, target);
		    			}
		    			else if (!source.isEmpty() && !cluster.isEmpty()) {
		    				graph.addVertex(source);
		    				graph.addVertex(cluster);
		    				graph.addEdge(source, cluster);

		    			}
		    			else if (!target.isEmpty() && !cluster.isEmpty()) {
		    				graph.addVertex(cluster);
		    				graph.addVertex(target);
		    				graph.addEdge(cluster, target);
		    			}
		    			// start hack bug JGrapht 1.3.1
		    			// biconnected graph on (1,2), (2,3), (3,1), (3,4)
		    			// then biconnected components found is (1,2,3,4) instead of (1,2,3) and (3,4)
		    			int idx = 0;
		    			String prefix = "mustNotExist/mustBeFiltered:";
		    			LinkedList<String> list = new LinkedList<String>(graph.vertexSet());
		    			for(String s : list) {
		    				String v = prefix + ++idx;
		    				graph.addVertex(v);
		    				graph.addEdge(v, s);
		    			}
		    			// end hack
		    			break;
	    			default:
	    				// do nothing
	    		}
	    	}
	    }
	    if(logger.isInfoEnabled()) {
	    	logger.info("nbLink: {}", cpt);
	    }

	    BiconnectivityInspector<String, DefaultEdge> biconnectivityInspector = new BiconnectivityInspector<>(graph);
        Set<Graph<String, DefaultEdge>> blocks = biconnectivityInspector.getBlocks();

	    List<Graph<String, DefaultEdge>> incoherentClasses = new ArrayList<>();
	    Map<Graph<String, DefaultEdge>, JSONArray>  incoherentLinksTmp = new HashMap<>();
	    JSONArray newComputedLinks = new JSONArray();
	    //Detect incoherent links
	    for(Object o : computedLinks) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject)o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case DIFF_FROM_KEY:
	    				String source = link.optString(SOURCE_KEY);
		    			String target = link.optString(TARGET_KEY);
		    			String cluster = link.optString(CLUSTER_KEY);

		    			String theSource = null;
	    				String theTarget = null;
		    			if (!source.isEmpty() && !target.isEmpty()) {
		    				theSource = source;
		    				theTarget = target;
		    			}
		    			else if (!source.isEmpty() && !cluster.isEmpty()) {
		    				theSource = source;
		    				theTarget = cluster;
		    			}
		    			else if (!target.isEmpty() && !cluster.isEmpty()) {
		    				theSource = cluster;
		    				theTarget = target;
		    			}

		    			for(Graph<String, DefaultEdge> g : blocks) {
		    				if(g.containsVertex(theSource) && g.containsVertex(theTarget)) {
		    					incoherentClasses.add(g);
		    				}
		    			}
	    			default:
	    				// do nothing
	    		}
	    	}
	    }

		for(Object p : computedLinks) {
	    	if(p instanceof JSONObject) {
	    		JSONObject computedLink = (JSONObject)p;
	    		String source = computedLink.optString(SOURCE_KEY);
    			String target = computedLink.optString(TARGET_KEY);
    			String cluster = computedLink.optString(CLUSTER_KEY);
    			String type = computedLink.optString(TYPE_KEY);

    			String theSource = null;
				String theTarget = null;
    			if (!source.isEmpty() && !target.isEmpty()) {
    				theSource = source;
    				theTarget = target;
    			}
    			else if (!source.isEmpty() && !cluster.isEmpty()) {
    				theSource = source;
    				theTarget = cluster;
    			}
    			else if (!target.isEmpty() && !cluster.isEmpty()) {
    				theSource = cluster;
    				theTarget = target;
    			}

    			Graph<String, DefaultEdge> gg = null;
    			for(Graph<String, DefaultEdge> g : incoherentClasses) {
    				if(g.containsVertex(theSource) && g.containsVertex(theTarget)) {
    					gg = g;
    					break;
    				}
    			}
    			if(gg != null) {
    				JSONArray jsonArray = incoherentLinksTmp.get(gg);
					if(jsonArray == null) {
						jsonArray = new JSONArray();
						incoherentLinksTmp.put(gg, jsonArray);
					}
					jsonArray.put(computedLink);

    				if(SAME_AS_KEY.equals(type)) {
    					JSONObject newLink2 = new JSONObject(computedLink.toString());
	    				newLink2.put(TYPE_KEY, SUGGESTED_SAME_AS_KEY);
	    				newComputedLinks.put(newLink2);
    				} else if(!DIFF_FROM_KEY.equals(type)) {
        				newComputedLinks.put(computedLink);
    				} else {
    					// do nothing = filter diffFrom
    				}
    			} else {
    				newComputedLinks.put(computedLink);
    			}

	    	}
		}



	    JSONObject outputLinks = new JSONObject();
	    outputLinks.put(COMPUTED_LINKS, newComputedLinks);
	    outputLinks.put(INCONSTISTENCIES, incoherentLinksTmp.values());


		 if(logger.isInfoEnabled()) {
		  //  	logger.info("new nb links: {} // {} ", newComputedLinks.length(), outputLinks.toString(1));
		    }


	    return outputLinks;
	}
}
