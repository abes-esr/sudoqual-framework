/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.feature;

import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;

/**
 * A {@link PreprocessedFeature} is a special kind of {@link Feature} which
 * can be cleaned or reshaped before being used by a {@link Criterion} or a {@link Filter}.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface PreprocessedFeature<FROM, TO> extends Feature {
	
	/**
	 * Builds the final value based on the provided raw value. The final value will be
	 * used by filters and criterions.
	 * @param rawValue
	 * @return the final value.
	 */
	TO buildValue(FROM rawValue);
	
	/**
	 * Gets raw feature name on which this one is built.
	 * @return a raw feature name.
	 */
	default String getRelatedRawFeature() {
		return this.getKey();
	}
}
