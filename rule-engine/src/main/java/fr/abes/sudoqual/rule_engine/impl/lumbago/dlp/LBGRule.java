package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import java.util.ArrayList;

import fr.abes.sudoqual.rule_engine.Rule;

public class LBGRule implements Rule {
	/** The hypothesis . */
	ArrayList<LBGAtom> hyp=new ArrayList<LBGAtom>();
	String ruleName;
	LBGAtom conclusion;
	
	public LBGRule(String ruleName,LBGAtom conclusion) {
		this.ruleName=ruleName;
		this.conclusion=conclusion;
	}
	
	public LBGRule(String ruleName, LBGAtom conclusion, LBGAtom hypAtom) {
		this.ruleName=ruleName;
		this.conclusion=conclusion;
		this.hyp.add(hypAtom);
	}
	
	@Override
	public LBGAtom getConclusion()
	{
		return conclusion;
	}
	
	@Override
	public ArrayList<LBGAtom> getHypothesis() {
		return this.hyp;
	}
	
	public void addHypAtom(LBGAtom hypAtom) {
		hyp.add(hypAtom);
	}
	
	/**
	 * Returns true iff the hypothesis contains the conclusion atom.
	 * @return true iff the hypothesis contains the conclusion atom.
	 */
	public boolean isCyclic() {
		return this.getHypothesis().contains(this.getConclusion());
	}
	
	@Override
	public String getName() {
		return this.ruleName;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(ruleName != null) {
    		sb.append('[')
    		.append(ruleName)
    		.append("] ");
		}
		sb.append(conclusion)
		.append(" :- ");
		boolean isFirst = true;
		for(LBGAtom atom : hyp) {
			if(!isFirst) {
				sb.append(',');
			}
			isFirst = false;
			sb.append(atom);
		}
		sb.append('.');
		return sb.toString();
	}
}
