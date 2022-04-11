package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGRule;

/**
 * The Class LBGCombination. This class represents a combination of computables
 * to conclude a certain atom is true (i.e. filters and/or criterions
 * combinations)
 */
class LBGCombination {

	/** The combination. */
	protected BitSet combination;

	/** The rules to trace the combination origin. */
	protected HashSet<LBGRule> rules;

	/**
	 * Instantiates a new LBG combination.
	 *
	 * @param init the init
	 */
	public LBGCombination(LBGCombination init) {
		this.combination = (BitSet) init.combination.clone();
		rules = new HashSet<LBGRule>();
		rules.addAll(init.rules);
	}

	/**
	 * Instantiates a new LBG combination.
	 */
	public LBGCombination() {
		this.combination = new BitSet();
		rules = new HashSet<LBGRule>();
	}

	/**
	 * Or.
	 *
	 * @param combination2 the combination2
	 * @throws Exception the exception
	 */
	public void or(LBGCombination combi) throws Exception {
		combination.or(combi.combination);
		rules.addAll(combi.getRuleTrace());
	}

	/**
	 * Adds a computable num to combination.
	 *
	 * @param numComputable the num of the computable
	 */
	public void add(int numC) {
		combination.set(numC);
	}

	/**
	 * Adds the rule.
	 *
	 * @param rule the rule
	 */
	public void addRuleTrace(LBGRule rule) {
		rules.add(rule);
	}

	public Set<LBGRule> getRuleTrace() {
		return rules;
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = combination.nextSetBit(0); i != -1; i = combination.nextSetBit(i + 1))
			s += "," + i;
		return s;
	}
}
