package fr.abes.sudoqual.rule_engine.predicate.exception;

/**
 * This exception is designed to be throw by a Predicate to represent
 * a case were needed data are missing
 * and must be caught by the RuleEngine.
 * 
 * A predicate throwing this exception must be evaluated to false in any case.
 */
public class NotComparableException extends PredicateException {

	private static final long serialVersionUID = -4719696718233604817L;

}
