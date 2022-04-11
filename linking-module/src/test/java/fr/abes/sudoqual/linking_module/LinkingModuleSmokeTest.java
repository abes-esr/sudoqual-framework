package fr.abes.sudoqual.linking_module;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.abes.sudoqual.linking_module.util.JSONValidatorUtils;
import fr.abes.sudoqual.test.SmokeTests;

@SmokeTests
class LinkingModuleSmokeTest {

	private static LinkingModule module;

	@BeforeAll
	static void beforeAll() throws Exception {
		module = new LinkingModuleImpl(1);
		module.registerPath(Config.RESOURCE_DIR + Config.SCENARIO_DIR);
	}

	@Test
	void testExecute() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'smoke-test',"
		                                  + "'sources': ['a1', 'a2', 'b1', 'b2', 'c1'],"
		                                  + "'targets': ['a', 'b', 'd'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'},"
		                                  + "  'd': {'name': 'd'},"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'b2': {'name': 'b'},"
		                                  + "  'c1': {'name': 'c'},"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");
		
		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(4, array.length(), "Wrong number of links found.");
		
		List<String> alreadyMetSources = new LinkedList<>();
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			if(alreadyMetSources.contains(source)) {
				fail("duplicate link with source " + source);
			}
			alreadyMetSources.add(source);
			
			assertAll("a link is not correct.", 
				() -> assertTrue("a1".equals(source) || "b1".equals(source) || "a2".equals(source) || "b2".equals(source)),
    		    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
    			() -> assertEquals(7, link.get("confidence"))
    		);
			
    		assumingThat("a1".equals(source) || "a2".equals(source),
        			() -> assertEquals("a", link.get("target"))
    		);
    		
    		assumingThat("b1".equals(source) || "b2".equals(source),
        			() -> assertEquals("b", link.get("target"))
    		);
		}
	}
	
	@Test
	void testOneToOne() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'smoke-test-one-to-one',"
		                                  + "'sources': ['a', 'b', 'c'],"
		                                  + "'targets': ['aa', 'bb', 'd'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'},"
		                                  + "  'aa': {'name': 'a'},"
		                                  + "  'bb': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'd': {'name': 'd'},"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");
		
		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(2, array.length(), "Wrong number of links found.");
		
		List<String> alreadyMetSources = new LinkedList<>();
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			if(alreadyMetSources.contains(source)) {
				fail("duplicate link with source " + source);
			}
			alreadyMetSources.add(source);
			
			assertAll("a link is not correct.", 
				() -> assertTrue("a".equals(link.get("source")) || "b".equals(link.get("source"))),
    		    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
    			() -> assertEquals(7, link.get("confidence"))
    		);
			
    		assumingThat("a".equals(link.get("source")),
        			() -> assertEquals("aa", link.get("target"))
    		);
    		
    		assumingThat("b".equals(link.get("source")),
        			() -> assertEquals("bb", link.get("target"))
    		);
		}
	}
	
	@Test
	void testManyToMany() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'smoke-test-many-to-many',"
		                                  + "'sources': ['a1', 'a2', 'b1', 'b2', 'c'],"
		                                  + "'targets': ['a1', 'a2', 'b1', 'b2', 'd'],"
		                                  + "'features': {"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'b2': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'd': {'name': 'd'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");
		
		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(4, array.length(), "Wrong number of links found.");
		
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			String target = link.getString("target");
			
			assertAll("a link is not correct.", 
				() -> assertTrue("a1".equals(source) || "b1".equals(source) || "a2".equals(source) || "b2".equals(source)),
    		    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
    			() -> assertEquals(7, link.get("confidence"))
    		);
			
    		assumingThat("a1".equals(source),
        			() -> assertTrue("a2".equals(target))
    		);
    		
    		assumingThat("a2".equals(source),
    			() -> assertTrue("a1".equals(target))
    		);
    		
    		assumingThat("b1".equals(source),
    			() -> assertTrue("b2".equals(target))
    		);
    		
    		assumingThat("b2".equals(source),
    			() -> assertTrue("b1".equals(target))
    		);
		}
	}

}
