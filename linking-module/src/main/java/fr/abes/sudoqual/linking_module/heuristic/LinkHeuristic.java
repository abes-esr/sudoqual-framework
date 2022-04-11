/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.heuristic;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.abes.sudoqual.linking_module.Link;
import fr.abes.sudoqual.linking_module.exception.LinkHeuristicException;
import fr.abes.sudoqual.linking_module.exception.UnsupportedHeuristicModeException;
import fr.abes.sudoqual.linking_module.multithreads.CanceledTaskException;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.RuleEngine.Result;
import fr.abes.sudoqual.util.ConfigurationProperties;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface LinkHeuristic {
	
	/**
	 * Defines the name of this heuristic
	 * @return a String representing the name of this heuristic
	 */
	String getKey();

	/**
	 * The main method which will provide links between the specified source and
	 * specified targets.
	 * 
	 * @param store a PredicateManager to evaluate predicates and features.
	 * @param engine a RuleEngine to provides co-reference clues.
	 * @param source the reference to link
	 * @param allCandidates candidates references to be linked with the source
	 * @param currentStep the current step of the main loop
	 * @param isCancelled true if the task was cancelled, so the method should 
	 * throw a CanceledTaskException as soon as possible. False, otherwise.
	 * @return a collection of Link for the specified source.
	 * @throws LinkHeuristicException
	 * @throws CanceledTaskException 
	 */
	Collection<Link> findLinks(PredicateManager store, RuleEngine engine, Reference source,
	    Set<Candidate> allCandidates, AtomicBoolean isCancelled)
	    throws LinkHeuristicException, CanceledTaskException;

	/**
	 * Gets the current heuristic mode.
	 * @return the current heuristic mode.
	 */
	HeuristicMode getMode();

	/**
	 * Sets the heuristic mode.
	 * @param mode a String representing the name of the heuristic mode to set to. If the name is 
	 * not recognize, this method will throw an UnsupportedHeuristicModeException.
	 * @throws UnsupportedHeuristicModeException 
	 */
	void setMode(String mode) throws UnsupportedHeuristicModeException;

	/**
	 * Allows to configure the heuristic.
	 * @param properties ConfigurationProperties representing the main configuration file
	 * @throws LinkHeuristicException
	 */
	void configure(ConfigurationProperties properties) throws LinkHeuristicException;

	/**
	 * Aggregates weight from sameAs rule and diffFrom rule.
	 * @param source
	 * @param sameAsClue
	 * @param target
	 * @param diffFromClue
	 * @return a positive value if the aggregation should be interpreted as a co-reference clue or 
	 * a negative value if it should be interpreted as a non co-reference clue.
	 */
	int computeProximity(Reference source, Result sameAsClue, Reference target, Result diffFromClue);

	/**
	 * Check if a target does not appear in two sameAs link.
	 * This treatment cannot be called directly by the heuristic because it processes sources independently.
	 * @param newLinks
	 * @return
	 */
	Set<Link> checkAndHandleOneToNSameAsConflict(Set<Link> actualLinks);


}
