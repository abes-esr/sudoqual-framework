/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.exception.FeatureManagerException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.feature.PreprocessedFeature;
import fr.abes.sudoqual.rule_engine.feature.UpdateableComputedFeature;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class FeatureManagerImpl implements FeatureManager {
	
	private static final Logger logger = LoggerFactory.getLogger(FeatureManagerImpl.class);

	private final Object internalLock = new Object();
	private final TIntObjectMap<JSONObject> data;
    
    // /////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////
	
	public FeatureManagerImpl(String jsonString, Map<String, Feature> featureMap) {
		this(new JSONObject(jsonString), featureMap) ;
	}
	
	public FeatureManagerImpl(JSONObject json, Map<String, Feature> featureMap) {
		if(logger.isDebugEnabled()) {
			logger.debug("create store from: {}", json);
		}
		this.data = new TIntObjectHashMap<>();

		Iterator<String> it = json.keys();
		while(it.hasNext()) {
			String reference = it.next();
			JSONObject rawValues = json.getJSONObject(reference);
			JSONObject processedValues = new JSONObject();
			processedValues.put(FeatureManager.URI_KEY, reference);
			
			for(String featureKey : featureMap.keySet()) {
				try {
					Feature feature = featureMap.get(featureKey);
					if(feature instanceof ComputedFeature) {
						// do nothing, be processed later
					} else if(feature instanceof PreprocessedFeature) {
						PreprocessedFeature pFeat = (PreprocessedFeature) feature;
						String relatedFeat = pFeat.getRelatedRawFeature();
						if(rawValues.has(relatedFeat)) {
							Object value = pFeat.buildValue(rawValues.get(relatedFeat));
							if(value != null) {
    							processedValues.put(featureKey, value);
    							if(logger.isInfoEnabled()) {
    								logger.info("processed value for {}: '{}'", featureKey, value);
    							}
							}
						}
					} else if (feature instanceof Feature){
						if(rawValues.has(featureKey)) {
							if(featureKey.equals(FeatureManager.URI_KEY)) {
								throw new FeatureManagerException("Overwrite URI special feature is forbidden");
							}
							processedValues.put(featureKey, rawValues.get(featureKey));
						}
					} else {
						throw new FeatureManagerException("Using undeclared feature: " + featureKey);
					}
				} catch (Exception e) {
					throw new FeatureManagerException("An error occured during processing feature " + featureKey + " from reference " + reference, e);
				}
			}
			data.put(Reference.nameToId(reference), processedValues);
		}
		
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////


	@Override
	public JSONObject getFeaturesValue(Reference ref, Collection<String> feature) {
		return this.get(ref);
	}
	
	public Collection<JSONObject> getFeaturesValue(Collection<Reference> refList, Collection<String> feature) {
		Collection<JSONObject> res = new LinkedList<>();
		for(Reference ref : refList) {
			res.add(this.data.get(ref.getId()));
		}
		return res;
	}
	
	@Override
	public JSONObject get(Reference ref) {
		if(!this.data.containsKey(ref.getId())) {
			throw new FeatureManagerException("No data found for reference: " + ref.getName());
		}
		return this.data.get(ref.getId());
	}
	
	@Override
	public JSONObject get(String ref) {
		return this.data.get(Reference.nameToId(ref));
	}
	
	@Override
	public boolean exist(Reference ref) {
		return this.data.get(ref.getId()) != null;
	}

	@Override
	public boolean exist(Reference ref, String feature) {
		JSONObject object = this.data.get(ref.getId());
		return object != null && object.has(feature);
	}
	
	@Override
	public void updateComputedFeatures(Reference ref, Collection<ComputedFeature<?>> featList, Set<Reference> newSameAs, Set<Reference> allSameAs) {
		for (ComputedFeature<?> feat : featList) {
			this.updateComputedFeature(ref, feat, newSameAs, allSameAs);
		}
	}
	
	private void updateComputedFeature(Reference ref, ComputedFeature<?> feature, Set<Reference> newSameAs, Set<Reference> allSameAs) {
		synchronized(internalLock) {
			JSONObject data = this.data.get(ref.getId());
			Object oldValue = null, newValue = null;
			if(data.has(feature.getKey())) {
				oldValue = data.get(feature.getKey());
			}
			if(feature instanceof UpdateableComputedFeature) {
    			Collection<JSONObject> selectedData = this.getFeaturesValue(newSameAs, feature.getRelatedFeatures());
    			
    			newValue = ((UpdateableComputedFeature)feature).update(oldValue, selectedData);
			} else {
				Collection<JSONObject> selectedData = this.getFeaturesValue(allSameAs, feature.getRelatedFeatures());
				newValue = feature.compute(selectedData);
			}
			this.put(ref, feature.getKey(), newValue);
    	 	if(newValue == null && oldValue != null) {
    	 		logger.warn("The computed feature {} which was not null, are now computed null for {} with new sameas {} and allSameAs {}", feature.getKey(), ref, newSameAs, allSameAs);
    	 	}
			
			
			if(logger.isInfoEnabled()) {
    	 		logger.info("Updated computed feature {} for {}: {}", feature.getKey(), ref.getName(), this.get(ref).get(feature.getKey()));
    	 	}
    	 }
	}

	/*@Override
	public void push(Reference ref, String feature, Object value) {
		synchronized(this) {
			JSONObject refData = this.data.get(ref.getId());
			Object object = (refData.has(feature))? refData.get(feature) : null;
			JSONArray array;
			if(object instanceof JSONArray) {
				array = (JSONArray) object;
			} else {
				array = new JSONArray();
				refData.put(feature, array);
				if(object != null) {
					array.put(object);
				}
			}
			array.put(value);
		}
	}*/
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int k : this.data.keys()) {
			JSONObject o = this.data.get(k);
			sb.append(k)
			.append(":\n")
			.append(o.toString(1))
			.append('\n');
		}
		return sb.toString();
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	PRIVATE OR PROTECTED METHODS
	// /////////////////////////////////////////////////////////////////////////

	protected void put(Reference ref, String key, Object o) {
		if(o != null) {
			this.data.get(ref.getId()).put(key, o);
		}
	}

}
