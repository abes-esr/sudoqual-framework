package fr.abes.sudoqual.rule_engine.predicate.exception;

/**
 * This exception is designed to be throw by a Predicate to represent
 * a case were data seems to be invalid
 * and must be caught by the RuleEngine.
 * 
 * A predicate throwing this exception must be evaluated to false in any case.
 */
public class InconsistentException extends PredicateException {

	private static final long serialVersionUID = 5518414168812237358L;

}
