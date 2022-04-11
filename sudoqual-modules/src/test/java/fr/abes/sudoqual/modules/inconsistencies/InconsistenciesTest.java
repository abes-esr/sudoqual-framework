package fr.abes.sudoqual.modules.inconsistencies;

import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Files;

class InconsistenciesTest {
	private static final Logger logger = LoggerFactory.getLogger(InconsistenciesTest.class);

	@Test
	void test() throws JSONException, IOException {
		JSONObject o1 = Inconsistencies.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/inconsistencies/testInconsistenciesCas1.json",Charset.forName("utf-8")))));
	    System.out.println(o1);
	}

}
