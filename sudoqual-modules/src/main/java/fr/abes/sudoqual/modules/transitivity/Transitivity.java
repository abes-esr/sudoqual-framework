package fr.abes.sudoqual.modules.transitivity;


import java.util.List;
import java.util.stream.Collectors;

import fr.abes.sudoqual.api.SudoqualModule;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Partitionner;


public enum Transitivity implements SudoqualModule {
    INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(Transitivity.class);
	private static final String LINKS = "links";
	private static final String LOOKS_FOR_LINKS = "looksForLinks";
	private static final String TYPE_KEY = "type";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
	private static final String SOURCES_KEY = "sources";
	private static final String TARGETS_KEY = "targets";
	private static final String CLUSTERS_KEY = "clusters";
	private static final String CLUSTER_KEY = "cluster";
	private static final String SAME_AS_KEY = "sameAs";
	private static final String FROM_KEY = "from";
	private static final String TO_KEY = "to";

	public JSONObject execute(JSONObject input) {
		JSONArray links = input.getJSONArray(LINKS);
		JSONArray looksForLinks = input.getJSONArray(LOOKS_FOR_LINKS);
		JSONArray sourceSet = input.optJSONArray(SOURCES_KEY);
		JSONArray targetSet = input.optJSONArray(TARGETS_KEY);
		JSONArray clusterSet = input.optJSONArray(CLUSTERS_KEY);
        JSONObject computedLinks = linksTransitivity(
            links,
            looksForLinks,
            (sourceSet != null)? sourceSet : new JSONArray(),
            (targetSet != null)? targetSet : new JSONArray(),
            (clusterSet != null)? clusterSet : new JSONArray()
        );

		return computedLinks;
	}


	private static JSONObject linksTransitivity(JSONArray links, JSONArray looksForLinks, JSONArray sourceSet, JSONArray targetSet, JSONArray clusterSet) {
		Partitionner<String> part = new Partitionner<>();

		int cpt = 0;
	    for(Object o : links) {
	    	if(o instanceof JSONObject) {
	    		JSONObject link = (JSONObject)o;
	    		switch(link.optString(TYPE_KEY)) {
	    			case SAME_AS_KEY:
	    				++cpt;
	    				String source = link.optString(SOURCE_KEY);
		    			String target = link.optString(TARGET_KEY);
		    			String cluster = link.optString(CLUSTER_KEY);

		    			if (!source.isEmpty() && !target.isEmpty()) {
		    				String String1 = new String(source);
		    				String String2 = new String(target);
		    				part.add(String1, String2);
		    			}
		    			else if (!source.isEmpty() && !cluster.isEmpty()) {
		    				String String1 = new String(source);
		    				String String2 = new String(cluster);
		    				part.add(String1, String2);
		    			}
		    			else if (!target.isEmpty() && !cluster.isEmpty()) {
		    				String String1 = new String(target);
		    				String String2 = new String(cluster);
		    				part.add(String1, String2);
		    			}

		    			break;
	    			default:
	    				// do nothing
	    		}
	    	}
	    }
	    if(logger.isInfoEnabled()) {
	    	logger.info("nbLink: {}", cpt);
	    }

	    cpt = 0;

	    // constructs output
        JSONObject outputLinks = new JSONObject();
        for(Object o : looksForLinks) { //from : {source|target|cluster} to : {source|target|cluster}
            if (o instanceof JSONObject) {
                outputLinks.put(((JSONObject)o).getString(FROM_KEY) + "-" + ((JSONObject)o).getString(TO_KEY), new JSONArray());
            }
        }

	    for(List<String> clazz: part) {
	    	++cpt;

	    	for(Object o : looksForLinks) { //from : {source|target|cluster} to : {source|target|cluster}
		    	if(o instanceof JSONObject) {
		    		JSONObject lookedLink = (JSONObject) o;
		    		if (!lookedLink.optString(FROM_KEY).isEmpty() && !lookedLink.optString(TO_KEY).isEmpty()) {
		    		    List<String> s = null;
		    		    switch(lookedLink.getString(FROM_KEY)) {
                            case "source":
                                s = (List<String>) (List<?>) sourceSet.toList();
                                break;
                            case "target":
                                s = (List<String>) (List<?>) targetSet.toList();
                                break;
                            case "cluster":
                                s = (List<String>) (List<?>)  clusterSet.toList();
                                break;
                            default:
                                throw new RuntimeException("Not yet handled");
                        }
                        List<String> t = null;
                        switch(lookedLink.getString(TO_KEY)) {
                            case "source":
                                t = (List<String>) (List<?>) sourceSet.toList();
                                break;
                            case "target":
                                t = (List<String>) (List<?>) targetSet.toList();
                                break;
                            case "cluster":
                                t = (List<String>) (List<?>)  clusterSet.toList();
                                break;
                            default:
                                throw new RuntimeException("Not yet handled");
                        }
		    			outputLinks = addLinks(clazz, outputLinks, lookedLink.getString(FROM_KEY), lookedLink.getString(TO_KEY), s, t, links);
		    		}
		    	}
	    	}
	    }

	    if(logger.isInfoEnabled()) {
 	    	logger.info("nbClazz: {}", cpt);
 	    }

	    return outputLinks;
	}

	private static JSONObject addLinks(List<String> clazz, JSONObject outputLinks, String from, String to, List<String> fromSet, List<String> toSet, JSONArray linksEntry) {
		//Only the from elements are filtered
		List<String> elFrom = clazz.stream().filter(e -> fromSet.contains(e)).collect(Collectors.toList());
		String key = from + "-" + to;

    	if (from.equals(to) && elFrom.size() > 1) { // the array elFrom contains what is needed
			String arrayName = from + "s"; // targets, sources or clusters
			JSONObject link = new JSONObject();
			link.put(TYPE_KEY, SAME_AS_KEY);
			JSONArray links = new JSONArray();

    		for(String o : elFrom) {
    		    links.put(o);
    		}
    		outputLinks.append(key, link.put(arrayName, links));
    	}
    	else {
    		 List<String> elTo = clazz.stream().filter(e -> toSet.contains(e)).collect(Collectors.toList());

			 for(Object o : elFrom) {
				if(o instanceof String) {
					String linkFrom = (String) o;

					for(Object p : elTo) {
                        String linkTo = (String) p;
                        //from and to need to be differents
                        if (!linkFrom.equals(linkTo)){

                            //don't add link already known
                            boolean ok = true;
                            for(Object q : linksEntry) {
                                if(q instanceof JSONObject) {
                                    JSONObject linkE = (JSONObject)q;
                                    if (linkE.optString(from).equals(linkFrom) &&
                                            linkE.optString(to).equals(linkTo) ) {
                                        ok = false;
                                    }
                                }
                             }

                            if (ok)
                                outputLinks.append(key, createLink(linkFrom,linkTo,clazz));
                        }
			    	}
				}
			 }
    	}

    	return outputLinks;
	}

	private static JSONObject createLink(String source, String target, List<String> clazz) {
		JSONObject link = new JSONObject();
		link.put(TYPE_KEY, SAME_AS_KEY);
		link.put(SOURCE_KEY, source);
		link.put(TARGET_KEY, target);
		JSONObject why = new JSONObject();
		why.put("transitivity", new JSONArray(clazz));
		link.put("why",why);
		return link;
	}

}
