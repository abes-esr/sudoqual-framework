/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import fr.abes.sudoqual.api.SudoqualModule;
import org.json.JSONObject;

import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;
import fr.abes.sudoqual.rule_engine.FeatureManager;

/**
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface LinkingModule extends Closeable, SudoqualModule {

	String URI_SPECIAL_FEATURE_KEY = FeatureManager.URI_KEY;
	String INITIAL_LINKS_SPECIAL_FEATURE_KEY = "initialLinks";

	/**
	 * A static factory method to create a LinkingModule instance.
	 * @param nbThreads
	 * @return a LinkingModule instance.
	 */
	static LinkingModule create(int nbThreads) {
		return new LinkingModuleImpl(nbThreads);
	}

	/**
	 * Register a path where LinkingModule will look for scenario files.
	 * @param path
	 */
	void registerPath(String path);

	/**
	 * Executes the linking module over the given input.
	 * @param input a String representation of the JSON input
	 * @return a String that represents the output of the module
	 * @throws LinkingModuleException
	 * @throws InterruptedException
	 */
	@Override
	String execute(String input) throws LinkingModuleException, InterruptedException;

	/**
	 * Executes the linking module over the given input.
	 * @param input the input as a {@link JSONObject}
	 * @return the output of the module as a {@link JSONObject}
	 * @throws LinkingModuleException
	 * @throws InterruptedException
	 */
    @Override
	JSONObject execute(JSONObject input) throws LinkingModuleException, InterruptedException;

	/**
	 * Executes the linking module over the given input.
	 * @param input a String representation of the JSON input given as an {@link InputStream}.
	 * @param charset the charset to be used to read the input
	 * @return a String that represents the output of the module
	 * @throws LinkingModuleException
	 * @throws InterruptedException
	 */
    @Override
	String execute(InputStream input, Charset charset) throws LinkingModuleException, InterruptedException;

	@Override
	void close();

    /**
     * Generates a JSONSchema on which you can validate input files for a given scenario name.
     * @param scenarioName the name of a scenario
     * @return a JSONSchema
     * @throws LinkingModuleException
     */
    JSONObject generateSchema(String scenarioName) throws LinkingModuleException;

    /**
     * Lists features used by the specified scenario
     * @param scenario the name of a scenario
     * @return a list of feature keys
     * @throws LinkingModuleException
     */
    List<String> listFeatures(String scenario) throws LinkingModuleException;
}
