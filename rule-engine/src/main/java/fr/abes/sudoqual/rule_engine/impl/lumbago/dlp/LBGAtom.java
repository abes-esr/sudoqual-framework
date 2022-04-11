package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import java.util.Collections;
import java.util.List;

import fr.abes.sudoqual.rule_engine.Atom;

public class LBGAtom implements Atom {

	/** The terms. */
	private final List<LBGTerm> terms;

	/** The predicate. */
	private final String predicate;

	/**
	 * Instantiates a new standard atom.
	 * 
	 * @param predicate
	 *                      the predicate
	 * @param terms
	 *                      the terms
	 */
	public LBGAtom(String predicate, LBGTerm... terms) {
		this.terms = List.of(terms);
		
		if (predicate.startsWith("\"") && predicate.endsWith("\""))
			this.predicate = predicate.substring(1, predicate.length() - 1);
		else
			this.predicate = predicate;

	}
	
	public LBGAtom(String predicate, List<LBGTerm> terms) {
		this.terms = Collections.unmodifiableList(terms);
		
		if (predicate.startsWith("\"") && predicate.endsWith("\""))
			this.predicate = predicate.substring(1, predicate.length() - 1);
		else
			this.predicate = predicate;

	}

	/**
	 * Gets the arity.
	 * 
	 * @return the arity
	 */
	@Override
	public int getArity() {
		return terms.size();
	}

	/**
	 * Gets the term.
	 * 
	 * @param index
	 *                  the index
	 * @return the term
	 */
	@Override
	public LBGTerm getTerm(int index) {
		// terms is immutable (created by List.of)
		return terms.get(index);
	}
	
	@Override
	public List<LBGTerm> getTerms() {
		return this.terms;
	}

	/**
	 * Gets the predicate.
	 * 
	 * @return the predicate
	 */
	@Override
	public String getPredicate() {
		return predicate;
	}

	@Override
	public int hashCode() {
		int hc = predicate.hashCode();
		for (int i = 0; i < getArity(); i++)
			hc += getTerm(i).hashCode();
		return hc;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LBGAtom) {
			LBGAtom otherAtom = (LBGAtom) other;
			if (otherAtom.getPredicate().equals(predicate)) {
				if (otherAtom.getArity() == getArity()) {
					for (int i = 0; i < getArity(); i++)
						if (!getTerm(i).equals(otherAtom.getTerm(i))) {
							return false;
						}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String s = predicate + "(" + getTerm(0);
		for (int i = 1; i < getArity(); i++)
			s += "," + getTerm(i);
		s += ")";
		return s;
	}
}
