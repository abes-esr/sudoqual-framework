package fr.abes.sudoqual.linking_module.feature;

import fr.abes.sudoqual.rule_engine.feature.Feature;
import org.json.JSONObject;

public class NameFeature implements Feature {


	@Override
	public boolean checkValue(Object o) {
		if(o instanceof String) {
			return !((String)o).isBlank();
		}
		return false;
	}

    @Override
    public JSONObject getValidationSchema() {
        return new JSONObject("{'type': 'string', 'minLength': 1 }");
    }
}
