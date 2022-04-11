package fr.abes.sudoqual.linking_module;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;
import fr.abes.sudoqual.linking_module.util.JSONValidatorUtils;
import fr.abes.sudoqual.test.EndToEndTests;

@EndToEndTests
class LinkingModuleEndToEndTest {

	private static LinkingModule module;

	@BeforeAll
	static void beforeAll() throws Exception {
		module = new LinkingModuleImpl(4);
		module.registerPath(Config.RESOURCE_DIR + Config.SCENARIO_DIR);
	}

	@Test
	void testExecuteLinkingModuleException() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'end-to-end-test',"
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
		assertThrows(LinkingModuleException.class, exec);
	}
	
	/**
	 * Features data contains an empty String for a name which should not be empty
	 * according to feature specification.
	 * 
	 * @throws Exception
	 */
	@Test
	void testExecuteExceptionDataDoesNotFulfillRequirements() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'end-to-end-test',"
										  + "'sources': ['a'],"
		                                  + "'targets': ['b'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': ''},"
		                                  + "  'b': {'name': 'b'}"
		                                  + "}"
		                                  + "}");

		// when
		//Config.ENFORCE_DATA_VALIDATION = true;
		Executable exec = () -> {
			module.execute(input);
		};

		// then
		assertThrows(LinkingModuleException.class, exec);
	}

	@Test
	void testExecuteSimpleTest() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'end-to-end-test',"
		                                  + "'sources': ['a1', 'a2', 'a3', 'b1', 'd1'],"
		                                  + "'targets': ['a', 'b', 'c'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'a3': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'd1': {'name': 'd'},"
		                                  + "}"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(4, array.length(), "Wrong number of links found.");

		for (Object o : array) {
			JSONObject link = (JSONObject) o;
			assertAll("A link is not correct.",
			    () -> assertTrue(Arrays.asList("a1", "a2", "a3", "b1").contains(link.get("source"))),
			    () -> assertTrue(Arrays.asList("a", "b").contains(link.get("target"))),
			    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
			    () -> assertEquals(7, link.get("confidence")));

			switch (link.getString("source")) {
				case "a1":
				case "a2":
				case "a3":
					assertEquals("a", link.getString("target"));
					break;
				case "b1":
					assertEquals("b", link.getString("target"));
					break;
				default:
					fail("Reference source unknown");
			}
		}
	}
	
	@Test
	void testWithExportedCriterionValuesTest() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'end-to-end-test',"
										  + "'options': { 'exportCriterionValues': true },"
		                                  + "'sources': ['a1', 'a2', 'a3', 'b1', 'd1'],"
		                                  + "'targets': ['a', 'b', 'c'],"
		                                  + "'features': {"
		                                  + "  'a': {'name': 'a'},"
		                                  + "  'b': {'name': 'b'},"
		                                  + "  'c': {'name': 'c'},"
		                                  + "  'a1': {'name': 'a'},"
		                                  + "  'a2': {'name': 'a'},"
		                                  + "  'a3': {'name': 'a'},"
		                                  + "  'b1': {'name': 'b'},"
		                                  + "  'd1': {'name': 'd'},"
		                                  + "},"
		                                  + "'criterionValues':["
		                                  + "{'name':'name','source':'a1','value':0,'target':'c'},"
		                                  + "{'name':'name','source':'a1','value':1,'target':'a'},"
		                                  + "{'name':'name','source':'a1','value':0,'target':'b'},"
		                                  + "{'name':'name','source':'a2','value':0,'target':'c'},"
		                                  + "{'name':'name','source':'a2','value':1,'target':'a'},"
		                                  + "{'name':'name','source':'a2','value':0,'target':'b'},"
		                                  + "{'name':'name','source':'a3','value':0,'target':'c'},"
		                                  + "{'name':'name','source':'a3','value':1,'target':'a'},"
		                                  + "{'name':'name','source':'a3','value':0,'target':'b'},"
		                                  + "{'name':'name','source':'b1','value':0,'target':'c'},"
		                                  + "{'name':'name','source':'b1','value':0,'target':'a'},"
		                                  + "{'name':'name','source':'b1','value':1,'target':'b'},"
		                                  + "{'name':'name','source':'d1','value':0,'target':'c'},"
		                                  + "{'name':'name','source':'d1','value':0,'target':'a'},"
		                                  + "{'name':'name','source':'d1','value':0,'target':'b'}"
		                                  + "],"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(4, array.length(), "Wrong number of links found.");

		for (Object o : array) {
			JSONObject link = (JSONObject) o;
			assertAll("A link is not correct.",
			    () -> assertTrue(Arrays.asList("a1", "a2", "a3", "b1").contains(link.get("source"))),
			    () -> assertTrue(Arrays.asList("a", "b").contains(link.get("target"))),
			    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
			    () -> assertEquals(7, link.get("confidence")));

			switch (link.getString("source")) {
				case "a1":
				case "a2":
				case "a3":
					assertEquals("a", link.getString("target"));
					break;
				case "b1":
					assertEquals("b", link.getString("target"));
					break;
				default:
					fail("Reference source unknown");
			}
		}
	}
	
	
	
	@Test
	void testExecuteSafeLinksAndSupportTest() throws Exception {
		// given
		JSONObject input = new JSONObject("{ "
				                          + "'scenario': 'data-enhancement',"
		                                  + "'sources': ['book1', 'book2', 'book3', 'book4', 'book5'],"
		                                  + "'targets': ['a', 'b'],"
		                                  + "'supports': 'sources',"
		                                  + "'features': {"
		                                  + "  'a': {},"
		                                  + "  'b': {},"
		                                  + "  'book1': {'coauthors': ['a','b']},"
		                                  + "  'book2': {'coauthors': ['a','b', 'c']},"
		                                  + "  'book3': {'coauthors': ['a','c', 'd']},"
		                                  + "  'book4': {'coauthors': ['a','d', 'e']},"
		                                  + "  'book5': {'coauthors': ['a','z']},"
		                                  + "},"
		                                  + "'safeLinks': ["
		                                  + "{'type': 'sameAs', 'source': 'book1', 'target': 'a'}"
		                                  + "]"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(3, array.length(), "Wrong number of links found.");

		for (Object o : array) {
			JSONObject link = (JSONObject) o;
			assertAll("A link is not correct.",
			    () -> assertTrue(Arrays.asList("book2", "book3", "book4").contains(link.get("source"))),
			    () -> assertEquals("a", link.get("target")),
			    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
			    () -> assertEquals(7, link.get("confidence")));

			switch (link.getString("source")) {
				case "book2":
				case "book3":
				case "book4":
					assertEquals("a", link.getString("target"));
					break;
				default:
					fail(link.getString("source") + "  should not be linked.");
			}
		}
	}
	
	@Test
	void testExecuteSafeLinksAndInitialDataFromSupports() throws Exception {
		// given
		JSONObject input = new JSONObject("{ 'scenario': 'data-enhancement-one-to-one',"
		                                  + "'sources': ['a1', 'b1', 'c1'],"
		                                  + "'targets': ['a2', 'b2', 'd2'],"
		                                  + "'supports': ['book1', 'book2', 'book3', 'book4', 'book5', 'book6'],"
		                                  + "'options': {'debug':true },"
		                                  + "'features': {"
		                                  + "  'a1': {},"
		                                  + "  'b1': {},"
		                                  + "  'c1': {},"
		                                  + "  'a2': {},"
		                                  + "  'b2': {},"
		                                  + "  'd2': {},"
		                                  + "  'book1': {'coauthors': ['a','b']},"
		                                  + "  'book2': {'coauthors': ['a','b','k']},"
		                                  + "  'book3': {'coauthors': ['b','d','e']},"
		                                  + "  'book4': {'coauthors': ['b','d']},"
		                                  + "  'book5': {'coauthors': ['c','e','j']},"
		                                  + "  'book6': {'coauthors': ['d','x','y','z']},"

		                                  + "},"
		                                  + "'safeLinks': ["
		                                  + "{'type': 'sameAs', 'source': 'a1', 'target': 'book1'},"
		                                  + "{'type': 'sameAs', 'source': 'a2', 'target': 'book2'},"
		                                  + "{'type': 'sameAs', 'source': 'b1', 'target': 'book3'},"
		                                  + "{'type': 'sameAs', 'source': 'b2', 'target': 'book4'},"
		                                  + "{'type': 'sameAs', 'source': 'c1', 'target': 'book5'},"
		                                  + "{'type': 'sameAs', 'source': 'd2', 'target': 'book6'},"

		                                  + "]"
		                                  + "}");

		// when
		JSONObject result = module.execute(input);

		// then
		assertTrue(JSONValidatorUtils.validateOutput(result).isSuccess(), "Returned JSONObject does pass schema validation");

		JSONArray array = result.getJSONArray("computedLinks");
		assertEquals(2, array.length(), "Wrong number of links found.");

		for (Object o : array) {
			JSONObject link = (JSONObject) o;
			assertAll("A link is not correct.",
			    () -> assertTrue(Arrays.asList("a1", "b1").contains(link.get("source"))),
			    () -> assertEquals(Link.Type.SAME_AS.toString(), link.get("type")),
			    () -> assertEquals(7, link.get("confidence")));

			assumingThat("a1".equals(link.getString("source")),
    			() -> assertEquals("a2", link.getString("target"))
    		);
    		
    		assumingThat("b1".equals(link.getString("source")),
    			() -> assertEquals("b2", link.getString("target"))

    		);
		}
	}


}
