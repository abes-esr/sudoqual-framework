/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import fr.abes.sudoqual.linking_module.impl.ScenarioFromProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.linking_module.exception.ExtractDataFromInputException;
import fr.abes.sudoqual.linking_module.exception.FeaturesDataDoesNotFulfillRequirements;
import fr.abes.sudoqual.linking_module.exception.HeuristicNotFoundException;
import fr.abes.sudoqual.linking_module.exception.JSONInputValidationException;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleAlgorithmException;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleException;
import fr.abes.sudoqual.linking_module.exception.LoadingRuleEngineException;
import fr.abes.sudoqual.linking_module.exception.ScenarioException;
import fr.abes.sudoqual.linking_module.exception.ScenarioNotFoundException;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.linking_module.impl.BusinessClassLoaderHelper;
import fr.abes.sudoqual.linking_module.impl.IntrospectionBusinessClassLoader;
import fr.abes.sudoqual.linking_module.impl.ScenarioWithOptions;
import fr.abes.sudoqual.linking_module.multithreads.Consumer;
import fr.abes.sudoqual.linking_module.multithreads.PoisonTask;
import fr.abes.sudoqual.linking_module.multithreads.Task;
import fr.abes.sudoqual.linking_module.util.JSONValidatorUtils;
import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.feature.Feature;
import fr.abes.sudoqual.rule_engine.impl.DiscretCompTypeImpl;
import fr.abes.sudoqual.rule_engine.impl.FeatureManagerImpl;
import fr.abes.sudoqual.rule_engine.impl.ReferenceImpl;
import fr.abes.sudoqual.rule_engine.impl.lumbago.PredicateManagerImpl;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;
import fr.abes.sudoqual.util.ResourceNotFoundException;
import fr.abes.sudoqual.util.ResourceUtils;
import fr.abes.sudoqual.util.Strings;
import fr.abes.sudoqual.util.json.JSONValidationReport;

