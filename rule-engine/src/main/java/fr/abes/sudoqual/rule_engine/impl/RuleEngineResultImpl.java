/*
* This file is part of SudoQual project.
* Created in 2018-08 by Clément SIPIETER.
*/
package fr.abes.sudoqual.rule_engine.impl;

import java.util.List;

import fr.abes.sudoqual.rule_engine.Rule;
import fr.abes.sudoqual.rule_engine.RuleEngine;

/** 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class RuleEngineResultImpl implements RuleEngine.Result {

	private final List<Rule> why;
	private final int value;

	public RuleEngineResultImpl(int value, List<Rule> why) {
		super();
		this.why = why;
		this.value = value;
	}
	
	@Override
	public List<Rule> why() {
		return why;
	}

	@Override
	public int value() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value)
		.append(" because of ")
		.append(why);
		return sb.toString();
	}
}
