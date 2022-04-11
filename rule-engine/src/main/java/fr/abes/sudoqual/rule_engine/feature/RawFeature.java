/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.feature;

import java.util.function.Predicate;

import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;

/**
 * A raw feature is a formal declaration of an entry of data.
 * This kind of feature gives data as it is to {@link Criterion} or {@link Filter} instances.<br/>
 * <br/>
 * This interface also provides two static factory methods.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface RawFeature extends Feature {
	
	/**
	 * A static factory methods to declare a raw feature with the given key.
	 * @param key the feature key
	 * @return an instance of a RawFeature
	 */
	public static RawFeature create(String key) {
		return new RawFeatureImpl(key);
	}
	
	/**
	 * A static factory methods to declare a raw feature with the given key
	 * and the given {@link Predicate} function to check validity of input
	 * values.
	 * @param key the feature key
	 * @param checkValue a function to check validity of input values.
	 * @return an instance of a RawFeature
	 */
	public static RawFeature create(String key, Predicate<Object> checkValue) {
		return new RawFeatureImpl(key, checkValue);
	}

}
