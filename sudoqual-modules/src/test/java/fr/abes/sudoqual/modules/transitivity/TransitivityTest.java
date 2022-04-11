package fr.abes.sudoqual.modules.transitivity;

import java.io.IOException;
import java.nio.charset.Charset;

import fr.abes.sudoqual.util.json.JSONArrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitivityTest {
    private static final Logger logger = LoggerFactory.getLogger(TransitivityTest.class);

    @Test
    void test1() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas1.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("target-target");
        assertEquals(1, computedLinks.length());
        JSONObject o = computedLinks.getJSONObject(0);
        assertTrue(o.has("targets"));
        JSONArray array = o.getJSONArray("targets");
        assertTrue(JSONArrays.contains(array, "idref:23488035X"));
        assertTrue(JSONArrays.contains(array, "idref:186956730"));
    }

    @Test
    void test2() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas2.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("target-target");
        assertEquals(1, computedLinks.length());
        JSONObject o = computedLinks.getJSONObject(0);
        assertTrue(o.has("targets"));
        JSONArray array = o.getJSONArray("targets");
        assertTrue(JSONArrays.contains(array, "idref:76895643X"));
        assertTrue(JSONArrays.contains(array, "idref:386956730"));
    }

    @Test
    void test3() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas3.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("target-target");
        assertEquals(1, computedLinks.length());
        JSONObject o = computedLinks.getJSONObject(0);
        assertTrue(o.has("targets"));
        JSONArray array = o.getJSONArray("targets");
        assertTrue(JSONArrays.contains(array, "idref:486956734"));
        assertTrue(JSONArrays.contains(array, "idref:668956432"));
    }

    @Test
    void test4() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas4.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("source-target");
        assertEquals(1, computedLinks.length());
        JSONObject o = computedLinks.getJSONObject(0);
        assertTrue(o.has("target"));
        assertEquals("idref:186956730", o.get("target"));
        assertTrue(o.has("source"));
        assertEquals("sudoc:234881828-2", o.get("source"));
    }

    @Test
    void test5() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas5.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("source-target");
        assertEquals(2, computedLinks.length());
        JSONArray computedLinks1 = res.getJSONArray("source-source");
        assertEquals(1, computedLinks1.length());
    }

    @Test
    void test6() throws JSONException, IOException {
        JSONObject res = Transitivity.INSTANCE.execute(new JSONObject(new String(Files.readFile("src/test/resources/transitivity/testTransitivityCas6.json", Charset.forName("utf-8")))));
        JSONArray computedLinks = res.getJSONArray("target-target");
        assertEquals(1, computedLinks.length());
        JSONObject o = computedLinks.getJSONObject(0);
        assertTrue(o.has("targets"));
        JSONArray array = o.getJSONArray("targets");
        assertTrue(JSONArrays.contains(array, "idref:486956734"));
        assertTrue(JSONArrays.contains(array, "idref:668956432"));
        assertTrue(JSONArrays.contains(array, "idref:333408762"));
    }

}
