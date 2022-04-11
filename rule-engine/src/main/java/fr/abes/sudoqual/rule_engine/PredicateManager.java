/*
 * This file is part of SudoQual project.
 * Created in 2018-08.
 */
package fr.abes.sudoqual.rule_engine;

import java.util.Collection;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;

/**
 * Manages and stores predicate values.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface PredicateManager {
	
	/**
	 * Gets the attached FeatureManager.
	 * @return the attached FeatureManager.
	 */
	FeatureManager getFeatureManager();
	
	/**
     * Executes the compare method of the specified criterion if the result value is
     * not already in cache.
     * 
     * @param criterion
     * @param ref1
     * @param ref2
     * @return the criterion.compare value.
     */
    int compare(Criterion criterion, Reference ref1, Reference ref2);

    /**
     * Executes the check method of the specified filter if the result value is not
     * already in cache.
     * 
     * @param filter
     * @param ref
     * @return the filter.check value.
     */
    boolean check(Filter filter, Reference ref);

    /**
     * Cleans caches of referenced criterions for specified references.
     * 
     * @param refSet
     * @param criterions
     */
    void cleanCriterions(Collection<Reference> refSet, Collection<Criterion> criterions);

    /**
     * Cleans caches of referenced filters for specified references.
     * 
     * @param refSet
     * @param filters
     */
    void cleanFilters(Collection<Reference> refSet, Collection<Filter> filters);

    /**
     * Allows to export currently computed values of predicates.
     * @return a list of either a criterion value for two references as JSONObject with keys "name", "source", "target", "value"
     * where "name" is the name of the criterion, "source" is the source reference, "target" the target reference and "value" the
     * currently stored value; either a filter value for one reference as JSONObject with keys "name", "reference", "value" where
     * "name" is the name of the filter, "reference" the related reference and "value" the currently stored value for this filter and
     * this reference.
     */
	Collection<JSONObject> exportCache();

	/**
	 * Allows to pre-load some values from a previous call to {@link #exportCache()}.
	 * 
	 * @param values an iterable of JSONObject as defined by the {@link #exportCache()} method. It is declared
	 * as Iterable<Object> because information can be given through a JSONArray which is declared as an Iterable<Object>.
	 * So we keep the generic argument as {@link Object} to be compatible with.
	 */
	void load(Iterable<Object> values);
}
