package fr.abes.sudoqual.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResourceUtilsTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void test() throws ResourceNotFoundException {
		URL url = ResourceUtils.getResource(getClass(), "/", "resource-test.txt");
		assertNotNull(url);
	}

}
