/*
 * This class has been developed for ABES (http://www.abes.fr/)
 */
package fr.abes.sudoqual.rule_engine.impl.lumbago;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.impl.ReferenceImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.exception.InconsistentException;
import fr.abes.sudoqual.rule_engine.predicate.exception.NotComparableException;

/**
 * This is a thread-safe implementation of {@link IRefManager}.
 * 
 * @author Clément SIPIETER {@literal <clement@6pi.fr>}
 */
public class PredicateManagerImpl implements PredicateManager {
    
	private static final Logger logger = LoggerFactory.getLogger(PredicateManagerImpl.class);

    private final ConcurrentMap<Reference, ConcurrentMap<Reference, ConcurrentMap<String, CacheObject<Integer>>>> criterionCache;
    private final ConcurrentMap<Reference, ConcurrentMap<String, Set<CacheObject<Integer>>>> criterionCache2;
            
    private final ConcurrentMap<Reference, ConcurrentMap<String, CacheObject<Boolean>>> filterCache;
    
    private FeatureManager store;
    
    ////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////////

    public PredicateManagerImpl(FeatureManager store, ConcurrentMap<String, ComputedFeature<?>> computedFeaturesMap) {
    	this.store = store;
                
        this.criterionCache = new ConcurrentHashMap<>(2048);
        this.criterionCache2 = new ConcurrentHashMap<>(4096);
        this.filterCache = new ConcurrentHashMap<>(2048);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // METHODS
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void load(Iterable<Object> values) {
    	for(Object obj : values) {
    		assert obj instanceof JSONObject;
    		JSONObject o = (JSONObject) obj; 
    		if(o.has("reference")) {
    			this.loadFilterValue(o);
    		} else {
    			this.loadCriterionValue(o);
    		}
    	}
    }

	@Override
	public FeatureManager getFeatureManager() {
		return this.store;
	}
    
    @Override
    public int compare(Criterion criterion, Reference ref1, Reference ref2) {
    	CacheObject<Integer> o = this.getCriterionValueSync(criterion.getKey(), ref1, ref2);
        synchronized(o) {
            if(!o.isSet()) {
                try {
					o.set(criterion.compare(this.store.getFeaturesValue(ref1, criterion.sourceFeatureSet()), this.store.getFeaturesValue(ref2, criterion.targetFeatureSet())));
				} catch (Exception e) {
					logger.error("An error occured in a criterion compare method:", e);
					o.set(DiscretCompType.ERROR);
				}
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("{}# {}({}, {}) = {}", Thread.currentThread(), criterion, ref1, ref2, o.get());
        }
        
        return o.get();
    }

    @Override
    public boolean check(Filter filter, Reference ref) {
        CacheObject<Boolean> o = this.getFilterValueSync(filter.getKey(), ref);
        synchronized(o) {
            if(!o.isSet()) {
				o.set(filter.check(this.store.getFeaturesValue(ref, filter.featureSet())));
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("{}# {}({}) = {}", Thread.currentThread(), filter, ref, o.get());
        }

        return o.get();
    }
    
    @Override
    public void cleanCriterions(Collection<Reference> refSet, Collection<Criterion> criterionNameSet) {
        for(Reference ref : refSet) {
            synchronized(ref) {
                for(Criterion s : criterionNameSet) {
                	if(logger.isDebugEnabled()) {
                 		logger.debug("{}# clear({}, {})", Thread.currentThread(), ref, s); 
                 	}
                    Set<CacheObject<Integer>> set = this.getCriterionCacheSync(ref, s.getKey());
                    for(CacheObject<Integer> o : set) {
                        o.clear();
                    }
                }
            }
        }
    }
    
    @Override
    public void cleanFilters(Collection<Reference> refSet, Collection<Filter> filterNameSet) {
        for(Reference ref : refSet) {
            synchronized(ref) {
                ConcurrentMap<String, CacheObject<Boolean>> cache = this.getFilterCacheSync(ref);
                for(Filter s : filterNameSet) {
                    CacheObject<Boolean> o = cache.get(s.getKey());
                    if(o != null) {
                        o.clear();
                    }
                }
            }
        }
    }
    
    @Override
    public Collection<JSONObject> exportCache() {
    	List<JSONObject> res = new LinkedList<>();
    	exportCriterionCache(res);
    	exportFilterCache(res);
    	return res;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    ////////////////////////////////////////////////////////////////////////////
    
    private void exportCriterionCache(List<JSONObject> res) {
		for(Map.Entry<Reference, ConcurrentMap<Reference, ConcurrentMap<String, CacheObject<Integer>>>> e1 : this.criterionCache.entrySet()) {
			for(Map.Entry<Reference, ConcurrentMap<String, CacheObject<Integer>>> e2 : e1.getValue().entrySet()) {
				for(Map.Entry<String, CacheObject<Integer>> e3 : e2.getValue().entrySet()) {
					CacheObject<Integer> value = e3.getValue();
					if(value.isSet()) {
    					JSONObject o = new JSONObject();
    					o.put("source", e1.getKey().getName());
    					o.put("target", e2.getKey().getName());
    					o.put("name", e3.getKey());
    					o.put("value", value.get());
    					res.add(o);
					}
				}
			}
		}
	}

	private void exportFilterCache(List<JSONObject> res) {
		for(Entry<Reference, ConcurrentMap<String, CacheObject<Boolean>>> e1 : this.filterCache.entrySet()) {
			for(Entry<String, CacheObject<Boolean>> e2 : e1.getValue().entrySet()) {
				CacheObject<Boolean> value = e2.getValue();
				if(value.isSet()) {
    				JSONObject o = new JSONObject();
    				o.put("reference", e1.getKey());
    				o.put("name", e2.getKey());
    				o.put("value", value.get());
    				res.add(o);
				}
			}
		}
	}

	protected Set<CacheObject<Integer>> getCriterionCacheSync(Reference ref, String criterion) {
        ConcurrentMap<String, Set<CacheObject<Integer>>> cache;
        synchronized(criterionCache2) {
            cache = this.criterionCache2.get(ref);
            if(cache == null) {
                cache = this.<String, Set<CacheObject<Integer>>> createMap();
                this.criterionCache2.put(ref, cache);
            }
        }
        
        Set<CacheObject<Integer>> set;
        synchronized(cache) {
            set = cache.get(criterion);
            if(set == null) {
                set = Collections.newSetFromMap( this. <CacheObject<Integer>, Boolean> createMap());
                cache.put(criterion, set);
            }
        }
        
        return set;
    }
    
	protected ConcurrentMap<String, CacheObject<Boolean>> getFilterCacheSync(Reference ref) {
		synchronized (this.filterCache) {
			ConcurrentMap<String, CacheObject<Boolean>> cache = this.filterCache.get(ref);
			if (cache == null) {
				cache = this.<String, CacheObject<Boolean>> createMap();
				this.filterCache.put(ref, cache);
			}

			return cache;
		}
	}
    
    protected CacheObject<Integer> getCriterionValueSync(String criterion, Reference ref1, Reference ref2) {
        
        ConcurrentMap<Reference, ConcurrentMap<String, CacheObject<Integer>>> map1;
        synchronized(this.criterionCache) {
            map1 = this.criterionCache.get(ref1);
            if(map1 == null) {
                map1 = this.<Reference, ConcurrentMap<String, CacheObject<Integer>>>createMap();
                this.criterionCache.put(ref1, map1);
            }
        }
        
        ConcurrentMap<String, CacheObject<Integer>> map2;
        synchronized(map1) {
            map2 = map1.get(ref2);
            if(map2 == null) {
                map2 = this.<String, CacheObject<Integer>>createMap();
                map1.put(ref2, map2);
            }
        }
        
        CacheObject<Integer> o;
        synchronized(map2) {
            o = map2.get(criterion);
            if(o == null) {
                o = new CacheObject<>();
                map2.put(criterion, o);
                this.getCriterionCacheSync(ref1, criterion).add(o);
                this.getCriterionCacheSync(ref2, criterion).add(o);
            }
        }
        
        return o;
    }
    
    protected CacheObject<Boolean> getFilterValueSync(String filter, Reference ref) { 
        ConcurrentMap<String, CacheObject<Boolean>> map ;
        synchronized(this.filterCache) {
            map = this.filterCache.get(ref);
            if(map == null) {
                map = this.<String, CacheObject<Boolean>>createMap();
                this.filterCache.put(ref, map);
            }
        }
        
        CacheObject<Boolean> o;
        synchronized(map) {
            o = map.get(filter);
            if(o == null) {
                o = new CacheObject<>();
                map.put(filter, o);
            }
        }
        
        return o;
    }
    
    protected <T,U> ConcurrentMap<T, U> createMap() {
        return new ConcurrentHashMap<>();
    }
    
    protected void loadCriterionValue(JSONObject o) {
    	Reference source = new ReferenceImpl(o.getString("source"));
    	Reference target = new ReferenceImpl(o.getString("target"));
    	String criterionName = o.getString("name");
    	Integer value = o.getInt("value");	
    	this.loadCriterionValue(criterionName, source, target, value);
    }
    
    protected void loadCriterionValue(String criterionName, Reference source, Reference target, Integer value) {
    	CacheObject<Integer> o = this.getCriterionValueSync(criterionName, source, target);
    	o.set(value);
	}

    protected void loadFilterValue(JSONObject o) {
    	String filterName = o.getString("name");
    	Reference ref = new ReferenceImpl(o.getString("reference"));
    	Boolean value = o.getBoolean("value");
    	this.loadFilterValue(filterName, ref, value);
    }
    
    protected void loadFilterValue(String filterName, Reference ref, Boolean value) {
    	 CacheObject<Boolean> o = this.getFilterValueSync(filterName, ref);
         o.set(value);
	}
            
    ////////////////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    ////////////////////////////////////////////////////////////////////////////
            
    protected class CacheObject<T> {
        private T value = null;
        
        public void set(T i) {
            this.value = i;
        }
        
        public T get() {
            return value;
        }
        
        public boolean isSet() {
            return this.value != null;
        }
        
        public void clear() {
            this.value = null;
        }
    }

}
    
  
