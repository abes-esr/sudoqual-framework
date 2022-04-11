/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.linking_module.BusinessClassLoader;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.Scenario;
import fr.abes.sudoqual.linking_module.exception.BusinessClassException;
import fr.abes.sudoqual.linking_module.heuristic.BasicLinkHeuristic;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.rule_engine.exception.FeatureConfigurationException;
import fr.abes.sudoqual.rule_engine.exception.PredicateConfigurationException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.feature.RawFeature;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.util.ConfigurationProperties;

/**
 * IntrospectionBusinessClassLoader is an {@link BusinessClassLoader} implementation using 
 * introspection to do so. It uses the org.reflections library available at 
 * {@link https://github.com/ronmamo/reflections}
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class IntrospectionBusinessClassLoader implements BusinessClassLoader {

	private static final Logger logger = LoggerFactory.getLogger(IntrospectionBusinessClassLoader.class);

	private Reflections reflections;
	private ConfigurationProperties properties;

	private Map<String, Class<? extends Predicate>> predicateMap;
	private Map<String, Class<? extends Feature>> featureMap;
	private Map<String, Class<? extends LinkHeuristic>> heuristicMap;

	
	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////
	
	public IntrospectionBusinessClassLoader(Scenario scenario) throws BusinessClassException {
		this(scenario.getBusinessClassPackageName(), scenario);
	}

	/**
	 * Constructs an InstanceLoader which will look for classes from the specified package (and
	 * its subpackage).
	 * @param fromPackage
	 * @throws BusinessClassException 
	 */
	public IntrospectionBusinessClassLoader(String fromPackageName, ConfigurationProperties properties) throws BusinessClassException {
		this.properties = properties;
		this.reflections = new Reflections(fromPackageName);

		predicateMap = new ConcurrentHashMap<>();
		Collection<Predicate> predicates = getAllInstances(Predicate.class);
		predicates.forEach(x -> predicateMap.put(x.getKey(), x.getClass()));
		if (logger.isInfoEnabled()) {
			predicates.forEach(x -> logger.info("Load predicate {} as {}", x.getClass(), x.getKey()));
		}
		this.predicateMap = Collections.unmodifiableMap(predicateMap);

		featureMap = new ConcurrentHashMap<>();
		Collection<Feature> features = getAllInstances(Feature.class);
		features.forEach(x -> featureMap.put(x.getKey(), x.getClass()));
		if(featureMap.containsKey(LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY)) {
			throw new BusinessClassException("The special feature " +LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY + " can't be overitten."); 
		}
		featureMap.put(LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY, InitialLinksRawFeature.class);
		
		if (logger.isInfoEnabled()) {
			features.forEach(x -> logger.info("Load feature {} as {}", x.getClass(), x.getKey()));
		}
		this.featureMap = Collections.unmodifiableMap(featureMap);
		
		heuristicMap = new ConcurrentHashMap<>();
		Collection<LinkHeuristic> heuristics = getAllInstances(LinkHeuristic.class);
		heuristics.forEach(x -> heuristicMap.put(x.getKey(), x.getClass()));
		if(!heuristicMap.containsKey(BasicLinkHeuristic.NAME)) {
			heuristicMap.put(BasicLinkHeuristic.NAME, BasicLinkHeuristic.class);
		}
		if (logger.isInfoEnabled()) {
			heuristics.forEach(x -> logger.info("Load heuristic {} as {}", x.getClass(), x.getKey()));
		}
		this.heuristicMap = Collections.unmodifiableMap(heuristicMap);
		
		
		
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public Set<String> getPredicateNames() {
		return predicateMap.keySet();
	}

	@Override
	public Predicate createPredicate(String name) {
		Class<? extends Predicate> class1 = this.predicateMap.get(name);
		if(class1 != null) {
			try {
				Predicate p = class1.getDeclaredConstructor().newInstance();
				p.configure(properties);
				return p;
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				logger.warn("Unable to instantiate the following predicate: {}" + class1 + " because: ", e);
			} catch (PredicateConfigurationException e) {
				logger.warn("Unable to configure the following predicate: {}" + class1 + " because: ", e);
			}
		}
		return null;
	}

	@Override
	public Set<Predicate> createPredicates(Iterable<String> names) {
		Set<Predicate> set = new HashSet<>();
		for(String s : names) {
			set.add(this.createPredicate(s));
		}
		set.remove(null);
		return set;
	}

	@Override
	public Set<String> getFeatureNames() {
		return featureMap.keySet();
	}

	@Override
	public Feature createFeature(String name) {
		Class<? extends Feature> class1 = this.featureMap.get(name);
		if(class1 != null) {
			try {
				Feature f = class1.getDeclaredConstructor().newInstance();
				f.configure(properties);
				return f;
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				logger.warn("Unable to instantiate the following feature: {}" + class1 + " because: ", e);
			} catch (FeatureConfigurationException e) {
				logger.warn("Unable to configure the following feature: {}" + class1 + " because: ", e);
			}
		}
		return null;
	}

	@Override
	public Set<Feature> createFeatures(Iterable<String> names) {
		Set<Feature> set = new HashSet<>();
		for(String s : names) {
			set.add(this.createFeature(s));
		}
		return set;
	}

	@Override
	public Set<String> getLinkHeuristicNames() {
		return heuristicMap.keySet();
	}

	@Override
	public LinkHeuristic createLinkHeuristic(String name) {
		Class<? extends LinkHeuristic> class1 = this.heuristicMap.get(name);
		if(class1 != null) {
			try {
				return class1.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.warn("Unable to instantiate the following heuristic: " + class1 + " because: ", e);
			}
		}
		return null;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private <T> Collection<T> getAllInstances(final Class<T> type) {
		Collection<T> list = new LinkedList<>();
		Set<Class<? extends T>> classes = reflections.getSubTypesOf(type);
		for (Class<? extends T> cl : classes) {
			int modifiers = cl.getModifiers();
			if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !cl.equals(InitialLinksRawFeature.class)) {
				try {
					list.add(type.cast(cl.getDeclaredConstructor().newInstance()));
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					logger.warn("Unable to instantiate the following class: " + cl + " because: ", e);
				}
			}
		}
		return list;
	}
	
	private static class InitialLinksRawFeature implements RawFeature {

		@Override
		public String getKey() {
			return LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY;
		}
	}

}
