/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.heuristic;

import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class Candidate {
	public Candidate(Reference target) {
		this.target = target;
	}
	public final Reference target;
	public RuleEngine.Result sameAsResult;
	public RuleEngine.Result diffFromResult;
	public int proximityClue;
	
}