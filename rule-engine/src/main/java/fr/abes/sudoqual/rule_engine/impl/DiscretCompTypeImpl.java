/*
* This file is part of SudoQual project.
*/
package fr.abes.sudoqual.rule_engine.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;

/**
 * The Class DefaultDiscretCompType.
 */
public class DiscretCompTypeImpl implements DiscretCompType {

	/** The max value. zero if no positive value */
	private int max;
	/** The min value. zero if no negative value */
	private int min;
	
	private List<String> acceptableValues;

	/** The has never. */
	private boolean hasNeutral, hasAlways, hasNever;

	/**
	 * Instantiates a new default discret comp type.
	 *
	 * @param hasNever
	 *            the has never
	 * @param min
	 *            the min
	 * @param hasNeutral
	 *            the has neutral
	 * @param max
	 *            the max
	 * @param hasAlways
	 *            the has always
	 */
	public DiscretCompTypeImpl(boolean hasNever, int min, boolean hasNeutral, int max, boolean hasAlways) {
		this.max = max;
		this.min = min;
		this.hasNeutral = hasNeutral;
		this.hasAlways = hasAlways;
		this.hasNever = hasNever;
		this.acceptableValues = new LinkedList<>();
		if(this.hasNeutral) {
			this.acceptableValues.add(NEUTRAL_KEY);
		}
		if(this.hasAlways) {
			this.acceptableValues.add(ALWAYS_KEY);
		}
		if(this.hasNever) {
			this.acceptableValues.add(NEVER_KEY);
		}
		this.acceptableValues = Collections.unmodifiableList(this.acceptableValues);
	}
	
	
	
	@Override
	public List<String> acceptableValues() {
		return this.acceptableValues;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lirmm.graphik.qualinca.model.IDiscretCompType#getMaxValue()
	 */
	@Override
	public int getMaxValue() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lirmm.graphik.qualinca.model.IDiscretCompType#getMinValue()
	 */
	@Override
	public int getMinValue() {
		return min;
	}

	/**
	 * Checks for positive values.
	 * 
	 * @return true, if successful
	 */
	public boolean hasPositiveValues() {
		return max > 0;
	}

	/**
	 * Checks for negative value.
	 * 
	 * @return true, if successful
	 */
	public boolean hasNegativeValue() {
		return min < 0;
	}
	
	@Override
	public boolean check(int value) {
		switch (value) {
		case NOT_COMPARABLE:
		case INCOHERENT:
			return true;
		case NEUTRAL:
			return hasNeutral;
		case ALWAYS:
			return hasAlways;
		case NEVER:
			return hasNever;
		}
		if (value < 0)
			return value >= min;
		else
			return value <= max;
	}
	
	@Override
	public boolean check(String value) {
		switch (value) {
		case DiscretCompType.NOT_COMPARABLE_KEY:
		case DiscretCompType.INCOHERENT_KEY:
			return true;
		case DiscretCompType.NEUTRAL_KEY:
			return hasNeutral;
		case DiscretCompType.ALWAYS_KEY:
			return hasAlways;
		case DiscretCompType.NEVER_KEY:
			return hasNever;
		default:
			return false;
		}
	}
	
	@Override
	public int toInt(String value) throws RuleEngineException {
		switch (value) {
			case DiscretCompType.NOT_COMPARABLE_KEY:
				return DiscretCompType.NOT_COMPARABLE;
			case DiscretCompType.INCOHERENT_KEY:
				return DiscretCompType.INCOHERENT;
			case DiscretCompType.NEUTRAL_KEY:
				return DiscretCompType.NEUTRAL;
			case DiscretCompType.ALWAYS_KEY:
				return DiscretCompType.ALWAYS;
			case DiscretCompType.NEVER_KEY:
				return DiscretCompType.NEVER;
			default:
				throw new RuleEngineException("wrong value");
			}
	}
	
	@Override 
	public String toString(int value) throws RuleEngineException {
		switch (value) {
		case DiscretCompType.NOT_COMPARABLE:
			return DiscretCompType.NOT_COMPARABLE_KEY;
		case DiscretCompType.INCOHERENT:
			return DiscretCompType.INCOHERENT_KEY;
		case DiscretCompType.NEUTRAL:
			return DiscretCompType.NEUTRAL_KEY;
		case DiscretCompType.ALWAYS:
			return DiscretCompType.ALWAYS_KEY;
		case DiscretCompType.NEVER:
			return DiscretCompType.NEVER_KEY;
		default:
			throw new RuleEngineException("wrong value");
		}
	}	

}
