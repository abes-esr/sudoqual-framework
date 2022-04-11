package fr.abes.sudoqual.rule_engine.feature;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class PreprocessedArrayFeature<FROM, TO> implements PreprocessedFeature<JSONArray, JSONArray> {

	private static final Logger logger = LoggerFactory.getLogger(PreprocessedArrayFeature.class);

	/**
	 * Builds the final value of an element from the provided JSONArray.
	 * If this method return null for an element, this element will be ignored.
	 * @param rawElementValue
	 * @return the final value of this element.
	 */
	protected abstract TO buildElementValue(FROM rawElementValue);


	@Override
	public JSONArray buildValue(JSONArray rawArray) {
    	JSONArray preproccessedArray = new JSONArray();
    	for(Object o : rawArray) {
    		try {
    			@SuppressWarnings("unchecked") // caught
				TO v = buildElementValue((FROM)o);
    			if(v != null) {
    				preproccessedArray.put(v);
    			}
    		} catch (ClassCastException e) {
    			logger.error("'{}' of type {} is not a correct value for {}",o, o.getClass(), getKey());
    		}
    	}

    	return (preproccessedArray.isEmpty())? null : preproccessedArray;
	}

	@Override
	public boolean checkValue(Object value) {
		if(value instanceof JSONArray) {
			for(Object o : (JSONArray)value) {
    			try {
    				@SuppressWarnings("unchecked") // caught
    				boolean b = checkElementValue((FROM) o);
    				if(!b) {
    					return false;
    				}
    			} catch (ClassCastException e) {
    				return false;
    			}
			}
			return true;
		}
		return false;
	}

    /**
     *
     * @param value
     * @return
     * @deprecated use {@link #getElementValidationSchema()} instead
     */
	@Deprecated
	protected boolean checkElementValue(FROM value) {
		return true;
	}

    @Override
    public JSONObject getValidationSchema() {
        return new JSONObject(
            "{'type': 'array'," +
                "'items':" + this.getElementValidationSchema().toString() +
                "}"
        );
    }

    protected JSONObject getElementValidationSchema() {
	    return new JSONObject("{'not':{'type':'null'}}");
    }
}
