package fr.abes.sudoqual.util.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public final class JSONObjects {

	private JSONObjects() {}

	/**
	 * Gets a {@link JSONObject} from its {@link String} representation.
	 * @param jsonString
	 * @return a JSONObject instance.
	 * @throws JSONException
	 */
	public static JSONObject from(String jsonString) throws JSONException {
		return new JSONObject(new JSONTokener(new StringReader(jsonString)));
	}

	/**
	 * Gets a {@link JSONObject} from its {@link String} representation given by an {@link InputStream}.
	 * @param jsonStream
	 * @param charset
	 * @return a JSONObject instance.
	 */
	public static JSONObject from(InputStream jsonStream, Charset charset) {
		return new JSONObject(new JSONTokener(new InputStreamReader(jsonStream, charset)));
	}

	/**
	 * Gets a {@link JSONObject} from its {@link String} representation given by a {@link Reader}.
	 * @param jsonStream
	 * @param charset
	 * @return a JSONObject instance.
	 */
	public static JSONObject from(Reader jsonStream) {
		return new JSONObject(new JSONTokener(jsonStream));
	}

	/**
	 * Gets a {@link JSONObject} from a JSON file using the specified {@link Charset}.
	 * @param jsonStream
	 * @param charset
	 * @return a JSONObject instance.
	 */
	public static JSONObject from(File jsonFile, Charset charset) throws JSONException, IOException {
		try(InputStream is = new FileInputStream(jsonFile)) {
			return from(is, charset);
		}
	}

    /**
     * Merge two JSONObject, if a key is present in the both object then values are merged into a JSONArray using
     * {@link JSONObject#accumulate(String, Object)}.
     * @param o1
     * @param o2
     * @return
     */
    public static JSONObject merge(JSONObject o1, JSONObject o2) {
        JSONObject res = new JSONObject();
        mergeIn(o1, res);
        mergeIn(o2, res);
        return res;
    }

    /**
     * Merge content of from into to.
     * @param from JSONObject to merge into "to"
     * @param to JSONObject in which from will be merged
     */
	public static void mergeIn(JSONObject from, JSONObject to) {
	    for(String keyFrom : from.keySet()) {
            Object toAdd = from.get(keyFrom);
            Object toMergeWith = to.opt(keyFrom);
            if(toMergeWith == null) {
                if(toAdd instanceof JSONArray) {
                    to.put(keyFrom,JSONArrays.copy((JSONArray)toAdd));
                } else if(toAdd instanceof JSONObject) {
                    to.put(keyFrom,JSONObjects.copy((JSONObject)toAdd));
                } else {
                    to.put(keyFrom, toAdd);
                }
            } else if(toMergeWith instanceof JSONArray) {
                JSONArrays.appendAllTo(toAdd, (JSONArray) toMergeWith);
            } else if(toAdd instanceof JSONArray) {
                to.put(keyFrom, JSONArrays.copy((JSONArray)toAdd));
            } else {
                if(toAdd instanceof JSONObject) {
                    to.accumulate(keyFrom,JSONObjects.copy((JSONObject)toAdd));
                } else {
                    to.accumulate(keyFrom, toAdd);
                }
            }
        }
    }

    public static JSONObject copy(JSONObject toCopy) {
	    return new JSONObject(toCopy.toString());
    }

    /**
     * Allows to rename property of a JSONObject o
     * @param o
     * @param from dot separated property path. exemple "person.address.street"
     * @param to dot separated property path. exemple "person.street"
     * @throws JSONException
     */
    public static void rename(JSONObject o, String from, String to) throws JSONException {
        String[] fromPath = from.split("\\.");
        String[] toPath = to.split("\\.");

        rename(o, fromPath, toPath);
    }


    public static void rename(JSONObject o, String[] fromPath, String[] toPath) throws JSONException {
	    Object objectOfWork = o;
        JSONObject toRemoveFrom = null;
        int i = -1;

	    // deep to the first uncommon part
	    while(++i < Math.min(fromPath.length, toPath.length)) {
	        if(!fromPath[i].equals(toPath[i])) {
	            break;
            } else {
                toRemoveFrom = (JSONObject) objectOfWork;
                objectOfWork = toRemoveFrom.get(fromPath[i]);
                if(objectOfWork instanceof JSONArray) {
                    JSONArrays.rename((JSONArray) objectOfWork, Arrays.copyOfRange(fromPath, i+1, fromPath.length), Arrays.copyOfRange(toPath, i+1, toPath.length));
                    return;
                } else if(!(objectOfWork instanceof JSONObject)) {
                    throw new JSONException("fromPath is not a correct path for the given object");
                }
            }
        }

        if(!(objectOfWork instanceof JSONObject)) {
            throw new JSONException("fromPath is not a correct path for the given object");
        }

	    // copy
	    Object toMove = objectOfWork;
	    for(; i < fromPath.length; ++i) {
	        if(!(toMove instanceof JSONObject)) {
	            throw new JSONException("fromPath is not a correct path");
            }
            toRemoveFrom = (JSONObject) toMove;
            toMove = ((JSONObject)toMove).get(fromPath[i]);
        }
	    toRemoveFrom.remove(fromPath[fromPath.length - 1]);

	    JSONObject toMoveIn = o;
        for(int j=0; j<toPath.length-1; ++j) {
            JSONObject next = toMoveIn.optJSONObject(toPath[j]);
            if(next == null) {
                next = new JSONObject();
                toMoveIn.put(toPath[j], next);
            }
            toMoveIn = next;
        }
        toMoveIn.put(toPath[toPath.length-1], toMove);
    }

	/**
	 * Formats the given JSON {@link String}.
	 * @param jsonString
	 * @param indentFactor
	 * @return a formated String.
	 * @throws JSONException
	 */
	public static String prettyPrint(String jsonString, int indentFactor) throws JSONException {
		return prettyPrint(from(jsonString), indentFactor);
	}

	/**
	 * Converts the given {@link JSONObjects} as a formated {@link String} .
	 * @param jsonString
	 * @param indentFactor
	 * @return a formated String.
	 * @throws JSONException
	 */
	public static String prettyPrint(JSONObject json, int indentFactor) {
		return json.toString(indentFactor);
	}
}
