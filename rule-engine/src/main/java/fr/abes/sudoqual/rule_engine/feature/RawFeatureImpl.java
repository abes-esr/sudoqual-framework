/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.rule_engine.feature;

import java.util.function.Predicate;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class RawFeatureImpl implements RawFeature {

	
	private String key;
	private Predicate<Object> checkValue;

	public RawFeatureImpl(String key) {
		this(key, x -> x != null);
	}
	
	public RawFeatureImpl(String key, Predicate<Object> checkValue) {
		this.key = key;
		this.checkValue = checkValue;
	}

	@Override
	public String getKey() {
		return this.key;
	}
	
	@Override
	public boolean checkValue(Object object) {
		return this.checkValue.test(object);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RawFeatureImpl other = (RawFeatureImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
}
