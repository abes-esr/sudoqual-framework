package fr.abes.sudoqual.linking_module.predicate;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

public class AlwaysCriterion implements Criterion {

	@Override
	public String getKey() {
		return "alwaysCriterion";
	}

	private static final DiscretCompType compType = new DiscretCompTypeImpl(true, -1, true, 1, true);

	@Override
	public DiscretCompType getComparisonType() {
		return compType;
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Collections.emptySet();
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		return DiscretCompType.ALWAYS;
	}

}
