package fr.abes.sudoqual.rule_engine.features;

public class Interval {
	
	public final int min;
	public final int max;
	
	public Interval(int min, int max) {
		if(max < min) {
			throw new IllegalArgumentException();
		}
		this.min = min;
		this.max = max;
	}
	
	@Override
	public String toString() {
		return "Interval [min=" + min + ", max=" + max + "]";
	}

	
}
