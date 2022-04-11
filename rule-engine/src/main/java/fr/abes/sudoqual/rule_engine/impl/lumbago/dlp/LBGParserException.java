package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import fr.abes.sudoqual.rule_engine.impl.lumbago.LBGEngineException;

public class LBGParserException extends LBGEngineException {
	
	private static final long serialVersionUID = -2537647794500123465L;

	public LBGParserException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public LBGParserException(String msg) {
		super(msg);
	}

}
