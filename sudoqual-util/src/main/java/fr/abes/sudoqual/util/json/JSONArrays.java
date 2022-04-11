package fr.abes.sudoqual.util.json;

import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class JSONArrays {

	private JSONArrays() {}

	/**
	 * Checks if the specified JSONArray contains the specified Object
	 * @param array a JSONArray
	 * @param value an Object to look for
	 * @return true if the array contains the specified object, false otherwise
	 * @see {@link Object#equals(Object)}
	 */
	public static boolean contains(JSONArray array, Object value) {
		for(Object o : array) {
			if(value.equals(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if an element of the specified JSONArray fulfills the checker requirements
	 * @param array a JSONArray
	 * @param checker a Predicate<Object> to check array's elements
	 * @return true if the checker returns true for at least one element of the specified array,
	 * false otherwise.
	 */
	public static boolean contains(JSONArray array, Predicate<Object> checker) {
		for(Object o : array) {
			if(checker.test(o)) {
				return true;
			}
		}
		return false;
	}

    public static void appendAllTo(Object from, JSONArray to) {
        if (from instanceof JSONArray) {
            appendAllTo((JSONArray) from, to);
        } else {
            to.put(from);
        }
    }

    public static void appendAllTo(JSONArray from, JSONArray to) {
	    if(from != null) {
            for (Object o : from) {
                to.put(o);
            }
        }
    }

    /**
     * Merge elements from "from" into "to". Elements equality are checked with BiPredicate "equals". In case
     * of equality, elements are merged with BinaryOperator "merge".
     * @param from
     * @param to
     * @param equals
     * @param merge
     */
    public static void mergeIn(JSONArray from, JSONArray to, BiPredicate<Object, Object> equals, BinaryOperator<Object> merge) {
        if(from != null) {
            for (Object oFrom : from) {
                boolean found = false;
                for(int i=0; i<to.length(); ++i) {
                    if(equals.test(oFrom, to.get(i))) {
                        found = true;
                        Object oTo = to.remove(i);
                        to.put(merge.apply(oFrom, oTo));
                        break;
                    }
                }
                if(!found) {
                    to.put(oFrom);
                }
            }
        }
    }

    public static JSONArray copy(JSONArray toAdd) {
	    return new JSONArray(toAdd.toString());
    }

    public static void putAllInto(JSONArray from, JSONArray to) {
	    for(Object o : from) {
	        to.put(o);
        }
    }

	public static void rename(JSONArray objectOfWork, String[] fromPath, String[] toPath) {
	    for(Object o : objectOfWork) {
	        if(o instanceof JSONObject) {
                JSONObjects.rename((JSONObject) o, fromPath, toPath);
            }
        }
	}
}
