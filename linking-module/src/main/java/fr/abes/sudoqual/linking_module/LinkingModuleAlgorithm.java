/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.exception.LinkingModuleAlgorithmException;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.linking_module.impl.ScenarioWithOptions;
import fr.abes.sudoqual.linking_module.multithreads.Consumer;
import fr.abes.sudoqual.linking_module.multithreads.Task;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
interface LinkingModuleAlgorithm {
	
	/**
	 * A static factory method to create an instance of a LinkingModuleAlgorithm
	 * @param scenario the scenario configuration which should be an aggregation from the scenario property file and
	 * options given through the JSON input.
	 * @param engine the rule engine instance to be used
	 * @param heuristic the heuristic instance to be used
	 * @param computedFeatList the set of computed features to be used
	 * @param predicateList the set of predicates to be used
	 * @param queue a {@link BlockingQueue} in which put {@link Task} to be processed by {@link Consumer} instances. Different
	 * instance of LinkingModuleAlgorithm can share a same set of consumers.
	 * @return an instance of a LinkingModuleAlgorithm
	 */
	static LinkingModuleAlgorithm create(ScenarioWithOptions scenario, RuleEngine engine, LinkHeuristic heuristic,
	    Collection<ComputedFeature<?>> computedFeatList, Collection<Predicate> predicateList, BlockingQueue<Task> queue) {
		return new LinkingModuleAlgorithmImpl(scenario, engine, heuristic, computedFeatList, predicateList, queue);
	}

	/**
	 * Launches the module over set of sources, targets and supports with the specified predicate manager and 
	 * a set of asserted safe link.
	 * @param manager
	 * @param sources
	 * @param targets
	 * @param supports a set of reference whose data can be used to enrich source or target references.
	 * @param safeLinks a set of link (sameAs or diffFrom) that must be interpreted by the linking module
	 * algorithm of certain and therefore not disputed. It will possibly used to enrich data from source or
	 * target at the first step.
	 * @return a set of inferred links
	 * @throws LinkingModuleAlgorithmException
	 * @throws InterruptedException
	 */
	Set<Link> launch(PredicateManager manager, Collection<Reference> sources, Collection<Reference> targets,
	    Collection<Reference> supports, Collection<Link> safeLinks)
	    throws LinkingModuleAlgorithmException, InterruptedException;
	
	void enableDebugMode(boolean enable);
	void enableClusteringMode(boolean enable);

	JSONObject getDebugData();

}