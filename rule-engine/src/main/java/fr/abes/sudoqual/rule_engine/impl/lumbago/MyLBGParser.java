package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.io.Reader;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.SimpleDirectedGraph;

import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGParser;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGParserException;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGParserListener;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGRule;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm.TERM_TYPE;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;

/** the datalog parser */
class MyLBGParser {
	
	private final LBGParser effectiveParser;
	private final MyLBGParserListener listener;
	
	public MyLBGParser(SimpleDirectedGraph<Object, Object> ruleGraph, 
		Map<String, SortedSet<Integer>> dimensionValues,
		Map<String, Integer> dimensionArity,
		Map<String, Filter> filters, 
		Map<String, Criterion> criterions) {
		this.effectiveParser = new LBGParser();
		this.listener = new MyLBGParserListener(ruleGraph, dimensionValues, dimensionArity, filters, criterions);
		
	}
	
	public void parse(Reader reader) throws LBGParserException {
		try { 
			this.effectiveParser.parse(reader, this.listener);
		} catch (Exception e) {
			throw new LBGParserException("Parsing error.", e);
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	
	// /////////////////////////////////////////////////////////////////////////

	
	private static class MyLBGParserListener extends LBGParserListener {
		
		private final SimpleDirectedGraph<Object, Object> ruleGraph;
		private final Map<String, SortedSet<Integer>> dimensionValues;
		private final Map<String, Integer> dimensionArity;
		private final Map<String, Filter> filters;
		private final Map<String, Criterion> criterions;
		
		public MyLBGParserListener(SimpleDirectedGraph<Object, Object> ruleGraph, 
			Map<String, SortedSet<Integer>> dimensionValues,
			Map<String, Integer> dimensionArity, 
			Map<String, Filter> filters, 
			Map<String, Criterion> criterions) {
			this.ruleGraph = ruleGraph;
			this.dimensionValues = dimensionValues;
			this.dimensionArity = dimensionArity;
			this.filters = filters;
			this.criterions = criterions;
		}
		
		@Override
		protected void exception(String msg) {
			throw new RuntimeException(msg);
		}

    	@Override
    	protected void newRule(LBGRule rule) {
    		try {
    			LBGUtils.addRule(ruleGraph, rule);
    		} catch (LBGEngineException e) {
    			throw new RuntimeException("Error during rule parsing: " + rule, e);
    		}
    	}
    	
    	@Override
    	public void createsLBGAtom(String predicate, LBGTerm[] terms) {
    		super.createsLBGAtom(predicate, terms);
    		try {
    			handleDimensions(predicate, terms);
    		} catch(LBGEngineException e) {
    			throw new RuntimeException(e);
    		}
    	}
    	
    	// /////////////////////////////////////////////////////////////////////////
    	//	
    	// /////////////////////////////////////////////////////////////////////////
    	
    	private void handleDimensions(String predicate, LBGTerm[] terms) throws LBGEngineException {
    		if(!filters.containsKey(predicate) && !criterions.containsKey(predicate)) {
    			if(terms.length == 0 || 
    					(terms[terms.length - 1].getType() != TERM_TYPE.CONSTANT
    					&& terms[terms.length - 1].getType() != TERM_TYPE.INTEGER)) {
    				throw new LBGEngineException("Undeclared predicate: " + predicate);
    			}
    			SortedSet<Integer> values = this.dimensionValues.get(predicate);
    			if(values == null) {
    				values = new TreeSet<>();
    				dimensionValues.put(predicate, values);
    			}
    			values.add(LBGUtils.getValueOf(terms[terms.length - 1]));
    			Integer previousArity = this.dimensionArity.put(predicate, terms.length);
    			if(previousArity != null && !previousArity.equals(terms.length)) {
    				throw new LBGEngineException("Multiple arities for predicate: " + predicate);
    			}
    		}
    	}
    	
	}

}
