package fr.abes.sudoqual.rule_engine.filters;

import java.util.Collections;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.predicate.Filter;

public class PersonFilter implements Filter {

	@Override
	public String getKey() {
		return "person";
	}

	@Override
	public Set<String> featureSet() {
		return Collections.singleton("type");
	}

	@Override
	public boolean check(JSONObject data) {
		String type = data.getString("type");
		return "person".equals(type);
	}

}
