package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGAtom;
import fr.lirmm.marel.gsh2.algorithm.AbstractAlgorithm;
import fr.lirmm.marel.gsh2.algorithm.Pluton;
import fr.lirmm.marel.gsh2.core.IMySet.Impl;
import fr.lirmm.marel.gsh2.core.MyBinaryContext;
import fr.lirmm.marel.gsh2.core.MyBottomUpIterator;
import fr.lirmm.marel.gsh2.core.MyConcept;
import fr.lirmm.marel.gsh2.core.MyConceptSet;
import fr.lirmm.marel.gsh2.core.MyGSH;
import fr.lirmm.marel.gsh2.core.MySetWrapper;
import fr.lirmm.marel.gsh2.io.DotWriter;
import fr.lirmm.marel.gsh2.io.DotWriter.DisplayFormat;

final class AOCPosetUtils {

	private AOCPosetUtils() {
	}

	static class AOCPosetData {
		private List<MyConcept> maximals;
		private Map<MyConcept, List<MyConcept>> lowerCovers;
		private MyBinaryContext binaryContext;
		private MyGSH gsh;

		public List<MyConcept> getMaximals() {
			return this.maximals;
		}

		public Map<MyConcept, List<MyConcept>> getLowerCovers() {
			return this.lowerCovers;
		}
	}
	
	public static AOCPosetData buildAOCPoset(List<LBGCombination> combinations, List<LBGAtom> computables) {
		MyBinaryContext mbc = buildMatrixForAOCPoset(combinations, computables);
		

		MySetWrapper.setDefaultImplementation(Impl.BITSET);
		mbc.changeImplementation(Impl.BITSET);
		return buildAOCPoset(mbc);
	}

	public static AOCPosetData buildAOCPoset(MyBinaryContext mbc) {
		// run algorithm
		AbstractAlgorithm algo = new Pluton(mbc);
		algo.run();
		MyGSH gsh = (MyGSH) algo.getResult();

		AOCPosetData data = new AOCPosetData();
		data.binaryContext = mbc;
		data.gsh = gsh;
		
		// populate lower cover of each concept
		data.maximals = getConceptList(gsh.getMaximals());
		data.lowerCovers = new HashMap<MyConcept, List<MyConcept>>();
		for (MyBottomUpIterator it = gsh.getBottomUpIterator(); it.hasNext();) {
			MyConcept concept = it.next();
			data.lowerCovers.put(concept, getConceptList(concept.getLowerCover()));
		}
		
		return data;
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private static MyBinaryContext buildMatrixForAOCPoset(List<LBGCombination> combinations, List<LBGAtom> computables) {
		MyBinaryContext mbc = new MyBinaryContext(combinations.size(), computables.size(), "");
		for (LBGAtom attr : computables)
			mbc.addAttributeName(attr.toString());
		for (int numobj = 0; numobj < combinations.size(); numobj++) {
			mbc.addObjectName("" + numobj);
			BitSet conj = combinations.get(numobj).combination;
			for (int i = conj.nextSetBit(0); i != -1; i = conj.nextSetBit(i + 1)) {
				mbc.set(numobj, i, true);
			}
		}
		return mbc;
	}

	public static void writeDotFile(File file, AOCPosetData data, DisplayFormat format) throws IOException {
		DotWriter dw = new DotWriter(
			new BufferedWriter(new FileWriter(file)),
			data.gsh, data.binaryContext, format, true);
		dw.write();
	}
	
	/**
	 * Create a collection of atoms corresponding to the ones declared by the bit-set representation.
	 * @param computables
	 * @param combination
	 * @return
	 */
	public static Collection<LBGAtom> combinationToString(List<LBGAtom> computables, BitSet combination) {
		List<LBGAtom> atoms = new ArrayList<>();
		for (int i = combination.nextSetBit(0); i >= 0; i = combination.nextSetBit(i + 1)) {
			atoms.add(computables.get(i));
		}
		return atoms;
	}

	private static List<MyConcept> getConceptList(MyConceptSet concepts) {
		return new ArrayList<MyConcept>(concepts.values());
	}
	
	

}
