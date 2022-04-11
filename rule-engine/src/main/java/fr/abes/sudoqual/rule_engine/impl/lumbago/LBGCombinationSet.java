package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.util.HashSet;

class LBGCombinationSet {
	HashSet<LBGCombination> setObjects;

	public LBGCombinationSet() {
		setObjects = new HashSet<LBGCombination>();
	}

	protected LBGCombinationSet(HashSet<LBGCombination> setObjects) {
		this.setObjects = setObjects;
	}

	public void add(LBGCombination combi2) {
		setObjects.add(combi2);
	}

	public LBGCombinationSet and(LBGCombinationSet m) {
		HashSet<LBGCombination> objSet = new HashSet<LBGCombination>();
		try {
			for (LBGCombination b : setObjects) {
				for (LBGCombination b2 : m.setObjects) {
					LBGCombination andCombi = new LBGCombination(b);
					andCombi.or(b2);
					objSet.add(andCombi);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new LBGCombinationSet(objSet);
	}

	public LBGCombinationSet or(LBGCombinationSet m) {
		HashSet<LBGCombination> objSet = new HashSet<LBGCombination>();
		objSet.addAll(setObjects);
		objSet.addAll(m.setObjects);
		return new LBGCombinationSet(objSet);
	}
}
