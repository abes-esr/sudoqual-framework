package fr.abes.sudoqual.rule_engine.impl.lumbago.dlp;

import fr.abes.sudoqual.rule_engine.Term;

public class LBGTerm implements Term {
	protected TERM_TYPE type;
	protected Object value;

	public static enum TERM_TYPE {
							 CONSTANT(false), VARIABLE(true), LITERAL(false), STRING(false), INTEGER(false);

		private boolean isVariable;

		TERM_TYPE(boolean isVariable) {
			this.isVariable = isVariable;
		}

		boolean isConstant() {
			return !this.isVariable;
		}

		boolean isVariable() {
			return this.isVariable;
		}
	}

	public LBGTerm(TERM_TYPE type, Object value) {
		if(value == null) {
			throw new IllegalArgumentException("Value must be not null.");
		}
		this.type = type;
		this.value = value;
	}

	public TERM_TYPE getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		switch (getType()) {
			case STRING:
				return "\"" + getValue() + "\"";
			default:
				return getValue().toString();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		LBGTerm t = (LBGTerm) o;
		return type == t.getType() && value.equals(t.getValue());
	}

	@Override
	public int hashCode() {
		return 17 * value.hashCode() + 37 * type.hashCode();
	}

}
