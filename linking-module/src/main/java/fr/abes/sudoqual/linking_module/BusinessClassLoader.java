/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.util.Set;

import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

/**
 * BusinessClassLoader is responsible to load needed {@link Predicate} and {@link Feature}.
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 *
 */
public interface BusinessClassLoader {

	/**
	 * @return the set of available {@link Predicate} names.
	 * @see {@link Predicate#getName()}
	 */
	Set<String> getPredicateNames();

	/**
	 * 
	 * @param name a {@link Predicate} name.
	 * @return a fresh {@link Predicate} instance based on its name. Or null, if no corresponding {@link Predicate} availables.
	 */
	Predicate createPredicate(String name);
	
	/**
	 * 
	 * @param names a set of {@link Predicate} names.
	 * @return a set of fresh {@link Predicate} instances based on their names. If no predicate availables
	 * for a name, it will be ignored without warning.
	 */
	Set<Predicate> createPredicates(Iterable<String> names);

	/**
	 * @return the set of available {@link Feature} names.
	 * @see {@link Feature#getName()}
	 */
	Set<String> getFeatureNames();

	/**
	 * 
	 * @param name a {@link Feature} name.
	 * @return a fresh {@link Feature} instance based on its name. Or null, if no corresponding {@link Feature} availables.
	 */
	Feature createFeature(String name);
	
	/**
	 * 
	 * @param names a set of {@link Feature} names.
	 * @return a set of fresh {@link Feature} instances based on their names. If no feature availables
	 * for a name, it will be ignored without warning since it probably is a raw feature.
	 */
	Set<Feature> createFeatures(Iterable<String> names);
	
	/**
	 * @return the set of available {@link LinkHeuristic} names.
	 * @see {@link LinkHeuristic#getName()}
	 */
	Set<String> getLinkHeuristicNames();

	/**
	 * 
	 * @param name a {@link LinkHeuristic} name.
	 * @return a fresh {@link LinkHeuristic} instance based on its name. Or null, if no corresponding {@link LinkHeuristic} availables.
	 */
	LinkHeuristic createLinkHeuristic(String name);

	

}