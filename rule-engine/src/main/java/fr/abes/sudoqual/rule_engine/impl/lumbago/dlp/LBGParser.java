package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import java.io.Reader;

import fr.abes.sudoqual.rule_engine.impl.lumbago.dlp.LBGTerm.TERM_TYPE;
import fr.lirmm.graphik.dlgp2.parser.DLGP2Parser;
import fr.lirmm.graphik.dlgp2.parser.ParseException;
import fr.lirmm.graphik.dlgp2.parser.TermFactory;


public class LBGParser {
	
	/**
	 * Parses GraphiK Datalog.
	 * 
	 * @param reader
	 *            the reader
	 * @throws Throwable
	 *             the throwable
	 */
	public void parse(Reader reader, LBGParserListener listener) throws ParseException {
		DLGP2Parser parser = new DLGP2Parser(new LBGTermFactory(), reader);
		parser.addParserListener(listener);
		parser.setDefaultBase("");
		parser.document();
	}

	private static class LBGTermFactory implements TermFactory {

		@Override
		public Object createIRI(String s) {
			return s;
		}

		@Override
		public LBGTerm createLiteral(Object datatype, String stringValue, String langTag) {
			if(datatype == "http://www.w3.org/2001/XMLSchema#integer") {
				return createTerm(LBGTerm.TERM_TYPE.INTEGER, Integer.parseInt(stringValue));
			} else if(datatype == "http://www.w3.org/2001/XMLSchema#string") {
				return createTerm(LBGTerm.TERM_TYPE.STRING, Integer.parseInt(stringValue));
			}
			throw new RuntimeException("LBGParser does not support following datatype: "+ datatype);
		}

		@Override
		public LBGTerm createVariable(String stringValue) {
			return createTerm(LBGTerm.TERM_TYPE.VARIABLE, stringValue);
		}
		
		private LBGTerm createTerm(final TERM_TYPE termType, final Object term) {
			return new LBGTerm(termType, term);
		}
		
	}
	
	



}
