package fr.abes.sudoqual.linking_module.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import fr.abes.sudoqual.test.UnitTests;
import fr.abes.sudoqual.util.json.JSONValidationReport;

@UnitTests
class JSONValidatorTest {

	@Test
	void testValidateInputSuccess() {
		// given
		JSONObject object = new JSONObject("{ 'scenario': 'smoke-test',"
		                                   + "'sources': ['a'],"
		                                   + "'targets': ['b'],"
		                                   + "'features': {"
		                                   + "  'a': {'name': 'c'},"
		                                   + "  'b': {'name': 'c'}"
		                                   + "}"
		                                   + "}");
		// when
		JSONValidationReport report = JSONValidatorUtils.validateInput(object);

		// then
		assertTrue(report.isSuccess(), report.getErrorMessage());
	}

	@Test
	void testValidateInputWithAllOption() {
		// given
		JSONObject object = new JSONObject("{ 'scenario': 'smoke-test',"
		                                   + "'sources': ['a1', 'a2', 'a3'],"
		                                   + "'targets': ['b'],"
		                                   + "'supports': 'sources',"
		                                   + "'safeLinks': ["
		                                   + "  { 'type': 'diffFrom', 'source': 'a1', 'target': 'b' },"
		                                   + "  { 'type': 'sameAs', 'source': 'a2', 'target': 'b' }"
		                                   + "],"
		                                   + "'features': {"
		                                   + "  'a': {'name': 'c'},"
		                                   + "  'b': {'name': 'c'}"
		                                   + "},"
		                                   + "'options': {"
		                                   + "  'validatedSameAsThreshold': 6,"
		                                   + "  'suggestedSameAsThreshold': 1,"
		                                   + "  'validatedDiffFromThreshold': 3,"
		                                   + "  'suggestedEnabled': true,"
		                                   + "  'keepOnlyBestSuggestions': true,"
		                                   + "  'exportCriterionValues': true,"
		                                   + "  'debug': true"
		                                   + "}"
		                                   + "}");
		// when
		JSONValidationReport report = JSONValidatorUtils.validateInput(object);

		// then
		assertTrue(report.isSuccess(), report.getErrorMessage());
	}

	@Test
	void testValidateOutputSuccess() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {}"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertTrue(report.isSuccess(), report.getErrorMessage());
	}

	@Test
	void testValidateOutputWithAllOptionsSuccess() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {},"
		                                   + "	   'step': 1"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test',"
		                                   + "  'options': {}"
		                                   + "},"
		                                   + "'debug': {}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertTrue(report.isSuccess(), report.getErrorMessage());
	}

	@Test
	void testValidateOutputFailNoSourceInTheLink() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'stuff': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {}"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertFalse(report.isSuccess());
	}

	@Test
	void testValidateOutputFailNoTargetInTheLink() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'stuff': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': ['some text']"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertFalse(report.isSuccess());
	}

	@Test
	void testValidateOutputFailWrongLinkType() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'stuff',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {}"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertFalse(report.isSuccess());
	}

	@Test
	void testValidateOutputFailNoLinksArrays() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'stuff': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {}"
		                                   + "  }"
		                                   + "],"
		                                   + "'metadata': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertFalse(report.isSuccess());
	}

	@Test
	void testValidateOutputFailNoMetadata() {
		// given
		JSONObject object = new JSONObject("{"
		                                   + "'computedLinks': ["
		                                   + "  {"
		                                   + "    'source': 'a',"
		                                   + "    'target': 'b',"
		                                   + "    'type': 'sameAs',"
		                                   + "    'confidence': 7,"
		                                   + "    'why': {}"
		                                   + "  }"
		                                   + "],"
		                                   + "'stuff': {"
		                                   + "  'version': 'vTest',"
		                                   + "  'scenario': 'test'"
		                                   + "}"
		                                   + "}");

		// when
		JSONValidationReport report = JSONValidatorUtils.validateOutput(object);

		// then
		assertFalse(report.isSuccess());
	}

}
