package fr.abes.sudoqual.linking_module;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;
import fr.abes.sudoqual.linking_module.exception.ScenarioException;
import fr.abes.sudoqual.linking_module.exception.ScenarioNotFoundException;
import fr.abes.sudoqual.linking_module.util.JSONValidatorUtils;
import fr.abes.sudoqual.test.UnitTests;

@UnitTests
class LinkingModuleUnitTest {

	private static LinkingModule module;

	@BeforeAll
	static void beforeAll() throws Exception {
		module = new LinkingModuleImpl(1);
		module.registerPath(Config.RESOURCE_DIR + Config.SCENARIO_DIR);
	}

	// /////////////////////////////////////////////////////////////////////////
	// TESTS
	// /////////////////////////////////////////////////////////////////////////

	@Test
	void testSuggestedEnable() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'suggested',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject link = array.getJSONObject(0);
		assertAll("The link is not correct.", () -> assertEquals("a", link.get("source")),
		    () -> assertEquals("b", link.get("target")),
		    () -> assertEquals(Link.Type.SUGGESTED.toString(), link.get("type")),
		    () -> assertEquals(2, link.get("confidence")));
	}

	@Test
	void testSuggestedDisabled() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'suggested-disabled',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(0, array.length(), "Wrong number of links found.");
	}

	@Test
	void testSuggestedDisabledByOption() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'suggested',"
		                                  + "'options': { 'suggestedEnabled': false },"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(0, array.length(), "Wrong number of links found.");
	}
	
	@Test
	void testSuggestedThresholdIncreasedByOption() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'suggested',"
		                                  + "'options': { 'suggestedSameAsThreshold': 3 },"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'}"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(0, array.length(), "Wrong number of links found.");
	}
	
	@Test
	void testSuggestedEvenIfValidated() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'suggested',"
		                                  + "'options': { 'keepOnlyBestSuggestions': false },"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['aa','b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'aa': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'},"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(2, array.length(), "Wrong number of links found.");
		
		List<String> alreadyMetTargets = new LinkedList<>();
		for(Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject link = (JSONObject) o;
			String source = link.getString("source");
			String target = link.getString("target");
			assertEquals("a", source);
			assertTrue("aa".equals(target) || "b".equals(target));
			
			if(alreadyMetTargets.contains(target)) {
				fail("duplicate link with source " + target);
			}
			alreadyMetTargets.add(target);
			
    		assumingThat("aa".equals(target),
    			() -> assertAll("a link is not correct.", 
    				() -> assertEquals("a", link.get("source")),
        		    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
        			() -> assertEquals(7, link.get("confidence"))
        		)
    		);
    		
    		assumingThat("b".equals(target),
    			() -> assertAll("a link is not correct.", 
    				() -> assertEquals("a", link.get("source")),
        		    () -> assertEquals(Link.Type.SUGGESTED.toString(), link.get("type")),
        			() -> assertEquals(2, link.get("confidence"))
        		)
    		);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Exception
	// /////////////////////////////////////////////////////////////////////////

	@Test
	void testNotCorrectlyDefinedScenario() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'not-correctly-defined',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'c'},"
		                                  + "  'b': {'name': 'c'}"
		                                  + "}"
		                                  + "}");

		// when
		Executable exec = () -> {
			module.execute(input);
		};

		// then
		assertThrows(ScenarioException.class, exec);
	}

	@Test
	void testRuleFileDoesNotExist() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'rule-file-does-not-exist',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'c'},"
		                                  + "  'b': {'name': 'c'}"
		                                  + "}"
		                                  + "}");

		// when
		Executable exec = () -> {
			module.execute(input);
		};

		// then
		assertThrows(ScenarioException.class, exec);
	}

	@Test
	void testScenarioNotFound() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'not-found',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'c'},"
		                                  + "  'b': {'name': 'c'}"
		                                  + "}"
		                                  + "}");

		// when
		Executable exec = () -> {
			module.execute(input);
		};

		// then
		assertThrows(ScenarioNotFoundException.class, exec);
	}

	@Test
	void testScenarioDoesNotExist() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'not-exist',"
		                                  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "}"
		                                  + "}");

		// when
		Executable exec = () -> {
			module.execute(input);
		};

		// then
		assertThrows(LinkingModuleException.class, exec);
	}

}
