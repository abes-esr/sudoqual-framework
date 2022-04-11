/*
* This file is part of SudoQual project.
* Created in 2018-08 by Clément SIPIETER.
*/
package fr.abes.sudoqual.rule_engine;

import java.io.Reader;
import java.util.Collection;
import java.util.List;

import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

/**
 * Defines needed methods for a RuleEngine and provides a static factory methods {@link #create(Reader, Collection, Collection)}. 
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface RuleEngine {
	
	/**
	 * Creates a RuleEngine from a rule-set and a set of computable predicates.
	 * @param ruleSet a {@link Reader} on a rule file in the specified subset of the DLGP format. See rule-engine documentation for more details
	 * about the rule format.
	 * @param predicates a collection of computable predicates.
	 * @return a RuleEngine based on the given rule-set and set of computable predicates.
	 * @throws RuleEngineException if there is something wrong during the building of the RuleEngine.
	 */
	public static RuleEngine create(Reader ruleSet, Collection<Predicate> predicates) throws RuleEngineException {
		return new LBGRuleEngineAdapter(ruleSet, predicates);
	}

	/**
	 * Asks the rule engine for the max value computed between source and target references for the specified predicate.
	 * 
	 * @param store a PredicateManager containing data about source and target and on which computable predicates will be evaluated.
	 * @param predicateToQuery the predicate to query (e.g. sameAs or diffFrom)
	 * @param source the source reference
	 * @param target the target reference
	 * @return the max value found and some information about used rules as a {@RuleEngineResult}.
	 * @throws RuleEngineException if something wrong happened.
	 */
	Result check(PredicateManager store, String predicateToQuery, Reference source, Reference target) throws RuleEngineException;

	/**
	 * Asks the rule engine for the max value computed between source and target references for the specified predicate. This
	 * method allows the rule engine to does not compute values under (strictly) the given minThreshold.
	 * 
	 * @param store a PredicateManager containing data about source and target and on which computable predicates will be evaluated.
	 * @param predicateToQuery the predicate to query (e.g. sameAs or diffFrom)
	 * @param source the source reference
	 * @param target the target reference
	 * @param minThreshold
	 * @return the max value found and some information about used rules as a {@RuleEngineResult}.
	 * @throws RuleEngineException if something wrong happened.
	 */
	Result check(PredicateManager store, String predicateToQuery, Reference source, Reference target, int minThreshold) throws RuleEngineException;

	/**
	 * Represents the output of a call to {@link RuleEngine#check(PredicateManager, String, Reference, Reference)}.
	 * 
	 * @author Clément Sipieter {@literal <clement@6pi.fr>}
	 */
	public interface Result {

		/**
		 * @return a list of rules used to find the associated value.
		 */
		List<Rule> why();

		/**
		 * @return the found value
		 */
		int value();

	}
}
