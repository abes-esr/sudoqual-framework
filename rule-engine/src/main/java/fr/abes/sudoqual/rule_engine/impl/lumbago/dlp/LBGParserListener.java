package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.dlgp2.parser.ParserListener;

public abstract class LBGParserListener implements ParserListener {

	private static final Logger logger = LoggerFactory.getLogger(LBGParserListener.class);

	/** The waiting_for_var_list. */
	boolean firstConjunction = true, waiting_for_var_list = false;

	/** The current type. */
	protected OBJECT_TYPE currentType = null;

	/** The current builder. */
	protected Builder currentBuilder;

	@Override
	public void startsObject(OBJECT_TYPE objectType, String name) {
		waiting_for_var_list = false;
		currentType = objectType;
		firstConjunction = true;
		switch (objectType) {
			case RULE:
				currentBuilder = new RuleBuilder(name);
				break;
			default:
				this.exception("LBGParser does not support " + objectType);
				break;
		}
	}

	@Override
	public void createsAtom(Object predicate, Object[] terms) {
		this.createsLBGAtom(predicate.toString(), createTermArray(terms));
	}
	
	public void createsLBGAtom(String predicate, LBGTerm[] terms) {
		LBGAtom atom = new LBGAtom(predicate, terms);
		if (currentBuilder instanceof RuleBuilder) {
			if (firstConjunction)
				((RuleBuilder) currentBuilder).addAtomConclusion(atom);
			else
				((RuleBuilder) currentBuilder).addAtomHypothesis(atom);
		} else {
			((ConjunctionBuilder) currentBuilder).addAtom(atom);
		}
	}

	@Override
	public void answerTermList(Object[] terms) {
		waiting_for_var_list = false;
	}


	@Override
	public void endsConjunction(OBJECT_TYPE objectType) {
		if (objectType != currentType) {
			if (logger.isWarnEnabled()) {
				logger.warn("Annotation problem: expected type {}, actual type {}.", currentType, objectType);
			}
			currentType = objectType;
			currentBuilder = currentBuilder.transform();
		}

		if (objectType == OBJECT_TYPE.RULE && firstConjunction) {
			firstConjunction = false;
		} else {
			endOfStatement();
		}

	}

	protected void endOfStatement() {
		if (currentBuilder instanceof RuleBuilder) {
			ArrayList<LBGAtom> conclusions = ((RuleBuilder) currentBuilder).conclusionBuilder.listAtom;
			if (conclusions.size() > 1) {
				throw new RuntimeException("only one head is supported");
			}
			LBGAtom conclusion = conclusions.get(0);
			if (!(conclusion instanceof LBGAtom))
				throw new RuntimeException("unsupported head type: " + conclusion.getClass().getName());
			LBGRule statement = new LBGRule(currentBuilder.getName(), (LBGAtom) conclusion);
			for (LBGAtom atom : ((RuleBuilder) currentBuilder).hypothesisBuilder.listAtom) {
				statement.addHypAtom((LBGAtom) atom);
			}
			newRule(statement);
		} else {
			this.exception("Kind of statement not supported");
		}
	}

	protected abstract void newRule(LBGRule rule);

	protected abstract void exception(String msg);
	

	@Override
	public void createsEquality(Object term1, Object term2) {
		this.exception("LBGParser does not support equality atom");
	}

	@Override
	public void declarePrefix(String prefix, String ns) {
		this.exception("This implementation does not support @prefix");
	}

	@Override
	public void declareBase(String base) {
		this.exception("This implementation does not support @base");
	}

	@Override
	public void declareTop(String top) {
		this.exception("This implementation does not support @top");
	}

	@Override
	public void declareUNA() {
		this.exception("This implementation does not support @una");
	}

	@Override
	public void directive(String text) {
		if (logger.isWarnEnabled()) {
			logger.warn("This implementation does not support %% directive");
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private static LBGTerm createConstant(Object uri) {
		return new LBGTerm(LBGTerm.TERM_TYPE.CONSTANT, uri);
	}

	private static LBGTerm createTerm(Object t) {
		if (t instanceof LBGTerm) {
			return (LBGTerm) t;
		} else {
			return createConstant(t);
		}
	}

	private static LBGTerm[] createTermArray(Object[] terms) {
		LBGTerm[] lbgTerms = new LBGTerm[terms.length];
		for (int i = 0; i < terms.length; ++i) {
			lbgTerms[i] = createTerm(terms[i]);
		}
		return lbgTerms;
	}

	// /////////////////////////////////////////////////////////////////////////
	// INTERNAL CLASSES
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * The Interface Builder.
	 */
	interface Builder {

		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		String getName();

		/**
		 * Gets the type.
		 * 
		 * @return the type
		 */
		OBJECT_TYPE getType();

		/**
		 * Transform.
		 * 
		 * @return the builder
		 */
		Builder transform();

	}

	/**
	 * The Class ConjunctionBuilder.
	 */
	class ConjunctionBuilder implements Builder {

		/** The name. */
		protected String name;

		/** The list atom. */
		protected ArrayList<LBGAtom> listAtom = new ArrayList<LBGAtom>();

		/**
		 * Instantiates a new conjunction builder.
		 * 
		 * @param name
		 *                 the name
		 */
		protected ConjunctionBuilder(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#getType()
		 */
		@Override
		public OBJECT_TYPE getType() {
			return OBJECT_TYPE.FACT;
		}

		/**
		 * Adds the atom.
		 * 
		 * @param atom
		 *                 the atom
		 */
		void addAtom(LBGAtom atom) {
			listAtom.add(atom);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#transform()
		 */
		@Override
		public Builder transform() {
			RuleBuilder ruleBuilder = new RuleBuilder(name);
			for (LBGAtom atom : listAtom)
				ruleBuilder.addAtomConclusion(atom);
			return ruleBuilder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#getName()
		 */
		@Override
		public String getName() {
			return name;
		}
	}

	/**
	 * The Class RuleBuilder.
	 */
	protected class RuleBuilder implements Builder {

		/** The name. */
		private String name;

		/** The hypothesis builder. */
		protected ConjunctionBuilder hypothesisBuilder;

		/** The conclusion builder. */
		protected ConjunctionBuilder conclusionBuilder;

		/**
		 * Instantiates a new rule builder.
		 * 
		 * @param name
		 *                 the name
		 */
		RuleBuilder(String name) {
			this.name = name;
			hypothesisBuilder = new ConjunctionBuilder("hyp_" + name);
			conclusionBuilder = new ConjunctionBuilder("conc_" + name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#getType()
		 */
		@Override
		public OBJECT_TYPE getType() {
			return OBJECT_TYPE.RULE;
		}

		/**
		 * Adds the atom hypothesis.
		 * 
		 * @param atom
		 *                 the atom
		 */
		void addAtomHypothesis(LBGAtom atom) {
			hypothesisBuilder.addAtom(atom);
		}

		/**
		 * Adds the atom conclusion.
		 * 
		 * @param atom
		 *                 the atom
		 */
		void addAtomConclusion(LBGAtom atom) {
			conclusionBuilder.addAtom(atom);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#transform()
		 */
		@Override
		public Builder transform() {
			hypothesisBuilder.name = name;
			return hypothesisBuilder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.lirmm.graphik.qualinca.model.IFMC.Builder#getName()
		 */
		@Override
		public String getName() {
			return name;
		}
	}

}