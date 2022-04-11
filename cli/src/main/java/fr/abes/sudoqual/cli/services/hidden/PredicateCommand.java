package fr.abes.sudoqual.cli.services.hidden;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import fr.abes.sudoqual.linking_module.impl.BusinessClassLoaderHelper;
import fr.abes.sudoqual.linking_module.impl.IntrospectionBusinessClassLoader;
import fr.abes.sudoqual.linking_module.impl.ScenarioFromProperties;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.exception.FeatureManagerException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.impl.FeatureManagerImpl;
import fr.abes.sudoqual.rule_engine.impl.ReferenceImpl;
import fr.abes.sudoqual.rule_engine.impl.lumbago.PredicateManagerImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.json.JSONObjects;

@Parameters(commandDescription = "run a predicate", hidden = true)
public class PredicateCommand {
	public static final String NAME = "predicate";

	private static final Logger logger = LoggerFactory.getLogger(PredicateCommand.class);

	@Parameter(description = "<scenario name> <predicate name>", required = true, arity = 2)
	private List<String> args = new ArrayList<String>();

	@Parameter(names = {"-d", "--data"}, description = "data on which execute the predicate. "
			+ "If the predicate is a criterion data must be a JSON object containing a source and a target key")
	private String data;

	private String scenarioDir;

	public int run(SudoqualCommander cmd) {
		this.scenarioDir = cmd.getScenarioDir();
		String scenarioName = args.get(0);
		String predicateName = args.get(1);

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
			logger.error("", e);
			return 1;
		}
		Predicate predicate = loader.createPredicate(predicateName);
		Map<String, Feature> featureMap = null;
		try {
			featureMap = BusinessClassLoaderHelper.loadFeaturesFromPredicates(loader, Collections.singleton(predicate), true);
		} catch (BusinessClassException e) {
			logger.error("An error occured when loading features associated to the predicate: ", e);
			return 1;
		}
		assert featureMap != null;

		JSONObject o = JSONObjects.from("{ \"data\": " + data + "}"); // TODO backspaces double quotes

		JSONObject input = o.optJSONObject("data");
		if(input == null) {
			logger.error("Provided data is not a JSON object.");
			return 1;
		}

		System.out.println();
		System.out.println("===========================");
		System.out.println("=    Predicate details    =");
		System.out.println("===========================");
		System.out.println("name: " + predicateName);
		if(predicate instanceof Filter) {
			System.out.println("kind: filter");
			Filter filter = (Filter) predicate;
			System.out.println("feature set: " + filter.featureSet());
		} else if(predicate instanceof Criterion) {
			System.out.println("kind: criterion");
			Criterion criterion = (Criterion) predicate;
			System.out.println("comparison type: " + criterion.getComparisonType());
			System.out.println("source feature set: " + criterion.sourceFeatureSet());
			System.out.println("target feature set: " + criterion.targetFeatureSet());
		} else {
			System.out.println("kind: unknown");
		}
		System.out.println("input:");
		System.out.println(input.toString(1));


