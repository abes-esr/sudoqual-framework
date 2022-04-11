/*
* This file is part of SudoQual project.
* Created in 2018-08.
*/
package fr.abes.sudoqual.rule_engine.impl;

import java.util.Objects;

import fr.abes.sudoqual.rule_engine.Reference;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class ReferenceImpl implements Reference {

	private String uri;
	private int id;

	public ReferenceImpl(String uri) {
		this.uri = uri;
		this.id = Reference.super.getId();
	}
	
	@Override
	public String getName() {
		return this.uri;
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o)
	        return true;
	    if (o == null || getClass() != o.getClass())
	        return false;

	    ReferenceImpl ref = (ReferenceImpl) o;
	    return Objects.equals(this.uri, ref.uri);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}


}
