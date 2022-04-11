/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine;

import java.util.List;

/**
 * An interface representing a logical rule.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>} *
 */
public interface Rule {

	/**
	 * Gets the name of this rule.
	 * @return the name of this rule (a.k.a label)
	 */
	String getName();

	Atom getConclusion();
	
	List<? extends Atom> getHypothesis();
}
