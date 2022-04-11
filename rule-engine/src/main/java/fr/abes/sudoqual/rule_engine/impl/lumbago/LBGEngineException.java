package fr.abes.sudoqual.rule_engine.impl.lumbago;

import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;

public class LBGEngineException extends RuleEngineException {
	

	private static final long serialVersionUID = 5121561421380491232L;

	public LBGEngineException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public LBGEngineException(String msg) {
		super(msg);
	}
}
