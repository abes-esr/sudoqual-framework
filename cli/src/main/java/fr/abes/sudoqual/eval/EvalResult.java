package fr.abes.sudoqual.eval;

public class EvalResult {
	private int good;
	private int careful;
	private int unsatisfactory;
	private int bad;
	private int warn;
	
	public EvalResult() {
		this(0,0,0,0,0);
	}

	public EvalResult(int good, int careful, int unsatisfactory, int bad, int warn) {
		this.good = good;
		this.careful = careful;
		this.unsatisfactory = unsatisfactory;
		this.bad = bad;
		this.warn = warn;
	}

	public void add(EvalResult other) {
		this.good += other.good;
		this.careful += other.careful;
		this.unsatisfactory += other.unsatisfactory;
		this.bad += other.bad;
		this.warn += other.warn;
	}
	
	public int getGood() {
		return this.good;
	}
	
	public void incrGood() {
		++this.good;
	}
	
	public int getCareful() {
		return this.careful;
	}
	
	public void incrCareful() {
		++this.careful;
	}
	
	public int getUnsatisfactory() {
		return this.unsatisfactory;
	}
	
	public void incrUnsatisfactory() {
		++this.unsatisfactory;
	}
	
	public int getBad() {
		return this.bad;
	}
	
	public void incrBad() {
		++this.bad;
	}
	
	public void incrWarn() {
		++this.warn;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n          good: ");
		sb.append(this.good);
		sb.append("\n       careful: ");
		sb.append(this.careful);
		sb.append("\nunsatisfactory: ");
		sb.append(this.unsatisfactory);
		sb.append("\n           bad: ");
		sb.append(this.bad);
		sb.append('\n');
		sb.append("\n          warn: ");
		sb.append(this.warn);
		sb.append('\n');
		return sb.toString();
	}

	
}
