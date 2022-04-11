/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.modules.diagnostic.rcra;

import fr.abes.sudoqual.modules.diagnostic.Config;
import fr.abes.sudoqual.modules.diagnostic.Diagnostician;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.json.JSONArrays;
import fr.abes.sudoqual.util.json.JSONValidator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumingThat;

/**
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class DiagnosticManyToOneModuleEndToEndTest {

	private static Diagnostician diagnostician;
	private static JSONValidator validator;

	@BeforeAll
	static void beforeAll() throws Exception {
		diagnostician = Diagnostician.createManyToOneDiagnostician();
		URL outputFile = ResourceUtils.getResource(DiagnosticManyToOneModuleEndToEndTest.class, Config.RESOURCE_DIR, "diagnostic/schema-output.json");
		JSONObject rawOutputSchema = new JSONObject(new JSONTokener(new InputStreamReader(outputFile.openStream(),
		                                                                                  Config.CHARSET)));
		validator = JSONValidator.instance(rawOutputSchema);
	}

	@Test
	void testEmpty() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': [], 'targets': [], 'initialLinks': [],"
		                                  + "'computedLinks': []}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		assertTrue(res.getJSONArray("diagnostic").isEmpty());
	}

	// /////////////////////////////////////////////////////////////////////////
	// Validated link
	// /////////////////////////////////////////////////////////////////////////

	@Test
	/**
	 * ∃ AL ∧ ∃ CL ∧ CL = AL –> Lien validé RC a un lien asserté et un lien calculé,
	 * et ces liens coïncident : le lien est validé.
	 *
	 * @throws Exception
	 */
	void testCase1() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertEquals("b", diag.getJSONObject("computedLink").get("target"));
		assertEquals(1, diag.get("case"));
		assertEquals("validatedLink", diag.get("status"));
	}

	// /////////////////////////////////////////////////////////////////////////
	// Erroneous Link
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * ∃ AL ∧ ∃ CL ∧ CL ≠ AL –> Lien erroné à corriger en CL RC a un lien asserté et
	 * un lien calculé, et ces liens ne coïncident pas : le lien est considéré comme
	 * erroné.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase2() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': [{'source':'a', 'target':'c', 'type':'sameAs'}]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertEquals("c", diag.getJSONObject("computedLink").get("target"));
		assertEquals(2, diag.get("case"));
		assertEquals("erroneousLink", diag.get("status"));
	}

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∈ IL ∧ SL ≠ {} (∧ CA ≠ IL) –> Lien erroné à corriger en SL
	 * RC a un lien asserté, n’a pas de lien calculé, mais le lien asserté est jugé
	 * comme impossible par SudoQual : on signale que le lien est erroné et on
	 * indique les corrections suggérées.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase3() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'b', 'type':'diffFrom'},"
		                                  + "{'source':'a', 'target':'c', 'type':'suggestedSameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(3, diag.get("case"));
		assertEquals("erroneousLink", diag.get("status"));

		assertTrue(diag.has("suggestedLinks"));
		JSONArray sugg = diag.getJSONArray("suggestedLinks");
		assertEquals(1, sugg.length());
		assertTrue(JSONArrays.contains(sugg, o -> "c".equals(((JSONObject) o).optString("target"))));

		assertTrue(diag.has("impossibleLinks"));
		JSONArray diffFrom = diag.getJSONArray("impossibleLinks");
		assertEquals(1, diffFrom.length());
		assertTrue(JSONArrays.contains(diffFrom, o -> "b".equals(((JSONObject) o).optString("target"))));
	}

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∈ IL ∧ SL = {} ∧ CA = IL –> Lien erroné à corriger en
	 * Nouvelle Autorité RC a un lien asserté, n’a pas de lien calculé, mais le lien
	 * asserté et toutes les autres autorités candidates sont jugées comme
	 * impossibles par SudoQual : on signale que le lien est erroné, il faudrait
	 * créer une nouvelle autorité.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase4() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'b', 'type':'diffFrom'},"
		                                  + "{'source':'a', 'target':'c', 'type':'diffFrom'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(4, diag.get("case"));
		assertEquals("erroneousLink", diag.get("status"));

		assertFalse(diag.has("suggestedLinks"));

		assertTrue(diag.has("impossibleLinks"));
		JSONArray diffFrom = diag.getJSONArray("impossibleLinks");
		assertEquals(2, diffFrom.length());
		assertTrue(JSONArrays.contains(diffFrom, o -> "b".equals(((JSONObject) o).optString("target"))));
		assertTrue(JSONArrays.contains(diffFrom, o -> "c".equals(((JSONObject) o).optString("target"))));

	}

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∈ IL ∧ SL = {} ∧ CA ≠ LI –> Lien erroné à corriger RC a un
	 * lien asserté, n’a pas de lien calculé, mais le lien asserté est jugé comme
	 * impossible par SudoQual et il n’y a pas d’autorités suggérées : on signale
	 * que le lien est erroné.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase5() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'b', 'type':'diffFrom'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(5, diag.get("case"));
		assertEquals("erroneousLink", diag.get("status"));

		assertFalse(diag.has("suggestedLinks"));
		assertTrue(diag.has("impossibleLinks"));
		JSONArray diffFrom = diag.getJSONArray("impossibleLinks");
		assertEquals(1, diffFrom.length());
		assertTrue(JSONArrays.contains(diffFrom, o -> "b".equals(((JSONObject) o).optString("target"))));

	}

	// /////////////////////////////////////////////////////////////////////////
	// Almost validated link
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∉ IL ∧ SL ≠ {} ∧ AL ∈ SL –> Lien quasi validé RC a un lien
	 * asserté, pas de lien calculé, mais le lien asserté appartient aux liens
	 * suggérés : on indique que le lien est plutôt de bonne qualité.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase6() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'b', 'type':'suggestedSameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(6, diag.get("case"));
		assertEquals("almostValidatedLink", diag.get("status"));

		assertTrue(diag.has("suggestedLinks"));
		JSONArray sugg = diag.getJSONArray("suggestedLinks");
		assertEquals(1, sugg.length());
		assertTrue(JSONArrays.contains(sugg, o -> "b".equals(((JSONObject) o).optString("target"))));

	}

	// /////////////////////////////////////////////////////////////////////////
	// Doubtful link
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∉ IL ∧ SL = {} –> Lien douteux RC a un lien asserté, mais
	 * n’a ni lien calculé, ni lien suggéré, et le lien asserté n’est pas jugé comme
	 * impossible : on indique que le lien est douteux mais reste possible.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase7() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(7, diag.get("case"));
		assertEquals("doubtfulLink", diag.get("status"));

		assertFalse(diag.has("suggestedLinks"));

	}

	/**
	 * ∃ AL ∧ ¬∃ CL ∧ AL ∉ IL ∧ SL ≠ {} ∧ AL ∉ SL –> Lien douteux avec suggestion de
	 * correction en SL RC a un lien asserté, n’a pas de lien calculé, mais a des
	 * liens suggérés, et le lien asserté bien que n’étant pas jugé comme impossible
	 * n’appartient pas aux liens suggérés : on indique que le lien est (très)
	 * douteux et on suggère de corriger avec les liens suggérés.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase8() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [{'source':'a', 'target':'b', 'type':'sameAs'}],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'c', 'type':'suggestedSameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertEquals("b", diag.get("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(8, diag.get("case"));
		assertEquals("doubtfulLink", diag.get("status"));

		assertTrue(diag.has("suggestedLinks"));
		JSONArray sugg = diag.getJSONArray("suggestedLinks");
		assertEquals(1, sugg.length());
		assertTrue(JSONArrays.contains(sugg, o -> "c".equals(((JSONObject) o).optString("target"))));

	}

	// /////////////////////////////////////////////////////////////////////////
	// Missing link
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * ¬∃ AL ∧ ∃ CL –> Lien absent à compléter avec CL RC n’a pas de lien asserté,
	 * mais a un lien calculé : le lien est absent et on indique la correction.
	 *
	 * @throws Exception
	 */
	@Test
	void testCase9() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'c', 'type':'sameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertFalse(diag.has("initialLink"));
		assertEquals("c", diag.getJSONObject("computedLink").getString("target"));
		assertEquals(9, diag.get("case"));
		assertEquals("missingLink", diag.get("status"));
	}

	/**
	 * ¬∃ AL ∧ ¬∃ CL ∧ CA = IL –> Lien absent à compléter avec Nouvelle Autorité RC
	 * n’a ni lien asserté, ni lien calculé et toutes les autorités candidates sont
	 * jugées comme impossible : le lien est absent et il faudrait créer une
	 * nouvelle autorité (ou chercher parmi les autorités du Sudoc non retournées
	 * par Find1)
	 *
	 * @throws Exception
	 */
	@Test
	void testCase10() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'b', 'type':'diffFrom'},"
		                                  + "{'source':'a', 'target':'c', 'type':'diffFrom'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertFalse(diag.has("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(10, diag.get("case"));
		assertEquals("missingLink", diag.get("status"));

		assertFalse(diag.has("suggestedLinks"));

		assertTrue(diag.has("impossibleLinks"));
		JSONArray diffFrom = diag.getJSONArray("impossibleLinks");
		assertEquals(2, diffFrom.length());
		assertTrue(JSONArrays.contains(diffFrom, o -> "b".equals(((JSONObject) o).optString("target"))));
		assertTrue(JSONArrays.contains(diffFrom, o -> "c".equals(((JSONObject) o).optString("target"))));
	}

	/**
	 * ¬∃ AL ∧ ¬∃ CL ∧ CA ≠ IL ∧ SL = {} –> Lien absent difficile à compléter RC n’a
	 * ni lien asserté, ni lien calculé, aucune autorité n’est suggéré mais quelques
	 * autorités candidates sont pos- sibles : le lien est absent et aucune
	 * suggestion de complétion n’est donnée
	 *
	 * @throws Exception
	 */
	@Test
	void testCase11() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [],"
		                                  + "'computedLinks': []}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertFalse(diag.has("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(11, diag.get("case"));
		assertEquals("missingLink", diag.get("status"));

		assertFalse(diag.has("suggestedLinks"));

	}

	/**
	 * ¬∃ AL ∧ ¬∃ CL ∧ CA ≠ IL ∧ SL ≠ {} –> Lien absent à compléter en SL RC n’a ni
	 * lien asserté, ni lien calculé, mais quelques liens sont suggérés : le lien
	 * est absent et il est proposé de le compléter avec l’un des liens suggérés
	 *
	 * @throws Exception
	 */
	@Test
	void testCase12() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': [],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a', 'target':'c', 'type':'suggestedSameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(1, array.length(), "Wrong number of links found.");

		JSONObject diag = array.getJSONObject(0);
		assertEquals("a", diag.get("source"));
		assertFalse(diag.has("initialLink"));
		assertFalse(diag.has("computedLink"));
		assertEquals(12, diag.get("case"));
		assertEquals("missingLink", diag.get("status"));

		assertTrue(diag.has("suggestedLinks"));
		JSONArray sugg = diag.getJSONArray("suggestedLinks");
		assertEquals(1, sugg.length());
		assertTrue(JSONArrays.contains(sugg, o -> "c".equals(((JSONObject) o).optString("target"))));

	}

	// /////////////////////////////////////////////////////////////////////////
	// Others
	// /////////////////////////////////////////////////////////////////////////

	@Test
	void testMultiSourceMultiCase() throws Exception {
		// given
		JSONObject input = new JSONObject("{'sources': ['a1', 'a2', 'a3', 'a4', 'a5'],"
		                                  + "'targets': ['b','c'],"
		                                  + "'initialLinks': ["
		                                  + "{'source':'a1', 'target':'b', 'type':'sameAs'},"
		                                  + "{'source':'a2', 'target':'b', 'type':'sameAs'},"
		                                  + "{'source':'a4', 'target':'b', 'type':'sameAs'},"
		                                  + "{'source':'a5', 'target':'b', 'type':'sameAs'}"
		                                  + "],"
		                                  + "'computedLinks': ["
		                                  + "{'source':'a1', 'target':'b', 'type':'sameAs'},"
		                                  + "{'source':'a2', 'target':'b', 'type':'diffFrom'},"
		                                  + "{'source':'a4', 'target':'b', 'type':'suggestedSameAs'},"
		                                  + "{'source':'a5', 'target':'c', 'type':'suggestedSameAs'}"
		                                  + "]}");

		// when
		JSONObject res = diagnostician.execute(input);

		// then
		assertTrue(validator.validate(res).isSuccess(), "Returned JSONObject does pass schema validation");
		JSONArray array = res.getJSONArray("diagnostic");
		assertEquals(5, array.length(), "Wrong number of links found.");

		List<String> alreadyMetSources = new LinkedList<>();

		for (Object o : array) {
			assertTrue(o instanceof JSONObject);
			JSONObject diag = (JSONObject) o;
			String source = diag.getString("source");
			if(alreadyMetSources.contains(source)) {
				fail("duplicate link with source " + source);
			}
			alreadyMetSources.add(source);

			assertTrue("a1".equals(source) || "a2".equals(source) || "a3".equals(source) ||
				"a4".equals(source) || "a5".equals(source));

			assumingThat("a1".equals(source), () -> {
				assertEquals("b", diag.get("initialLink"));
				assertEquals("b", diag.getJSONObject("computedLink").getString("target"));
				assertEquals(1, diag.get("case"));
				assertEquals("validatedLink", diag.get("status"));
			});

			assumingThat("a2".equals(source), () -> {
				assertEquals("b", diag.get("initialLink"));
				assertFalse(diag.has("computedLink"));
				assertEquals(5, diag.get("case"));
				assertEquals("erroneousLink", diag.get("status"));

				assertFalse(diag.has("suggestedLinks"));
				assertTrue(diag.has("impossibleLinks"));
				JSONArray diffFrom = diag.getJSONArray("impossibleLinks");
				assertEquals(1, diffFrom.length());
				assertTrue(JSONArrays.contains(diffFrom, obj -> "b".equals(((JSONObject) obj).optString("target"))));
			});

			assumingThat("a3".equals(source), () -> {
				assertFalse(diag.has("initialLink"));
				assertFalse(diag.has("computedLink"));
				assertEquals(11, diag.get("case"));
				assertEquals("missingLink", diag.get("status"));

				assertFalse(diag.has("suggestedLinks"));
			});

			assumingThat("a4".equals(source), () -> {
				assertEquals("b", diag.get("initialLink"));
				assertFalse(diag.has("computedLink"));
				assertEquals(6, diag.get("case"));
				assertEquals("almostValidatedLink", diag.get("status"));

				assertTrue(diag.has("suggestedLinks"));
				JSONArray sugg = diag.getJSONArray("suggestedLinks");
				assertEquals(1, sugg.length());
				assertTrue(JSONArrays.contains(sugg, obj -> "b".equals(((JSONObject) obj).optString("target"))));
			});

			assumingThat("a5".equals(source), () -> {
				assertEquals("b", diag.get("initialLink"));
				assertFalse(diag.has("computedLink"));
				assertEquals(8, diag.get("case"));
				assertEquals("doubtfulLink", diag.get("status"));

				assertTrue(diag.has("suggestedLinks"));
				JSONArray sugg = diag.getJSONArray("suggestedLinks");
				assertEquals(1, sugg.length());
				assertTrue(JSONArrays.contains(sugg, obj -> "c".equals(((JSONObject) obj).optString("target"))));
			});
		}
	}

}
