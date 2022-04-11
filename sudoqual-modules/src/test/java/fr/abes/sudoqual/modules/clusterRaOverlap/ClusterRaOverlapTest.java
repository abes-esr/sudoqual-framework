package fr.abes.sudoqual.modules.clusterRaOverlap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.util.Files;

class ClusterRaOverlapTest {
	private static final Logger logger = LoggerFactory.getLogger(ClusterRaOverlapTest.class);

	@Test
	void testNoComputedLinks() throws JSONException, IOException {
		JSONObject o = ClusterRaOverlap.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/clusterRaOverlap/testClusterRa.json",Charset.forName("utf-8")))), 90, -1);
		assertTrue(o.getJSONArray("computedLinks").isEmpty());
	}

    @Test
    void testOneComputedLinks() throws JSONException, IOException {
        JSONObject o = ClusterRaOverlap.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/clusterRaOverlap/testClusterRa.json",Charset.forName("utf-8")))), 66, -1);
        assertEquals(1, o.getJSONArray("computedLinks").length());
        JSONObject link = o.getJSONArray("computedLinks").getJSONObject(0);
        assertEquals("idref:23488035X", link.getString("target"));
        assertEquals("sameAs", link.getString("type"));
        assertEquals("_:cluster1", link.getString("cluster"));
	}

    @Test
    void testOneSuggestedLink() throws JSONException, IOException {
        JSONObject o = ClusterRaOverlap.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/clusterRaOverlap/testClusterRa.json",Charset.forName("utf-8")))), 90, 66);
        assertEquals(1, o.getJSONArray("computedLinks").length());
        JSONObject link = o.getJSONArray("computedLinks").getJSONObject(0);
        assertEquals("idref:23488035X", link.getString("target"));
        assertEquals("suggestedSameAs", link.getString("type"));
        assertEquals("_:cluster1", link.getString("cluster"));
    }

    @Test
    void testNicolasYann() throws JSONException, IOException {
        JSONObject o = ClusterRaOverlap.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/clusterRaOverlap/testClusterRaNicolasYann.json",Charset.forName("utf-8")))),90, -1);
        assertEquals(3, o.getJSONArray("computedLinks").length());
    }

    @Test
    void testDouble() throws JSONException, IOException {
        JSONObject o = ClusterRaOverlap.INSTANCE.execute(new JSONObject( new String(Files.readFile("src/test/resources/clusterRaOverlap/testClusterRaDouble.json",Charset.forName("utf-8")))),90, -1);
        assertEquals(2, o.getJSONArray("computedLinks").length());
    }

}
