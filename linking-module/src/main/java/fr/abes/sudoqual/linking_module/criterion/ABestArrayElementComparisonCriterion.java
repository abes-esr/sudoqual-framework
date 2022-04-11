package fr.abes.sudoqual.linking_module.criterion;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public abstract class ABestArrayElementComparisonCriterion<TS, TT, CMP_TYPE extends Comparable<CMP_TYPE>> implements Criterion {

	private HashMap<String, ComparisonCacheEntry> cache;
	private boolean cacheEnabled;
	private CMP_TYPE maxCmpValue;
	
	protected ABestArrayElementComparisonCriterion(boolean enableCache, CMP_TYPE maxCmpValue) {
		this.cacheEnabled = enableCache;
		if(enableCache) {
			this.cache = new HashMap<>(2048);
		}
		this.maxCmpValue = maxCmpValue;
	}

	protected abstract CMP_TYPE compareElement(TS elementSource, TT elementTarget);
		
	protected abstract int mapComparisonValueToCriterionValue(CMP_TYPE cmpValue);
	
	public abstract String sourceFeature();
	
	public abstract String targetFeature();

	
	@Override
	public Set<String> sourceFeatureSet() {
		return Set.of(sourceFeature());
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Set.of(targetFeature());
	}
	
	@Override 
	public int compare(JSONObject source, JSONObject target) {
		JSONArray arraySource, arrayTarget;
		try {
    		arraySource = source.getJSONArray(sourceFeature());
    		arrayTarget = target.getJSONArray(targetFeature());
		} catch (JSONException e) {
			return DiscretCompType.NOT_COMPARABLE;
		}

		if (arraySource.isEmpty() || arrayTarget.isEmpty()) {
			return DiscretCompType.NOT_COMPARABLE;
		}

		// cache
		String cacheKey = source.get("uri") + "_" + target.get("uri");
		CMP_TYPE cmpValue = compare(cacheKey, maxCmpValue, arraySource, arrayTarget);
		
		return mapComparisonValueToCriterionValue(cmpValue); 
	}

	private CMP_TYPE compare(String cacheKey, CMP_TYPE maxValue, JSONArray arraySource, JSONArray arrayTarget) {
		ComparisonCacheEntry cacheEntry = null;
		if(this.cacheEnabled) {
			cacheEntry = cache.get(cacheKey);
			if (cacheEntry == null) {
				cacheEntry = new ComparisonCacheEntry();
				cache.put(cacheKey, cacheEntry);
			}
			if (cacheEntry.lastValue != null && cacheEntry.lastValue.compareTo(maxValue) >= 0) {
				return maxValue;
			}
		}

		// main
		int lastEntry = (this.cacheEnabled)? cacheEntry.lastEntry + 1 : 0;
		CMP_TYPE bestComparisonValue = (this.cacheEnabled)? cacheEntry.lastValue : null;
		CMP_TYPE currentValue;
		for (Object elementSource : arraySource) {
			for (int i = lastEntry; i < arrayTarget.length(); ++i) {
				Object elementTarget = arrayTarget.get(i);
				currentValue = this.compareElement((TS)elementSource, (TT)elementTarget);
				if (currentValue == null) {
					throw new NullPointerException("compareElement between " + elementSource + " and " + elementTarget + " return null.");
				}
				if (currentValue.compareTo(maxValue) >= 0) {
					if(this.cacheEnabled) {
						cacheEntry.lastEntry = i;
						cacheEntry.lastValue = maxValue;
					}
					return currentValue;
				} else if (bestComparisonValue == null || currentValue.compareTo(bestComparisonValue) > 0) {
					bestComparisonValue = currentValue;
				}
			}
		}
		
		if(this.cacheEnabled) {
			cacheEntry.lastEntry = arrayTarget.length() - 1;
			cacheEntry.lastValue = bestComparisonValue;
		}
		return bestComparisonValue;
	}
	
	private class ComparisonCacheEntry {

	    public CMP_TYPE lastValue = null;
	    public int lastEntry = -1;
	}

}
