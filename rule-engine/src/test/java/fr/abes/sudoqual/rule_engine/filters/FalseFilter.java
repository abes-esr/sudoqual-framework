package fr.abes.sudoqual.rule_engine.filters;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.predicate.Filter;

public class FalseFilter implements Filter {

	@Override
	public String getKey() {
		return "false";
	}

	@Override
	public Set<String> featureSet() {
		return Collections.emptySet();
	}

	@Override
	public boolean check(JSONObject data) {
		return false;
	}

}
