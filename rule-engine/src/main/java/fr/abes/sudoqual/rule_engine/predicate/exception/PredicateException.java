package fr.abes.sudoqual.rule_engine.predicate.exception;

/**
 * This exception is designed to be throw by a Predicate
 * and must be caught by the RuleEngine.
 * 
 * A predicate throwing this exception must be evaluated to false in any case.
 */
public abstract class PredicateException extends Exception {

	private static final long serialVersionUID = 1102404368274966096L;

}
