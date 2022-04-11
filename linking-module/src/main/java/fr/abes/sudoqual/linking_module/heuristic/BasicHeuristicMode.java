/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.heuristic;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public enum BasicHeuristicMode implements HeuristicMode {
	ONE_TO_ONE,
	ONE_TO_MANY,
	MANY_TO_ONE,
	MANY_TO_MANY 
}
