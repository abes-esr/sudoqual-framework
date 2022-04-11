package fr.abes.sudoqual.rule_engine.criterions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;

/**
 * Return 100 iff names are exactly equals, 90 if they are case insensitivly equals, 0 otherwise
 * @author clement
 *
 */
public class NameCriterion implements Criterion {

	private static final DiscretCompType compType = new DiscretCompTypeImpl(true, -2, true, 2, true);

	@Override
	public DiscretCompType getComparisonType() {
		return compType;
	}

	@Override
	public String getKey() {
		return "nameCriterion";
	}

	@Override
	public Set<String> sourceFeatureSet() {
		return Collections.singleton("name");
	}

	@Override
	public Set<String> targetFeatureSet() {
		return Collections.singleton("name");
	}

	@Override
	public int compare(JSONObject ref1, JSONObject ref2) {
		String name1 = ref1.getString("name");
		String name2 = ref2.getString("name");
		if (name1.equals(name2)) {
			return 2;
		} else if (name1.toLowerCase().equals(name2.toLowerCase())){
			return 1;
		} else {
			Set<Character> set = commonChars(name1, name2);
			if(set.size() <= 1) {
				return -2;
			} else {
				return 0;
			}
		}

	}
	
	private Set<Character> commonChars(String s1, String s2) {
		Set<Character> set = new HashSet<>();
		for(Character c1 : s1.toCharArray()) {
			if(s2.indexOf(c1) >= 0) {
				set.add(c1);
			}
		}
		return set;
	}

}
