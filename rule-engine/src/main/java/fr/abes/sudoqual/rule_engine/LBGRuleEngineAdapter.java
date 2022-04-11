/*
* This file is part of SudoQual project.
* Created in 2018-08 by Clément SIPIETER.
*/
package fr.abes.sudoqual.rule_engine;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;

import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.impl.lumbago.LBGEngine;
import fr.abes.sudoqual.rule_engine.impl.lumbago.LBGEngineException;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

/**
 * This class implements {@link RuleEngine} by adapting the {@link LBGEngine}.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class LBGRuleEngineAdapter implements RuleEngine {

	private final LBGEngine adaptee;
	
	// /////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////

	public LBGRuleEngineAdapter(Reader ruleSet, Collection<Predicate> predicates)
	    throws RuleEngineException {
		super();
		try {
			this.adaptee = LBGEngine.create(ruleSet, predicates);
		} catch (Exception e) {
			throw new RuleEngineException("LBGEngine exception during creation.", e);
		}

	}
	
	// /////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////

	@Override
	public Result check(PredicateManager store, String queryAtom, Reference from, Reference to) throws RuleEngineException {
		return this.check(store, queryAtom, from, to, Integer.MIN_VALUE);
	}

	@Override
	public Result check(PredicateManager store, String queryAtom, Reference from, Reference to, int minThreshold) throws RuleEngineException {
		try {
			return this.adaptee.getMax(store, queryAtom, from, to, minThreshold);
		} catch (LBGEngineException e) {
			throw new RuleEngineException("LBGEngine exception: ", e);
		}
	}
	
	private static Collection<ComputedFeature<?>> keepOnlyComputed(Collection<Feature> features) {
		Collection<ComputedFeature<?>> result = new LinkedList<ComputedFeature<?>>();
		for(Feature f : features) {
			if(f instanceof ComputedFeature) {
				result.add((ComputedFeature<?>)f);
			}
		}
		return result;
	}
	
	
}
