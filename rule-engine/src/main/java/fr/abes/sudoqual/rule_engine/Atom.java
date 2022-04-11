package fr.abes.sudoqual.rule_engine;

import java.util.List;


public interface Atom {


	int getArity();

	Term getTerm(int index);
	
	List<? extends Term> getTerms();

	String getPredicate();
}
