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
public class ConstantCompType implements DiscretCompType {
	
	private boolean hasNeutral;
	private List<String> negativeValues;
	private List<String> positiveValues;
	private List<String> acceptableValues;

	/**
	 * negativeValues and positiveValues must be disjoint and does not contains "neutral", "not_comparable" or "incoherent".
	 * @param negativeValues
	 * @param hasNeutral
	 * @param positiveValues
	 */
	public ConstantCompType(List<String> negativeValues, boolean hasNeutral, List<String> positiveValues) {
		this.negativeValues = (negativeValues != null)? Collections.unmodifiableList(negativeValues) : Collections.emptyList();
		this.positiveValues = (positiveValues != null)? Collections.unmodifiableList(positiveValues) : Collections.emptyList();
		this.hasNeutral = hasNeutral;
		
		if(!Collections.disjoint(negativeValues, positiveValues)) {
			throw new IllegalArgumentException("negativeValues & positiveValues must be disjoint");
		}
		this.acceptableValues = new LinkedList<>();
		this.acceptableValues.addAll(negativeValues);
		if(this.hasNeutral) {
			this.acceptableValues.add(DiscretCompType.NEUTRAL_KEY);
		}
		this.acceptableValues.addAll(positiveValues);
		
		if(acceptableValues.contains(DiscretCompType.NEUTRAL_KEY)
				|| acceptableValues.contains(DiscretCompType.INCOHERENT_KEY)
				|| acceptableValues.contains(DiscretCompType.NOT_COMPARABLE)) {
			throw new IllegalArgumentException("negativeValue & positiveValues can't contain \"neutral\", \"not_comparable\" or \"incoherent\".");
		}
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
		return this.positiveValues.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lirmm.graphik.qualinca.model.IDiscretCompType#getMinValue()
	 */
	@Override
	public int getMinValue() {
		return - this.negativeValues.size();
	}
	
	@Override
	public boolean check(int value) {
		switch (value) {
		case NOT_COMPARABLE:
		case INCOHERENT:
			return true;
		case NEUTRAL:
			return hasNeutral;
		}
		if (value < 0)
			return value >= getMinValue();
		else
			return value <= getMaxValue();
	}
	
	@Override
	public boolean check(String value) {
		switch (value) {
		case DiscretCompType.NOT_COMPARABLE_KEY:
		case DiscretCompType.INCOHERENT_KEY:
			return true;
		default:
			return this.acceptableValues.contains(value);
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
				if(hasNeutral) {
					return DiscretCompType.NEUTRAL;
				} else {
					throw new RuleEngineException("wrong value");
				}
			default:
				int idx = this.positiveValues.indexOf(value);
				if(idx >= 0) {
					return idx + 1;
				} else {
					idx = this.negativeValues.indexOf(value);
					if(idx >= 0) {
						return -(idx + 1);
					} else {
						throw new RuleEngineException("wrong value");
					}
				}
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
				if(hasNeutral) {
					return DiscretCompType.NEUTRAL_KEY;
				} else {
					throw new RuleEngineException("wrong value");
				}
			default:
				try {
					return this.positiveValues.get(value - 1);
				} catch(IndexOutOfBoundsException e) {
					// do nothing
				}
				try {
					return this.negativeValues.get(-value - 1);
				} catch(IndexOutOfBoundsException e) {
					// do nothing
				}
				throw new RuleEngineException("wrong value");
			}
	}	

}