		if(predicate instanceof Filter) {
			JSONObject features = new JSONObject();
			Reference ref = new ReferenceImpl("uri:fake");
			try {
				features.put(ref.getName(), prepareInput(loader, featureMap, input));
			} catch (Exception e) {
				logger.error("An exception occured during preprocessing input.", e);
				return 1;
			}
			PredicateManager manager = createPredicateManager(features, featureMap);
			System.out.println("pre-processed input:");
			System.out.println(manager.getFeatureManager().get("uri:fake").toString(1));

			boolean res = manager.check((Filter)predicate, ref);
			System.out.print("filter value: ");
			System.out.println(res);
		} else if(predicate instanceof Criterion) {
			JSONObject source = input.optJSONObject("source");
			if(source == null) {
				logger.error("The provided source value is not a JSON object: " + input.get("source"));
			}
			try {
				source = prepareInput(loader, featureMap, source);
			} catch (Exception e) {
				logger.error("An exception occured during preprocessing source input.", e);
			}
			JSONObject target = input.optJSONObject("target");
			if(target == null) {
				logger.error("The provided target value is not a JSON object: " + input.get("target"));
			}
			try {
				target = prepareInput(loader, featureMap, target);
			} catch (Exception e) {
				logger.error("An exception occured during preprocessing target input.", e);
				return 1;
			}
			if(source == null || target == null) {
				logger.error("Something went wrong during preprocessing source or target input.");
				return 1;
			}
			Reference sourceRef = new ReferenceImpl("source");
			Reference targetRef = new ReferenceImpl("target");
			JSONObject json = new JSONObject();
			json.put("source", source);
			json.put("target", target);

			PredicateManager manager = createPredicateManager(json, featureMap);
			System.out.println("pre-processed input:");
			System.out.println("\"source\":");
			System.out.println(manager.getFeatureManager().get("source").toString(1));
			System.out.println("\"target\":");
			System.out.println(manager.getFeatureManager().get("target").toString(1));

			int res = manager.compare((Criterion)predicate, sourceRef, targetRef);
			System.out.print("criterion value: ");
			System.out.println(res);
		} else {
			logger.error("The predicate type of " + predicateName + " was not recognized.");
			return 1;
		}

		return 0;
	}


	private static JSONObject prepareInput(BusinessClassLoader loader, Map<String, Feature> featureMap, JSONObject input) throws Exception {
		JSONObject res = new JSONObject();
		for(String key : input.keySet()) {
			Feature feature = featureMap.get(key);
			Object o = input.get(key);
			if(feature instanceof ComputedFeature) {
				if(o instanceof JSONArray) {
					Collection<JSONObject> preparedFeatures = CLIUtils.prepareFeature(loader, (ComputedFeature<?>)feature, (JSONArray) o);
					res.put(key, ((ComputedFeature<?>)feature).compute(preparedFeatures));
				} else {
					throw new Exception("Error on feature data of " + key + ": computed feature value must be provided as JSONArray. Found: " + o);
				}
			} else {
				res.put(key, o);
			}
		}
		return res;
	}

	private PredicateManager createPredicateManager(JSONObject featuresJSON, Map<String, Feature> featureMap) {
		FeatureManager store = new FeatureManagerDebug(featuresJSON, featureMap);
		ConcurrentMap<String, ComputedFeature<?>> computedFeatureMap = createComputedFeatureMap(featureMap.values());
		PredicateManagerImpl manager = new PredicateManagerImpl(store, computedFeatureMap);
		return manager;
	}

	private static class FeatureManagerDebug extends FeatureManagerImpl {
		public FeatureManagerDebug(JSONObject json, Map<String, Feature> featureMap) {
			super(json, featureMap);
			Iterator<String> it = json.keys();
			while(it.hasNext()) {
				String reference = it.next();
				JSONObject rawValues = json.getJSONObject(reference);
				JSONObject processedValues = new JSONObject();
				processedValues.put(FeatureManager.URI_KEY, reference);

				for(String featureKey : featureMap.keySet()) {
					try {
						Feature feature = featureMap.get(featureKey);
						if(feature instanceof ComputedFeature) {
							this.put(new ReferenceImpl(reference), featureKey, rawValues.opt(featureKey));
						}
					} catch (Exception e) {
						throw new FeatureManagerException("An error occured during processing feature " + featureKey + " from reference " + reference, e);
					}
				}
			}
		}

	}

	private static ConcurrentMap<String, ComputedFeature<?>> createComputedFeatureMap(
	    Collection<Feature> featureInstances) {
		ConcurrentMap<String, ComputedFeature<?>> map = new ConcurrentHashMap<>(featureInstances.size() * 2);
		for (Feature feat : featureInstances) {
			if (feat instanceof ComputedFeature) {
				map.put(feat.getKey(), (ComputedFeature<?>) feat);
			}
		}
		return map;
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
