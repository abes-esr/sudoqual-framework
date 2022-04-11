/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module;

import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.MANY_TO_ONE;
import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.ONE_TO_MANY;
import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.ONE_TO_ONE;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.linking_module.Link.Type;
import fr.abes.sudoqual.linking_module.exception.LinkingModuleAlgorithmException;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.linking_module.impl.ScenarioWithOptions;
import fr.abes.sudoqual.linking_module.multithreads.ConcurrentUtils;
import fr.abes.sudoqual.linking_module.multithreads.Task;
import fr.abes.sudoqual.linking_module.multithreads.task.LinkingTask;
import fr.abes.sudoqual.linking_module.multithreads.task.UpdateComputedFeatureTask;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;
import fr.abes.sudoqual.rule_engine.predicate.Criterion;
import fr.abes.sudoqual.rule_engine.predicate.Filter;
import fr.abes.sudoqual.rule_engine.predicate.Predicate;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
class LinkingModuleAlgorithmImpl implements LinkingModuleAlgorithm {



	private static final Logger logger = LoggerFactory.getLogger(LinkingModuleAlgorithmImpl.class);

	private static final String FEATURES_KEY = "features";
	private static final String SOURCE_KEY = "source";
	private static final String TARGET_KEY = "target";
	private static final String CRITERION_VALUES_KEY = "criterionValues";
	private static final String NEW_DIFF_FROM_KEY = "newDiffFrom";
	private static final String NEW_SUGGESTED_SAME_AS_KEY = "newSuggestedSameAs";
	private static final String NEW_SAME_AS_KEY = "newSameAs";
	private static final String SAFE_LINKS_KEY = "safeLinks";
	private static final String INIT_KEY = "init";

	private final LinkHeuristic heuristic;
	private final RuleEngine ruleEngine;
	private final BlockingQueue<Task> queue;

	private final Collection<ComputedFeature<?>> computedFeatList;
	private final Collection<Filter> toCleanFilter;
	private final Collection<Criterion> toCleanCriterion;

	private final ScenarioWithOptions scenario;

	private boolean debugMode = false;
	private boolean clusteringMode = false;

	private JSONObject debugData = new JSONObject();

	////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	////////////////////////////////////////////////////////////////////////////

	public LinkingModuleAlgorithmImpl(ScenarioWithOptions scenario, RuleEngine engine, LinkHeuristic heuristic,
	    Collection<ComputedFeature<?>> computedFeatList, Collection<Predicate> predicateList, BlockingQueue<Task> queue) {
		this.scenario = scenario;
		this.ruleEngine = engine;
		this.computedFeatList = computedFeatList;
		this.heuristic = heuristic;
		this.toCleanCriterion = new LinkedList<>();
		this.toCleanFilter = new LinkedList<>();
		buildDependencies(computedFeatList, predicateList);

		this.queue = queue;
	}

