package fr.abes.sudoqual.cli.services.hidden;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import fr.abes.sudoqual.cli.CLIUtils;
import fr.abes.sudoqual.cli.SudoqualCommander;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import fr.abes.sudoqual.linking_module.BusinessClassLoader;
import fr.abes.sudoqual.linking_module.LinkingModule;
import fr.abes.sudoqual.linking_module.Scenario;
import fr.abes.sudoqual.linking_module.exception.BusinessClassException;
import fr.abes.sudoqual.linking_module.exception.ScenarioException;
import fr.abes.sudoqual.linking_module.exception.ScenarioNotFoundException;
import fr.abes.sudoqual.linking_module.impl.IntrospectionBusinessClassLoader;
import fr.abes.sudoqual.linking_module.impl.ScenarioFromProperties;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.feature.PreprocessedFeature;
import fr.abes.sudoqual.rule_engine.feature.RawFeature;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.json.JSONObjects;

@Parameters(commandDescription = "run a feature", hidden = true)
public class FeatureCommand {
	public static final String NAME = "feature";

	private static final Logger logger = LoggerFactory.getLogger(FeatureCommand.class);

	@Parameter(description = "<scenario name> <feature name>", required = true, arity = 2)
	private List<String> args = new ArrayList<String>();

	@Parameter(names = {"-d", "--data"}, description = "data on which execute the feature")
	private String data;

	private String scenarioDir;


	public FeatureCommand() {
	}

	public int run(SudoqualCommander cmd) {
		this.scenarioDir = cmd.getScenarioDir();
		String scenarioName = args.get(0);
		String featureName = args.get(1);

		Scenario scenario = null;
		try {
			scenario = getScenario(scenarioName);
		} catch (ScenarioException e) {
			logger.error("",e);
			return 1;
		}
		BusinessClassLoader loader = null;
		try {
			loader = new IntrospectionBusinessClassLoader(scenario.getBusinessClassPackageName(), scenario);
		} catch (BusinessClassException e) {
			logger.error("",e);
			return 1;
		}
		Feature feat = loader.createFeature(featureName);

		JSONObject o = JSONObjects.from("{ \"data\": " + data + "}"); // TODO backspaces double quotes
		Object input = o.get("data");

		System.out.println();
		System.out.println("=========================");
		System.out.println("=    Feature details    =");
		System.out.println("=========================");
		System.out.println("name: " + featureName);
		if(feat instanceof PreprocessedFeature) {
			System.out.println("kind: preprocessed");
		} else if(feat instanceof ComputedFeature) {
			System.out.println("kind: computed");
		} else if(feat instanceof RawFeature) {
			System.out.println("kind: raw");
		} else {
			System.out.println("kind: unknown");
		}
		System.out.println("input:");
		if(input instanceof JSONObject) {
			System.out.println(((JSONObject)input).toString(1));
		} else if (input instanceof JSONArray) {
			System.out.println(((JSONArray)input).toString(1));
		} else {
			System.out.println(input);
		}


		System.out.println();
		System.out.print("input validation: ");
		if(!feat.checkValue(input)) {
			System.err.println("Input does not fulfill feature requirements, see " + feat.getClass().getCanonicalName() + ".checkValue(Object) method.");
		} else {
			System.out.println("ok");
		}

		Object res = null;
		if(feat instanceof PreprocessedFeature) {
			res = ((PreprocessedFeature)feat).buildValue(input);
		} else if(feat instanceof ComputedFeature) {
			if(!(input instanceof JSONArray)) {
				System.err.println("\nError: The " + featureName + " feature is a computed feature, input value must be an array.");
				return 1;
			}
			Collection<JSONObject> preparedFeatures;
			try {
				preparedFeatures = CLIUtils.prepareFeature(loader,(ComputedFeature<?>)feat, (JSONArray) input);
			} catch (Exception e) {
				logger.warn("An error occured during feature preparation", e);
				return 1;
			}
			res = ((ComputedFeature<?>)feat).compute(preparedFeatures);
		} else if(feat instanceof RawFeature) {
			res = input;
		} else {
			System.err.println("The feature type of " + featureName + " was not recognized.");
		}
		if(res != null) {
			if(res instanceof JSONObject) {
				res = ((JSONObject)res).toString(1);
			} else if (res instanceof JSONArray) {
				res = ((JSONArray)res).toString(1);
			}
			System.out.println();
			System.out.println("===================================");
			System.out.println("=     Feature effective value     =");
			System.out.println("===================================");
			System.out.println(res);
		}
		return 0;
	}

	private Scenario getScenario(String scenarioName) throws ScenarioException {

		NavigableSet<String> resourceLookupPaths = new ConcurrentSkipListSet<>();
		resourceLookupPaths.add(this.scenarioDir);
		resourceLookupPaths.add("/scenarios/");

		URL scenarioFile;
		try {
			scenarioFile = ResourceUtils.getResource(LinkingModule.class, resourceLookupPaths,
			    scenarioName + ".properties");
		} catch (ResourceNotFoundException e) {
			throw new ScenarioNotFoundException(scenarioName, e);
		}

		return new ScenarioFromProperties(scenarioFile, resourceLookupPaths);
	}



}
