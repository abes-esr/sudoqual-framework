package fr.abes.sudoqual.rule_engine.features;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;

public abstract class AComputedFeatureImpl<T> implements ComputedFeature<T> {

	private final String name;
	private final Set<String> relatedFeatures;

	public AComputedFeatureImpl(String name, Collection<String> relatedFeatures) {
		this.name = name;
		this.relatedFeatures = Collections.<String>unmodifiableSet(new HashSet<String>(relatedFeatures));	}

	@Override
	public String getKey() {
		return this.name;
	}
	
	@Override
	public Set<String> getRelatedFeatures() {
		return this.relatedFeatures;
	}
	
	
}
