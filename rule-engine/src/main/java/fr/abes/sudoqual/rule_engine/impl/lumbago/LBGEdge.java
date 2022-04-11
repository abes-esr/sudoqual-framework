package fr.abes.sudoqual.rule_engine.impl.lumbago;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGAtom;
import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm;

class LBGEdge {

	private static final Logger logger = LoggerFactory.getLogger(LBGEdge.class);
	
	HashMap<LBGAtom,Boolean> results=new HashMap<LBGAtom,Boolean>();
	HashMap<String,Object> computedValues=new HashMap<String,Object>();
	/**
	 * Instantiates a new criterion edge.
	 * 
	 * @param criterion
	 *            the criterion
	 * @param value
	 *            the value
	 */
	public LBGEdge() {
	}
	public void addResult(String predicate,Object value)
	{
		computedValues.put(predicate,value);
	}
	public void addResult(LBGAtom atom,boolean value)
	{
		results.put(atom,value);
	}
	public boolean isComputed(LBGAtom atom)
	{
		return results.containsKey(atom) || computedValues.containsKey(atom.getPredicate());
	}
	public boolean check(LBGAtom atom)
	{
		Boolean result=results.get(atom);
		if(result==null)
		{
			Object o=computedValues.get(atom.getPredicate());
			if(o!=null )
			{
				boolean ret;
				// criterion inheritance
				if(o instanceof Integer)
				{
					Integer computedVal=(Integer)o;
					 if(computedVal== DiscretCompType.NOT_COMPARABLE) return false;
					 if(computedVal==DiscretCompType.INCOHERENT) return true;
					 Integer newValue;
					 if(atom.getTerm(2).getType()==LBGTerm.TERM_TYPE.CONSTANT)
					 {
						 if("never".equals(atom.getTerm(2).getValue()))
						 {
							 newValue=DiscretCompType.NEVER;
						 }
						 else if("always".equals(atom.getTerm(2).getValue()))
						 {
							 newValue=DiscretCompType.ALWAYS;
						 }
						 else if("neutral".equals(atom.getTerm(2).getValue()))
						 {
							 newValue=DiscretCompType.NEUTRAL;
						 }
						 else {
							 if(logger.isWarnEnabled()) {
								 logger.warn("unknown constant for criterion value {}", atom);
							 }
							 return false;
						 }
					 }
					 else if(atom.getTerm(2).getType()==LBGTerm.TERM_TYPE.INTEGER)
						 newValue=(Integer)atom.getTerm(2).getValue();
					 else
					 {
    					 if(logger.isWarnEnabled()) {
    						 logger.warn("unknown parameter for criterion value {}", atom);
    					 }
    					 return false;
					 }
					 if(computedVal==DiscretCompType.NOT_COMPARABLE || newValue==DiscretCompType.NOT_COMPARABLE)
						 new Exception().printStackTrace();
					 if(computedVal<0)
					 {
						if(newValue<0) ret=newValue>=computedVal;
						else ret=false;
					 }
					 else
					 {
						if(newValue>=0)	ret=computedVal>=newValue;
						else ret=false;
					 }
				}
				else 
				{
					if(logger.isWarnEnabled()) {
						 logger.warn("compare {} to {}", o ,atom.getTerm(2).getValue());
					}
					ret=o.equals(atom.getTerm(2).getValue());
				}
				addResult(atom,ret);
				return ret;
			}
			else 
			{
				if(logger.isWarnEnabled()) {
					 logger.warn("{} is checked but not computed", atom.getPredicate());
				}
				return false; 
			}
		}
		else return result;			
	}
	public void cleanAttributes(Set<String> toRemove) {
		for(String predicate:toRemove)
		{
			computedValues.remove(predicate);
			ArrayList<LBGAtom> list=new ArrayList<LBGAtom>();
			for(LBGAtom atom:results.keySet())
				if(toRemove.contains(atom.getPredicate())) list.add(atom);
			for(LBGAtom atom:list) 
				{
				results.remove(atom);
				}
		}
	}
}