	/**
	 * Fulfill toCleanCriterion and toCleanFilter based on the feature used by each
	 * predicate from the predicateList. If a Criterion or a Filter is based on a
	 * computedFeature then it will be add in the corresponding list.
	 *
	 * @param computedFeatures
	 * @param predicateList
	 */
	private void buildDependencies(Collection<ComputedFeature<?>> computedFeatures, Collection<Predicate> predicateList) {
		Collection<String> computedFeatureNames = computedFeatures.stream().map(x -> x.getKey())
		                                                          .collect(Collectors.toList());
		for (Predicate p : predicateList) {
			if (p instanceof Criterion) {
				Criterion crit = (Criterion) p;
				if (CollectionUtils.containsAny(crit.sourceFeatureSet(), computedFeatureNames)
				    || CollectionUtils.containsAny(crit.targetFeatureSet(), computedFeatureNames)) {
					toCleanCriterion.add(crit);
				}
			} else if (p instanceof Filter) {
				Filter filter = (Filter) p;
				if (CollectionUtils.containsAny(filter.featureSet(), computedFeatureNames)) {
					toCleanFilter.add(filter);
				}
			} else {
				throw new Error("Unknown kind of Predicate.");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Set<Link> launch(PredicateManager manager, Collection<Reference> sources, Collection<Reference> targets,
	    Collection<Reference> supports, Collection<Link> safeLinks)
	    throws LinkingModuleAlgorithmException, InterruptedException {
		Map<Reference, List<Reference>> acceptableTargets = createAcceptableTargetMap(sources, targets, clusteringMode);

		Set<Link> allLinks = ConcurrentUtils.createConcurrentSet();
		Map<Reference, Set<Reference>> allSameAs = new HashMap<>();
		Set<Link> newLinks = ConcurrentUtils.createConcurrentSet();
		Map<Reference, Set<Reference>> newSameAs = new HashMap<>();

		Set<Reference> refSetDebugMode = null;
		if(debugMode) {
			refSetDebugMode = new HashSet<>();
			refSetDebugMode.addAll(sources);
			refSetDebugMode.addAll(targets);
			refSetDebugMode.addAll(supports);
			JSONObject jFeatures = new JSONObject();
			for(Reference ref : refSetDebugMode) {
				jFeatures.put(ref.getName(), new JSONObject(manager.getFeatureManager().get(ref).toMap()));
			}
			refSetDebugMode = new HashSet<>();
			refSetDebugMode.addAll(sources);
			refSetDebugMode.addAll(targets);
			JSONObject jInit = new JSONObject();
			jInit.put(FEATURES_KEY, jFeatures);
			debugData.put(INIT_KEY, jInit);

		}
		for (Link link : safeLinks) {
			if (link.getType() == Type.SAME_AS) {
				addLinkTo(link, newSameAs);
				if(debugMode) {
					JSONObject jLink = new JSONObject();
					jLink.put(SOURCE_KEY, link.getSource());
					jLink.put(TARGET_KEY, link.getTarget());
					debugData.getJSONObject(INIT_KEY).append(SAFE_LINKS_KEY, jLink);
				}
			}
		}
		copySameAs(newSameAs,allSameAs);
		this.removeSourceOrTargetAlreadyLinked(sources, targets, safeLinks);

		if(Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		List<Task> toWait = new LinkedList<>();
		int step = 0;

		do {
			++step;
			if(debugMode) {
				debugData.put("step " + step, new JSONObject());
			}
			if (logger.isInfoEnabled()) {
				logger.info("### new step: {}", step);
			}

			this.updateComputedFeatures(manager, newSameAs, allSameAs, supports);
			if(debugMode) {
				JSONObject jFeatures = new JSONObject();
				for(Reference ref : refSetDebugMode) {
					jFeatures.put(ref.getName(), new JSONObject(manager.getFeatureManager().get(ref).toMap()));
				}
				debugData.getJSONObject("step " + step).put(FEATURES_KEY, jFeatures);
			}

			newLinks.clear();
			newSameAs.clear();

			try {
    			for (Reference rc : sources) {
    				LinkingTask t = new LinkingTask(manager, ruleEngine, this.heuristic, rc, acceptableTargets.get(rc), newLinks,
    				                                step);
    				queue.put(t);
    				toWait.add(t);
    			}
    			waitAll(toWait);
			} catch (InterruptedException e) {
				interruptAll(toWait);
				throw e;
			}

			newLinks = this.heuristic.checkAndHandleOneToNSameAsConflict(newLinks);

			if(debugMode) {
				JSONObject jStep = debugData.getJSONObject("step " + step);
				for(Link link : newLinks) {
					switch(link.getType()) {
						case SAME_AS:
							jStep.append(NEW_SAME_AS_KEY, toJSONObject(link));
							break;
						case SUGGESTED:
							jStep.append(NEW_SUGGESTED_SAME_AS_KEY, toJSONObject(link));
							break;
						case DIFF_FROM:
							jStep.append(NEW_DIFF_FROM_KEY, toJSONObject(link));
							break;
					}
				}
				jStep.put(CRITERION_VALUES_KEY, new JSONArray(manager.exportCache()));
			}
			Collection<Link> newValidatedLinks = this.handleNewLinks(newLinks, sources, acceptableTargets);
			allLinks.addAll(newValidatedLinks);
			newValidatedLinks.stream()
				.filter(link -> link.getType().equals(Type.SAME_AS))
				.forEach(link -> addLinkTo(link, newSameAs));

			copySameAs(newSameAs,allSameAs);

		} while (!newSameAs.keySet().isEmpty() && !sources.isEmpty() && !supports.isEmpty() && !Thread.currentThread().isInterrupted());

		if(Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		if (this.scenario.isSuggestedEnabled()) {
			for (Link link : newLinks) {
				if (link.getType() == Type.SUGGESTED) {
					allLinks.add(link);
				}
			}
		}

		return allLinks;
	}


	/**
	 * Removes some candidates and dispatch new link
	 * @param newLinks
	 * @param targets
	 * @param sources
	 * @param acceptableTargets
	 * @return selected links to keep (validated links)
	 */
	private Collection<Link> handleNewLinks(Set<Link> newLinks, Collection<Reference> sources, Map<Reference, List<Reference>> acceptableTargets) {
		Collection<Link> linksToReturn = new LinkedList<>();

			for (Link link : newLinks) {
				if (logger.isInfoEnabled()) {
					logger.info("created link: {}", link);
				}
				switch (link.getType()) {
					case SAME_AS:
						if (MANY_TO_ONE.equals(heuristic.getMode()) || ONE_TO_ONE.equals(heuristic.getMode())) {
							// remove sources for all targets
							sources.remove(link.getSource());
							// save last suggested elements for the current sources
						if (!this.scenario.isKeepOnlyBestSuggestionsEnabled()) {
								for (Link l : newLinks) {
									if (l.getType() == Type.SUGGESTED && l.getSource().equals(link.getSource())) {
									linksToReturn.add(l);
									}
								}
							}
						}

						if(ONE_TO_MANY.equals(heuristic.getMode()) || ONE_TO_ONE.equals(heuristic.getMode())) {
							// remove target for all sources
						for(List<Reference> values : acceptableTargets.values()) {
								values.remove(link.getTarget());
							}
							// save last suggested elements for the current target
						if (!this.scenario.isKeepOnlyBestSuggestionsEnabled()) {
								for (Link l : newLinks) {
									if (l.getType() == Type.SUGGESTED && l.getTarget().equals(link.getTarget())) {
									linksToReturn.add(l);
									}
								}
							}
						} else {
						// MANY_TO_*
						// only remove target for the current source because is already linked it
						acceptableTargets.get(link.getSource()).remove(link.getTarget());
						}

					linksToReturn.add(link);
						break;
					case SUGGESTED:
						// Nothing to do
						break;
					case DIFF_FROM:
					linksToReturn.add(link);
					acceptableTargets.get(link.getSource()).remove(link.getTarget());
						break;
				}
			}

		return linksToReturn;
	}

	@Override
	public void enableDebugMode(boolean enable) {
		this.debugMode = enable;
	}

	@Override
	public JSONObject getDebugData() {
		return this.debugData;
	}

	////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	////////////////////////////////////////////////////////////////////////////


	private void updateComputedFeatures(PredicateManager manager, Map<Reference, Set<Reference>> newSameAs,
	    Map<Reference, Set<Reference>> allSameAs, Collection<Reference> supports)
	    throws InterruptedException {
		manager.cleanCriterions(newSameAs.keySet(), this.toCleanCriterion);
		manager.cleanFilters(newSameAs.keySet(), this.toCleanFilter);

		List<Task> toWait = new LinkedList<>();
		long infoTime = 0;
		if (logger.isInfoEnabled()) {
			infoTime = System.currentTimeMillis();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("* Force ComputedFeature ");
		}

		try {
    		for (Map.Entry<Reference, Set<Reference>> e : newSameAs.entrySet()) {
    			if(CollectionUtils.containsAny(supports, e.getValue())) {
    				Reference key = e.getKey();
	    			Task task = new UpdateComputedFeatureTask(manager.getFeatureManager(), key, this.computedFeatList,
	    			                                          newSameAs.get(key), allSameAs.get(key));
	    			queue.put(task);
	    			toWait.add(task);
    			}
    		}
    		waitAll(toWait);
		} catch (InterruptedException e) {
			interruptAll(toWait);
			throw e;
		}

		if (logger.isInfoEnabled()) {
			logger.info("ComputedFeature compute time: {}", System.currentTimeMillis() - infoTime);
		}

	}

	/*
	 * private void clearComputedFeatures(PredicateManager manager, Set<Reference>
	 * toUpdate) { manager.cleanCriterions(toUpdate, this.toCleanCriterion);
	 * manager.cleanFilters(toUpdate, this.toCleanFilter);
	 * manager.getFeatureStore().cleanFeatures(toUpdate, this.computedFeatList); }
	 *
	 * private static void addLinksInfoInto(Collection<Link> safeLinks,
	 * FeatureStore store) { for(Link link : safeLinks) {
	 * store.push(link.getSource(), link.getType().toString(),
	 * link.getTarget().toString()); store.push(link.getTarget(),
	 * link.getType().toString(), link.getSource().toString()); } }
	 */

	private void removeSourceOrTargetAlreadyLinked(Collection<Reference> sources, Collection<Reference> targets,
		Collection<Link> safeLinks) {
		for (Link link : safeLinks) {
			if (link.getType() == Type.SAME_AS
					&& sources.contains(link.getSource())
					&& targets.contains(link.getTarget())) {
				// a same-as link between a source and a target exists (not source or target from support only)
        		if(ONE_TO_ONE.equals(heuristic.getMode())) {
        				sources.remove(link.getSource());
        				targets.remove(link.getTarget());
        		} else if(ONE_TO_MANY.equals(heuristic.getMode())) {
        				targets.remove(link.getTarget());
        		} else if(MANY_TO_ONE.equals(heuristic.getMode())) {
        				sources.remove(link.getSource());
        		// } else if(heuristic.getMode().isManyToMany()) {
        			// do nothing
        		}
			}
		}
	}

	private static void copySameAs(Map<Reference, Set<Reference>> newSameAs, Map<Reference, Set<Reference>> allSameAs) {
		for(Map.Entry<Reference, Set<Reference>> e : newSameAs.entrySet()) {
			Set<Reference> set = allSameAs.get(e.getKey());
			if(set == null) {
				set = new HashSet<>();
				allSameAs.put(e.getKey(), set);
			}
			set.addAll(e.getValue());
		}
	}

	private static void addLinkTo(Link link, Map<Reference, Set<Reference>> newSameAs) {
		Reference source = link.getSource();
		Reference target = link.getTarget();
		{
			Set<Reference> set = newSameAs.get(source);
			if (set == null) {
				set = new HashSet<>();
				newSameAs.put(source, set);
			}
			set.add(target);
		}
		{
			Set<Reference> set = newSameAs.get(target);
			if (set == null) {
				set = new HashSet<>();
				newSameAs.put(target, set);
			}
			set.add(source);
		}
	}

	/**
	 * Wait all specified tasks
	 *
	 * @param toWait
	 *                   a List of tasks to wait for
	 * @throws InterruptedException
	 */
	private static void waitAll(List<Task> toWait) throws InterruptedException {
		for (Task t : toWait) {
			synchronized (t) {
				if (!t.isDone()) {
					t.wait(); // wait() release lock on t
				}
			}
		}
	}

	/**
	 * Interruts all specified tasks
	 *
	 * @param toWait
	 *                   a List of tasks to wait for
	 * @throws InterruptedException
	 */
	private static void interruptAll(List<Task> toWait) {
		for (Task t : toWait) {
			t.cancel();
		}
	}

	/**
	 * Return a Map containing for each RC, a copy of the specified RAList.
	 *
	 * @param sources
	 * @param targets
	 * @param clusteringMode
	 * @return
	 */
	private static Map<Reference, List<Reference>> createAcceptableTargetMap(Collection<Reference> sources,
	    Collection<Reference> targets, boolean clusteringMode) {
		Map<Reference, List<Reference>> map = new ConcurrentHashMap<>(sources.size() * 2);

		for (Reference source : sources) {
			LinkedList<Reference> list = new LinkedList<>(targets); // all targets are acceptable
			if(clusteringMode) {
				list.removeAll(map.keySet()); // except all previous
			}
			list.remove(source); // except source itself

			map.put(source, list);
		}
		return map;
	}

	private static final String TYPE_LINK_KEY = "type";
	private static final String SOURCE_LINK_KEY = "source";
	private static final String TARGET_LINK_KEY = "target";
	private static final String WHY_LINK_KEY = "why";
	private static final String CONFIDENCE_LINK_KEY = "confidence";
	private JSONObject toJSONObject(Link link) {
		JSONObject res = new JSONObject();
		res.put(TYPE_LINK_KEY, link.getType().toString());
		res.put(SOURCE_LINK_KEY, link.getSource().getName());
		res.put(TARGET_LINK_KEY, link.getTarget().getName());
		JSONObject why = new JSONObject();
		why.put("sameAsClue", link.getWhySameAs());
		why.put("diffFromClue", link.getWhyDiffFrom());
		res.put(WHY_LINK_KEY, why);
		res.put(CONFIDENCE_LINK_KEY, link.getConfidence());

		return res;
	}
	/*
	 * private void removeRcAlreadyLinked(Collection<Reference> RCList,
	 * Collection<Link> LinkList) { for (Link link : LinkList) { if (link.getType()
	 * == Link.Type.SAME_AS) { RCList.remove(link.getSource()());
	 * link.getRA().addSafeLink(link.getRC()); } } }
	 *
	 * private void removeRaAlreadyImpossible(Map<Long, List<IReference>>
	 * acceptableRa, Collection<Link> LinkList) { for (Link link : LinkList) { if
	 * (link.getType() == Link.Type.DIFFERENT_FROM) {
	 * link.getRC().addImpossibleLink(link.getRA());
	 * acceptableRa.get(link.getRC().getId()).remove(link.getRA()); } } }
	 */

	@Override
	public void enableClusteringMode(boolean enable) {
		this.clusteringMode  = enable;
	}

	/*
	 * private void forceSuperFeatures(FeatureStore store, Collection<Reference>
	 * RAList) throws InterruptedException { List<Task> toWait = new LinkedList<>();
	 * long infoTime = 0; if(logger.isInfoEnabled()) { infoTime =
	 * System.currentTimeMillis(); } if(logger.isDebugEnabled()) {
	 * logger.debug("* Force ComputedFeature "); } for(Reference ra : RAList) {
	 * ProvideComputedFeatureTask task = new ProvideComputedFeatureTask(store, ra,
	 * this.computedFeatList); queue.put(task); toWait.add(task); } waitAll(toWait);
	 * if(logger.isInfoEnabled()) { logger.info("ComputedFeature compute time: " +
	 * (System.currentTimeMillis() - infoTime)); } }
	 */

}
