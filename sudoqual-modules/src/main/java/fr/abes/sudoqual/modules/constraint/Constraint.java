package fr.abes.sudoqual.modules.constraint;

import fr.abes.sudoqual.util.json.JSONArrays;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Constraint {

    private static final String TYPE_KEY = "type";
    private static final String SAME_AS_KEY = "sameAs";
    private static final String SUGGESTED_SAME_AS_KEY = "suggestedSameAs";

    public static JSONArray oneToOne(JSONArray links, JSONArray safeLinks, String from, String to) {
        JSONArray res = new JSONArray();
        JSONObject map = new JSONObject();

        addToMap(safeLinks, map, res, from, to, true);
        addToMap(links, map, res, from, to, false);

        for(String key : map.keySet()) {
            JSONArray array = map.getJSONArray(key);
            if(array.length() == 1) {
                res.put(((Element)array.get(0)).element);
            } else {
                for(Object o : array) {
                    Element e = (Element)o;
                    if(e.isSafe) {
                        res.put(e.element);
                    } else {
                        res.put(downgrade(e.element));
                    }
                }
            }
        }
        return res;
    }

    private static void addToMap(JSONArray links, JSONObject map, JSONArray res, String from, String to, boolean isSafe) {
        for(Object o : links) {
            if(o instanceof JSONObject) {
                JSONObject obj = (JSONObject)o;
                if(SAME_AS_KEY.equals(obj.optString(TYPE_KEY)) && obj.has(from) && obj.has(to)) {
                    String key = obj.getString(from) + "-" + obj.getString(to);
                    map.append(key, new Element(obj, isSafe));
                } else {
                    res.put(obj); // FIXME not specified what to do in this case
                }
            } else {
                res.put(o); // FIXME not specified what to do in this case
            }
        }
    }

    public static JSONArray oneToMany(JSONArray links, JSONArray safeLinks, String from, String to) {
        return manyToOne(links, safeLinks, to, from);
    }

    // TODO check this method
    public static JSONArray manyToOne(JSONArray links, JSONArray safeLinks, String from, String to) {
        JSONArray res = new JSONArray();
        List<String> hasSafe = new LinkedList<>();
        Map<String, List<JSONObject>> tmp = new HashMap<>();

        for(Object o : safeLinks) {
            res.put(o);
            if(o instanceof JSONObject) {
                JSONObject obj = (JSONObject)o;
                if(SAME_AS_KEY.equals(obj.optString(TYPE_KEY))) {
                    if(obj.has(from) && obj.has(to)) {
                        hasSafe.add(obj.getString(to));
                    }
                }
            }
        }

        for(Object o : links) {
            boolean filtered = false;
            if(o instanceof JSONObject) {
                JSONObject obj = (JSONObject)o;
                if(SAME_AS_KEY.equals(obj.optString(TYPE_KEY))) {
                    if(obj.has(from) && obj.has(to)) {
                        filtered = true;
                        List<JSONObject> array = tmp.get(to);
                        if(array == null) {
                            array = new LinkedList<>();
                            tmp.put(to, array);
                        }
                        array.add(obj);
                    }
                }
            }
            if(!filtered) {
                res.put(o);
            }
        }

        for(Map.Entry<String, List<JSONObject>> e : tmp.entrySet()) {
            if(hasSafe.contains(e.getKey()) || e.getValue().size() > 1) {
                for(JSONObject obj : e.getValue()) {
                    res.put(downgrade(obj));
                }
            } else {
                assert e.getValue().size() == 1;
                res.put(e.getValue().get(0));
            }
        }

        return res;
    }

    private static JSONObject downgrade(JSONObject obj) {
        JSONObject o = new JSONObject(obj.toString());
        o.put(TYPE_KEY, SUGGESTED_SAME_AS_KEY);
        JSONObject why = o.optJSONObject("why");
        why.put("constraint", "This link has been downgraded because it does not fulfill constraints.");
        return o;
    }

    private static class Element {
        final boolean isSafe;
        Element(JSONObject element, boolean isSafe) {
            this.isSafe = isSafe;
            this.element = element;
        }
        final JSONObject element;
    }


}