/**
 * This implementation of {@link LinkingModule} is a thread safe implementation.
 *
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class LinkingModuleImpl implements LinkingModule {


	private static final Logger logger = LoggerFactory.getLogger(LinkingModuleImpl.class);

	private static final String SCENARIO_KEY = "scenario";
	private static final String OPTIONS_KEY = "options";
	private static final String FEATURES_KEY = "features";
	private static final String INITIAL_LINKS_KEY = "initialLinks";
	private static final String SOURCES_KEY = "sources";
	private static final String TARGETS_KEY = "targets";
	private static final String SUPPORTS_KEY = "supports";
	private static final String SAFE_LINKS_KEY = "safeLinks";
	private static final String CRITERION_VALUES_KEY = "criterionValues";
	private static final String DIFF_FROM_KEY = "diffFrom";
	private static final String SAME_AS_KEY = "sameAs";
	private static final String DEBUG_KEY = "debug";
	private static final String COMPUTED_LINKS_KEY = "computedLinks";
	private static final String METADATA_KEY = "metadata";
	private static final String VERSION_KEY = "version-framework";
	private static final String VERSION_SCENARIO_KEY = "version-scenario";


	private static final String TYPE_LINK_KEY = "type";
	private static final String SOURCE_LINK_KEY = "source";
	private static final String TARGET_LINK_KEY = "target";
	private static final String WHY_LINK_KEY = "why";
	private static final String CONFIDENCE_LINK_KEY = "confidence";
	private static final String STEP_LINK_KEY = "step";


	private final NavigableSet<String> resourceLookupPaths;

	private BlockingQueue<Task> queue;
	private final int NB_THREADS;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public LinkingModuleImpl(int nbThreads) {
		this.NB_THREADS = nbThreads;

		this.resourceLookupPaths = new ConcurrentSkipListSet<>();
		this.resourceLookupPaths.add("/" + Config.SCENARIO_DIR);

		this.queue = new LinkedBlockingQueue<>();
		createConsumers(NB_THREADS, this.queue);
	}

	@Override
	public void close() {
		LinkingModuleImpl.destroyConsumers(NB_THREADS, queue);
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void registerPath(String path) {
		this.resourceLookupPaths.add(path);
	}

	@Override
	public String execute(String input) throws LinkingModuleException, InterruptedException {
		assert input != null;

		return this.execute(Strings.toInputStream(input, StandardCharsets.UTF_16), StandardCharsets.UTF_16);
	}

	@Override
	public String execute(InputStream input, Charset charset) throws LinkingModuleException, InterruptedException {
		assert input != null;

		JSONObject res = this.execute(new JSONObject(new JSONTokener(new InputStreamReader(input, charset))));
		return res.toString();
	}

	@Override
	public JSONObject execute(JSONObject input) throws LinkingModuleException, InterruptedException {
		assert input != null;

		JSONValidationReport report = JSONValidatorUtils.validateInput(input);
		if (!report.isSuccess()) {
			logger.info("invalid input: {}", report.toString());
			throw new JSONInputValidationException("The input does not fulfill the requirements:\n"
			                                       + report.toString());
		}

		return this.process(input);
	}

    @Override
    public JSONObject generateSchema(String scenarioName) throws LinkingModuleException {
        Scenario scenario = getScenario(scenarioName);
        Map<String, Feature> featureMap = createFeatureMap(scenario);
        JSONObject jsonSchemaObject = null;
        try {
            URL linkInputSchemaURL = ResourceUtils.getResource(LinkingModule.class, Config.RESOURCE_DIR, "schema-input.json");
            try (InputStream stream = linkInputSchemaURL.openStream()) {
                jsonSchemaObject = new JSONObject(new JSONTokener(new InputStreamReader(stream,
                    Config.CHARSET)));
            }
        } catch (ResourceNotFoundException | IOException e) {
            throw new Error("Error during validator creation. ", e);
        }
        if(jsonSchemaObject != null) {
            JSONObject properties = jsonSchemaObject.getJSONObject("properties");
            properties.put("scenario", new JSONObject("{ 'type': 'string', 'pattern': '^" + scenarioName + "$' }"));
            properties.put("features", generateFeaturePartOfSchema(featureMap));
            return jsonSchemaObject;
        } else {
            throw new LinkingModuleException("Unable to read schema-input.json.");
        }
    }

    @Override
    public List<String> listFeatures(String scenarioName) throws LinkingModuleException {
        Scenario scenario = getScenario(scenarioName);
        Map<String, Feature> featureMap = createFeatureMap(scenario);
        return featureMap.values().stream().filter(f -> !(f instanceof ComputedFeature)).map(f -> f.getKey()).collect(Collectors.toList());
    }

    private JSONObject generateFeaturePartOfSchema(Map<String, Feature> featureMap) {
        JSONObject features = new JSONObject();
        features.put("type", "object");

        JSONObject patternProperties = new JSONObject();
        features.put("patternProperties", patternProperties);

        JSONObject pattern = new JSONObject();
        patternProperties.put("^.*$", pattern);

        JSONObject properties = new JSONObject();
        pattern.put("type", "object");
        pattern.put("additionalProperties", false);
        pattern.put("properties", properties);
        for(Map.Entry<String, Feature> e : featureMap.entrySet()) {
            if(!(e.getValue() instanceof ComputedFeature)) {
                properties.put(e.getKey(), e.getValue().getValidationSchema());
            }
        }
        return features;
    }

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

    private Object lock = new Object();
	private JSONObject process(JSONObject input) throws LinkingModuleException, InterruptedException {
		ScenarioWithOptions scenario = getScenarioWithOptions(input);

        LinkHeuristic heuristic;
        RuleEngine engine;
        Set<Predicate> predicateInstances;
        ConcurrentMap<String, ComputedFeature<?>> computedFeatureMap;
        PredicateManager manager;

        synchronized (lock) {
            BusinessClassLoader businessClassLoader = new IntrospectionBusinessClassLoader(scenario.getBusinessClassPackageName(),
                scenario);
            if (businessClassLoader.getFeatureNames().isEmpty()
                && businessClassLoader.getPredicateNames().isEmpty()
                && businessClassLoader.getLinkHeuristicNames().isEmpty()) {
                throw new ScenarioException("No business class found under specified package: "
                    + scenario.getBusinessClassPackageName());
            }

            URL dlpFile = getRuleFile(scenario);
            Set<String> predicateNames = BusinessClassLoaderHelper.extractPredicateNames(dlpFile);
            predicateInstances = businessClassLoader.createPredicates(predicateNames);
            Map<String, Feature> featureMap = BusinessClassLoaderHelper.loadFeaturesFromPredicates(businessClassLoader, predicateInstances, scenario.isUndeclaredRawFeatureEnabled());
            computedFeatureMap = createComputedFeatureMap(featureMap.values());

            if (scenario.isDataValidationEnabled()) {
                // TODO use {@link #generateSchema(String) instead}
                checkDataValidity(input, featureMap);
            }
            manager = createPredicateManager(input, featureMap, computedFeatureMap);

            engine = getRuleEngine(dlpFile, predicateInstances, featureMap.values());
            heuristic = businessClassLoader.createLinkHeuristic(scenario.getHeuristicName());
        }
		if (heuristic == null) {
			throw new HeuristicNotFoundException("The LinkHeuristic "
			                                     + scenario.getHeuristicName()
			                                     + " was not found.");
		}
		heuristic.configure(scenario);

		Map<String, Reference> referenceMap = new HashMap<>();
		Collection<Reference> sources = getSources(input, referenceMap);
		Collection<Reference> targets = getTargets(input, referenceMap);
		Collection<Reference> supports = getSupports(input, referenceMap);
		Collection<Link> safeLinks = getSafeLinks(input, referenceMap);

		LinkingModuleAlgorithm core = new LinkingModuleAlgorithmImpl(scenario, engine, heuristic,
		                                                             computedFeatureMap.values(), predicateInstances,
		                                                             queue);

		core.enableDebugMode(scenario.isDebugEnabled());
		core.enableClusteringMode(input.get(TARGETS_KEY).equals(SOURCES_KEY));

		Set<Link> results = null;
		try {
			results = core.launch(manager, sources, targets, supports, safeLinks);
		} catch (LinkingModuleAlgorithmException e) {
			throw new LinkingModuleAlgorithmException("Error while processing main algorithm.", e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(manager.getFeatureManager().toString());
		}
		Map<String, Predicate> predicateMap = createPredicateMap(predicateInstances);
		return produceJSONOutput(input, results, scenario, manager, predicateMap, core.getDebugData());
	}

	private PredicateManager createPredicateManager(JSONObject input, Map<String, Feature> featureMap, ConcurrentMap<String, ComputedFeature<?>> computedFeatureMap) {
		JSONObject featuresJSON = input.getJSONObject(FEATURES_KEY);
		populateInitialLinks(input.optJSONArray(INITIAL_LINKS_KEY), featuresJSON);

		// create feature and predicate manager
		FeatureManager store = new FeatureManagerImpl(featuresJSON, featureMap);
		PredicateManagerImpl manager = new PredicateManagerImpl(store, computedFeatureMap);
		if (input.has(CRITERION_VALUES_KEY)) {
			// recover previous values
			manager.load(input.getJSONArray(CRITERION_VALUES_KEY));
		}

		return manager;
	}

	protected JSONObject produceJSONOutput(JSONObject input, Set<Link> results, ScenarioWithOptions scenario,
	    PredicateManager manager, Map<String, Predicate> predicateMap, JSONObject debugData) {
		JSONObject res = new JSONObject();

		{ // write metadata
			JSONObject metadata = new JSONObject();
			metadata.put(VERSION_KEY, Config.VERSION);
			metadata.put(VERSION_SCENARIO_KEY, scenario.get(Scenario.VERSION_KEY));
			metadata.put(SCENARIO_KEY, input.get(SCENARIO_KEY));

			JSONObject options = input.optJSONObject(OPTIONS_KEY);
			if (options != null) {
				metadata.put(OPTIONS_KEY, options);
			}
			res.put(METADATA_KEY, metadata);
		}

		{ // write computedLinks
			JSONArray computedLinks = new JSONArray();
			for (Link link : results) {
				computedLinks.put(toJSONObject(link, predicateMap));
			}
			res.put(COMPUTED_LINKS_KEY, computedLinks);
		}

		if (scenario.isExportCriterionValuesEnabled()) {
			res.put(CRITERION_VALUES_KEY, exportCriterionValues(manager));
		}

		if (scenario.isDebugEnabled()) {
			res.put(DEBUG_KEY, debugData);
		}

		assert JSONValidatorUtils.validateOutput(res)
		                         .isSuccess() : "Produced JSON output does not pass validation:"
		                                        + JSONValidatorUtils.validateOutput(res).getErrorMessage();
		return res;
	}

	protected JSONObject toJSONObject(Link link, Map<String, Predicate> predicateMap) {
		JSONObject res = new JSONObject();
		res.put(TYPE_LINK_KEY, link.getType().toString());
		res.put(SOURCE_LINK_KEY, link.getSource().getName());
		res.put(TARGET_LINK_KEY, link.getTarget().getName());
		res.put(WHY_LINK_KEY, produceWhyOutput(link.getWhySameAs(), link.getWhyDiffFrom(), predicateMap));
		res.put(CONFIDENCE_LINK_KEY, handleSpecialValuesToString(link.getConfidence()));
		res.put(STEP_LINK_KEY, link.getStep());

		return res;
	}

	private Map<String, Feature> createFeatureMap(Scenario scenario) throws LinkingModuleException {
        synchronized (lock) {
            BusinessClassLoader businessClassLoader = new IntrospectionBusinessClassLoader(scenario.getBusinessClassPackageName(),
                scenario);
            if (businessClassLoader.getFeatureNames().isEmpty()
                && businessClassLoader.getPredicateNames().isEmpty()
                && businessClassLoader.getLinkHeuristicNames().isEmpty()) {
                throw new ScenarioException("No business class found under specified package: "
                    + scenario.getBusinessClassPackageName());
            }

            URL dlpFile = getRuleFile(scenario);
            Set<String> predicateNames = BusinessClassLoaderHelper.extractPredicateNames(dlpFile);
            Set<Predicate> predicateInstances = businessClassLoader.createPredicates(predicateNames);
            return BusinessClassLoaderHelper.loadFeaturesFromPredicates(businessClassLoader, predicateInstances, scenario.isUndeclaredRawFeatureEnabled());
        }
    }

	// /////////////////////////////////////////////////////////////////////////
	// STATIC METHODS
	// /////////////////////////////////////////////////////////////////////////



	private static Map<String, Predicate> createPredicateMap(Set<Predicate> predicateInstances) {
		Map<String, Predicate> map = new HashMap<>();
		for(Predicate p : predicateInstances) {
			map.put(p.getKey(), p);
		}
		return map;
	}


	private static JSONObject produceWhyOutput(JSONObject whySameAs, JSONObject whyDiffFrom, Map<String, Predicate> predicateMap) {
		enhanceWhy(whySameAs, predicateMap);
		enhanceWhy(whyDiffFrom, predicateMap);

		JSONObject res = new JSONObject();
		res.put("sameAsClue", whySameAs);
		res.put("diffFromClue", whyDiffFrom);

		return res;
	}

	private static void enhanceWhy(JSONObject why, Map<String, Predicate> predicateMap) {
		Set<String> sourceFeatureSet = new HashSet<>();
		Set<String> targetFeatureSet = new HashSet<>();
		for(Object s : why.getJSONArray("predicates")) {
			Predicate pred = predicateMap.get(s);
			if(pred != null) {
				if (pred instanceof Criterion) {
					Criterion crit = (Criterion) pred;
					sourceFeatureSet.addAll(crit.sourceFeatureSet());
					targetFeatureSet.addAll(crit.targetFeatureSet());
				}
			} else {
				// dimension - nothing to do
			}
		}
		JSONObject whyFeature = new JSONObject();
		whyFeature.put("source", sourceFeatureSet);
		whyFeature.put("target", targetFeatureSet);
		why.put("features", whyFeature);
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

	private static Collection<Link> getSafeLinks(JSONObject input, Map<String, Reference> referenceMap) {
		Collection<Link> res = new LinkedList<>();
		if (input.has(SAFE_LINKS_KEY)) {
			JSONArray linkArray = input.getJSONArray(SAFE_LINKS_KEY);
			for (Object o : linkArray) {
				if (o instanceof JSONObject) {
					JSONObject linkObject = (JSONObject) o;
					res.add(createLinkFrom(linkObject, referenceMap));
				}
			}
		}
		return res;
	}

	private static Link createLinkFrom(JSONObject linkObject, Map<String, Reference> referenceMap) {
		String source = linkObject.getString(SOURCE_LINK_KEY);
		String target = linkObject.getString(TARGET_LINK_KEY);
		String type = linkObject.getString(TYPE_LINK_KEY);
		Link res = null;

		if (SAME_AS_KEY.equals(type)) {
			res = new LinkImpl(Link.Type.SAME_AS, referenceMap.get(source), referenceMap.get(target));
		} else if (DIFF_FROM_KEY.equals(type)) {
			res = new LinkImpl(Link.Type.DIFF_FROM, referenceMap.get(source), referenceMap.get(target));
		}
		return res;
	}

	private static Collection<Reference> getSupports(JSONObject input, Map<String, Reference> referenceMap)
	    throws LinkingModuleException {
		final String errorMsg = "Fails to extract the list of support references.";
		if (input.has(SUPPORTS_KEY)) {
			Object o = input.get(SUPPORTS_KEY);
			if (o instanceof String) {
				switch ((String) o) {
					case SOURCES_KEY:
						return getSources(input, referenceMap);
					case TARGETS_KEY:
						return getTargets(input, referenceMap);
					default:
						throw new ExtractDataFromInputException(errorMsg);

				}
			} else if (o instanceof JSONArray) {
				try {
					JSONArray array = (JSONArray) o;
					return getReferencesFrom(array, referenceMap);
				} catch (JSONException e) {
					throw new ExtractDataFromInputException(errorMsg);
				}
			} else {
				throw new ExtractDataFromInputException(errorMsg);
			}
		} else {
			return Collections.emptyList();
		}
	}

	private static Collection<Reference> getSources(JSONObject input, Map<String, Reference> referenceMap)
	    throws LinkingModuleException {
		try {
			JSONArray array = input.getJSONArray(SOURCES_KEY);
			return getReferencesFrom(array, referenceMap);
		} catch (JSONException e) {
			throw new ExtractDataFromInputException("Fails to extract the list of source references.");
		}
	}

	private static Collection<Reference> getTargets(JSONObject input, Map<String, Reference> referenceMap)
	    throws LinkingModuleException {
		final String errorMsg = "Fails to extract the list of target references.";
		Object o = input.get(TARGETS_KEY);
		if (o instanceof String) {
			switch ((String) o) {
				case SOURCES_KEY:
					return getSources(input, referenceMap);
				default:
					throw new ExtractDataFromInputException(errorMsg);

			}
		} else if (o instanceof JSONArray) {
			try {
				JSONArray array = (JSONArray) o;
				return getReferencesFrom(array, referenceMap);
			} catch (JSONException e) {
				throw new ExtractDataFromInputException(errorMsg);
			}
		} else {
			throw new ExtractDataFromInputException(errorMsg);
		}
	}

	private static Collection<Reference> getReferencesFrom(JSONArray array, Map<String, Reference> referenceMap) {
		Collection<Reference> refs = new LinkedList<>();
		for (Object e : array) {
			if (e instanceof String) {
				Reference ref = referenceMap.get(e);
				if (ref == null) {
					ref = new ReferenceImpl((String) e);
					referenceMap.put((String) e, ref);
				}
				refs.add(ref);
			} else {
				throw new JSONException("Expected a String but is a {}" + e.getClass().toString());
			}
		}
		return refs;
	}

	private static String getDataValidityMessage(JSONObject input, Map<String, Feature> features) {
        JSONObject data = input.getJSONObject(FEATURES_KEY);
        Iterator<String> refIt = data.keys();
        boolean isError = false;
        StringBuilder message = new StringBuilder();

        while (refIt.hasNext()) {
            String ref = refIt.next();
            JSONObject o = data.getJSONObject(ref);
            Iterator<String> featIt = o.keys();
            while (featIt.hasNext()) {
                String featName = featIt.next();
                Feature feature = features.get(featName);
                if (feature != null) {
                    JSONValidationReport report = feature.validate(o.get(featName));
                    if (!report.isSuccess()) {
                        if (isError) {
                            message.append(",\n");
                        }
                        isError = true;
                        message.append(featName)
                            .append(" value for ")
                            .append(ref)
                            .append(" is not valid: ")
                            .append(o.get(featName))
                            .append("\n")
                            .append(report.getErrorMessage());
                    }
                }
            }
        }
        if(isError) {
            return message.toString();
        } else {
            return null;
        }
    }

	/**
	 * Throws a FeaturesDataDoesNotFulfillRequirements if some feature requirements
	 * are not fulfilled.
	 *
	 * @param input
	 * @param features
	 * @throws FeaturesDataDoesNotFulfillRequirements
	 */
	private static void checkDataValidity(JSONObject input, Map<String, Feature> features)
	    throws FeaturesDataDoesNotFulfillRequirements {
	    String msg = getDataValidityMessage(input, features);
		if (msg != null) {
			throw new FeaturesDataDoesNotFulfillRequirements(msg);
		}
	}

	protected static JSONArray exportCriterionValues(PredicateManager manager) {
		return new JSONArray(manager.exportCache());
	}

	/*
	 * protected static FeatureStore createFeatureStore(JSONObject input,
	 * ConcurrentMap<String, Feature> computedFeatureMap) { return new
	 * FeatureStoreImpl(input.getJSONObject(FEATURES_KEY), computedFeatureMap); }
	 */

	private URL getRuleFile(Scenario scenario) throws ScenarioException {
		try {
			return ResourceUtils.getResource(LinkingModule.class, this.resourceLookupPaths.descendingSet(),
			    scenario.getRuleSetFileName());
		} catch (IllegalArgumentException | ResourceNotFoundException e) {
			throw new ScenarioException("The rule file associated to the scenario was not found: "
			                            + scenario.getRuleSetFileName(), e);
		}
	}

	private static RuleEngine getRuleEngine(URL dlpFile, Collection<Predicate> predicates, Collection<Feature> features)
	    throws LoadingRuleEngineException {
		if (logger.isInfoEnabled()) {
			logger.info("Load rule engine for following rule set {}", dlpFile.getPath());
		}
		RuleEngine engine = null;
		try {
			engine = RuleEngine.create(new InputStreamReader(dlpFile.openStream()), predicates);
		} catch (RuleEngineException | IOException e) {
			throw new LoadingRuleEngineException(e);
		}
		return engine;
	}

    private ScenarioWithOptions getScenarioWithOptions(JSONObject input) throws ScenarioException {
        String scenarioName = input.getString(SCENARIO_KEY);
        URL scenarioFile;
        try {
            scenarioFile = ResourceUtils.getResource(LinkingModule.class, this.resourceLookupPaths.descendingSet(),
                scenarioName + ".properties");
        } catch (ResourceNotFoundException e) {
            throw new ScenarioNotFoundException(scenarioName, e);
        }
        JSONObject options = input.optJSONObject(OPTIONS_KEY);
        return new ScenarioWithOptions(scenarioFile, options, this.resourceLookupPaths);
    }

	private Scenario getScenario(String scenarioName) throws ScenarioException {
		URL scenarioFile;
		try {
			scenarioFile = ResourceUtils.getResource(LinkingModule.class, this.resourceLookupPaths.descendingSet(),
			    scenarioName + ".properties");
		} catch (ResourceNotFoundException e) {
			throw new ScenarioNotFoundException(scenarioName, e);
		}
		return new ScenarioFromProperties(scenarioFile, this.resourceLookupPaths);
	}

	@SuppressWarnings("PMD.AvoidThreadGroup")
	private static void createConsumers(int nbThread, BlockingQueue<Task> queue) {
		ThreadGroup group = new ThreadGroup("SudoQual-Consumers");
		for (int i = 0; i < nbThread; ++i) {
			Consumer c = new Consumer(group, queue, i);
			c.start();
		}
	}

	private static void destroyConsumers(int nbThread, BlockingQueue<Task> queue) {
		for (int i = 0; i < nbThread; ++i) {
			queue.offer(new PoisonTask());
		}
	}

	/**
	 * Extract information from sameAs initialLinks to create initial link feature.
	 * @param initialLinks
	 * @param features
	 */
	private static void populateInitialLinks(JSONArray initialLinks, JSONObject features) {
		if(initialLinks == null) {
			return;
		}
		for(Object o : initialLinks) {
			if(o instanceof JSONObject) {
				JSONObject link = (JSONObject)o;
				if(SAME_AS_KEY.equals(link.getString(TYPE_LINK_KEY))) {
					String source = link.getString(SOURCE_LINK_KEY);
					String target = link.getString(TARGET_LINK_KEY);
					populateInitialLink(features, source, target);
				}
			}
		}
	}

	private static void populateInitialLink(JSONObject features, String source, String target) {
		// add source into target
		JSONObject targetFeatures = features.optJSONObject(target);
		if(targetFeatures != null) {
			targetFeatures.append(LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY, source);
		}
		// add target into source
		JSONObject sourceFeatures = features.optJSONObject(source);
		if(sourceFeatures != null) {
			sourceFeatures.append(LinkingModule.INITIAL_LINKS_SPECIAL_FEATURE_KEY, target);
		}
	}

	private static final DiscretCompType basicCompType = new DiscretCompTypeImpl(true,0,true,0,true);
	private static Object handleSpecialValuesToString(int confidence) {
		try {
			return basicCompType.toString(confidence);
		} catch (RuleEngineException e) {
			// do nothing
		}
		return confidence;
	}

}
