package fr.abes.sudoqual.linking_module;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.abes.sudoqual.linking_module.util.JSONValidatorUtils;
import fr.abes.sudoqual.test.EndToEndTests;

@EndToEndTests
class LinkingModuleSuggestedTest {

	private static LinkingModule module;

	@BeforeAll
	static void beforeAll() throws Exception {
		module = new LinkingModuleImpl(1);
		module.registerPath(Config.RESOURCE_DIR + Config.SCENARIO_DIR);
	}
	
	@Test
	void testSuggestedDueToSeveralTargetsMatchManyToOne() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'smoke-test',"
		                                  + "'sources': ['a1', 'a2', 'b1', 'b2', 'c'],"
		                                  + "'targets': ['aa1', 'aa2', 'bb1', 'bb2', 'd'],"
		                                  + "'features': {"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'b2': {'name': 'b'},"
		                                  + "  'aa1': {'name': 'a'},"
		                                  + "  'aa2': {'name': 'a'},"
		                                  + "  'bb1': {'name': 'b'},"
		                                  + "  'bb2': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'd': {'name': 'd'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");
		
		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(8, array.length(), "Wrong number of links found.");
		
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			String target = link.getString("target");
			
			assertAll("a link is not correct.", 
				() -> assertTrue("a1".equals(source) || "b1".equals(source) || "a2".equals(source) || "b2".equals(source)),
    		    () -> assertEquals(Link.Type.SUGGESTED.toString(), link.get("type")),
    			() -> assertEquals(7, link.get("confidence"))
    		);
			
    		assumingThat("a1".equals(source) || "a2".equals(source),
        			() -> assertTrue("aa1".equals(target) || "aa2".equals(target))
    		);
    		
    		assumingThat("b1".equals(source) || "b2".equals(source),
    			() -> assertTrue("bb1".equals(target) || "bb2".equals(target))
    		);
		}
	}
	
	@Test
	void testSuggestedDueToSeveralTargetsMatchOneToOne() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'smoke-test-one-to-one',"
		                                  + "'sources': ['a1', 'a2', 'b1', 'b2', 'c'],"
		                                  + "'targets': ['aa1', 'aa2', 'bb1', 'bb2', 'd'],"
		                                  + "'features': {"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'b2': {'name': 'b'},"
		                                  + "  'aa1': {'name': 'a'},"
		                                  + "  'aa2': {'name': 'a'},"
		                                  + "  'bb1': {'name': 'b'},"
		                                  + "  'bb2': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'd': {'name': 'd'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");
		
		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(8, array.length(), "Wrong number of links found.");
		
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			String target = link.getString("target");
			
			assertAll("a link is not correct.", 
				() -> assertTrue("a1".equals(source) || "b1".equals(source) || "a2".equals(source) || "b2".equals(source)),
    		    () -> assertEquals(Link.Type.SUGGESTED.toString(), link.get("type")),
    			() -> assertEquals(7, link.get("confidence"))
    		);
			
    		assumingThat("a1".equals(source) || "a2".equals(source),
        			() -> assertTrue("aa1".equals(target) || "aa2".equals(target))
    		);
    		
    		assumingThat("b1".equals(source) || "b2".equals(source),
    			() -> assertTrue("bb1".equals(target) || "bb2".equals(target))
    		);
		}
	}

}
