/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class CriterionUtils {
	
	 private CriterionUtils(){}
	 
	 public static <E> Set<E> intersection(final Iterable<? extends E> a, final Iterable<? extends E> b) {
		 	Set<E> set = new HashSet<>();

	        for(E ea : a) {
	        	for(E eb : b) {
	        		if(ea.equals(eb)) {
	        			set.add(ea);
	        			break;
	        		}
	        	}
	        }
	        
	        return set;
	    }
}
