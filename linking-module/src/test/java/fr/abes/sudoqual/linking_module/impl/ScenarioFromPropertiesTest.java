package fr.abes.sudoqual.linking_module.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.abes.sudoqual.linking_module.Config;
import fr.abes.sudoqual.linking_module.Scenario;
import fr.abes.sudoqual.util.ResourceUtils;

class ScenarioFromPropertiesTest {

	private Scenario scenario;

	@BeforeEach
	void beforeEach() throws Exception {
		URL file = ResourceUtils.getResource(getClass(), Set.of(Config.RESOURCE_DIR, "/"), "scenario-test.properties");
		scenario = new ScenarioFromProperties(file, Set.of("/"));
	}

	@Test
	void testGetRuleSetFileName() {
		// when
		String value = scenario.getRuleSetFileName();

		// then
		assertEquals("scenario.dlp", value);
	}

	@Test
	void testGetValidatedSameAsThreshold() {
		// when
		int value = scenario.getValidatedSameAsThreshold();

		// then
		assertEquals(5, value);
	}

	@Test
	void testGetSuggestedSameAsThreshold() {
		// when
		int value = scenario.getSuggestedSameAsThreshold();

		// then
		assertEquals(1, value);
	}

	@Test
	void testGetValidatedDiffFromThreshold() {
		// when
		int value = scenario.getValidatedDiffFromThreshold();

		// then
		assertEquals(-6, value);
	}

	@Test
	void testGetSuggestedEnabled() {
		// when
		boolean value = scenario.isSuggestedEnabled();

		// then
		assertEquals(true, value);
	}

	@Test
	void testGetSuggestedEvenIfValidatedEnabled() {
		// when
		boolean value = scenario.isKeepOnlyBestSuggestionsEnabled();

		// then
		assertEquals(false, value);
	}

	@Test
	void testGetExportCriterionValues() {
		// when
		boolean value = scenario.isExportCriterionValuesEnabled();

		// then
		assertEquals(false, value);
	}

}
