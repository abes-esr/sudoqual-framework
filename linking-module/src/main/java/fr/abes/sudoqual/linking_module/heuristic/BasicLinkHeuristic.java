/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.heuristic;

import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.MANY_TO_MANY;
import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.MANY_TO_ONE;
import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.ONE_TO_MANY;
import static fr.abes.sudoqual.linking_module.heuristic.BasicHeuristicMode.ONE_TO_ONE;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

import fr.abes.sudoqual.linking_module.Link;
import fr.abes.sudoqual.linking_module.Scenario;
import fr.abes.sudoqual.linking_module.exception.LinkHeuristicException;
import fr.abes.sudoqual.linking_module.exception.UnsupportedHeuristicModeException;
import fr.abes.sudoqual.linking_module.multithreads.CanceledTaskException;
import fr.abes.sudoqual.rule_engine.DiscretCompType;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.util.ConfigurationProperties;
import fr.abes.sudoqual.util.ConfigurationPropertiesException;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class BasicLinkHeuristic implements LinkHeuristic {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasicLinkHeuristic.class);
	public static final String NAME = "default";
	
	protected int DIFF_FROM_THRESHOLD;
	protected int SAME_AS_THRESHOLD;
	protected int SUGGESTED_THRESHOLD;
	protected boolean SUGGESTED_ENABLED;
	protected boolean KEEP_ONLY_BEST_SUGGESTIONS_ENABLED;
	
	protected HeuristicMode mode;

	
	public BasicLinkHeuristic() {
		// default constructor
	}
	
	// /////////////////////////////////////////////////////////////////////////
	//	
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey() {
		return NAME;
	}
	
	@Override
	public void configure(ConfigurationProperties properties) throws LinkHeuristicException {
		try {
			this.setMode(properties.get(Scenario.HEURISTIC_MODE_KEY));
		} catch (UnsupportedHeuristicModeException e) {
			throw new LinkHeuristicException("Error when setting mode", e);
		}
		try {
			this.SAME_AS_THRESHOLD = properties.getInteger(Scenario.VALIDATED_SAME_AS_THRESHOLD_KEY);
    		this.SUGGESTED_THRESHOLD = properties.getInteger(Scenario.SUGGESTED_SAME_AS_THRESHOLD_KEY);
    		this.DIFF_FROM_THRESHOLD = -(properties.getInteger(Scenario.VALIDATED_DIFF_FROM_THRESHOLD_KEY));
    		this.KEEP_ONLY_BEST_SUGGESTIONS_ENABLED = properties.getBoolean(Scenario.KEEP_ONLY_BEST_SUGGESTIONS_KEY);
    		this.SUGGESTED_ENABLED = properties.getBoolean(Scenario.SUGGESTED_ENABLED_KEY);
		} catch (ConfigurationPropertiesException e) {
			throw new LinkHeuristicException("Error during heuristic configuration.", e);
		}
	}

	@Override
	public HeuristicMode getMode() {
		return this.mode;
	}
	
	@Override
	public void setMode(String mode) throws UnsupportedHeuristicModeException {
		try {
			this.mode = Enum.valueOf(BasicHeuristicMode.class, mode);
		} catch (IllegalArgumentException e) {
			mode = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, mode);
			try {
				this.mode =  Enum.valueOf(BasicHeuristicMode.class, mode);
			} catch (IllegalArgumentException e2) {
				throw new UnsupportedHeuristicModeException("The heuristic impl " + this.getClass() + " does not support the specified mode: " + mode);
			}
		}
	}

	@Override
	public Collection<Link> findLinks(PredicateManager manager, RuleEngine engine, Reference source,
		Set<Candidate> allCandidates, AtomicBoolean isCancelled) throws LinkHeuristicException, CanceledTaskException {
		Collection<Link> links = new LinkedList<>();

		int bestProximity = 0;
		Set<Candidate> bestCandidates = new HashSet<>();

		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Looking for links of {}", source.getName());
		}
		for (Candidate candidate : allCandidates) {
			if(isCancelled.get()) {
				throw new CanceledTaskException();
			}
			
			// handle suggested 
			if (!this.KEEP_ONLY_BEST_SUGGESTIONS_ENABLED && candidate.proximityClue >= SUGGESTED_THRESHOLD) {
				if (candidate.proximityClue > bestProximity) {
					// recovery old best candidates
					for (Candidate c : bestCandidates) {
						if(c.proximityClue >= SUGGESTED_THRESHOLD) {
							links.add(Link.create(Link.Type.SUGGESTED, source, c));
						}
					}
				} else if (candidate.proximityClue < bestProximity) {
					// put current candidate as suggested
					links.add(Link.create(Link.Type.SUGGESTED, source, candidate));
				}
			}
			// save best candidates
			if (candidate.proximityClue >= bestProximity) {
				if (candidate.proximityClue > bestProximity) {
					bestProximity = candidate.proximityClue;
					bestCandidates.clear();
				}
				bestCandidates.add(candidate);
			}

			links.addAll(localLinkDeduction(manager.getFeatureManager(), source, candidate));
			
		}

		links.addAll(globalLinkDeduction(manager.getFeatureManager(), allCandidates, bestCandidates, bestProximity, source));
		
		return links;
	}
	
	protected Collection<Link> localLinkDeduction(FeatureManager featureStore, Reference source, Candidate candidate) {
		Collection<Link> newLinks = new LinkedList<>();

		if (MANY_TO_MANY.equals(mode) && candidate.proximityClue >= this.SAME_AS_THRESHOLD/*|| MODE.isOneToMany()*/) {
			newLinks.add(Link.create(Link.Type.SAME_AS, source, candidate));
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("SameAs {}", candidate.target);
			}
		}
		
		// reject worst candidates
		if (candidate.proximityClue <= this.DIFF_FROM_THRESHOLD ) {
			newLinks.add(Link.create(Link.Type.DIFF_FROM, source, candidate));
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Reject {} because proximity value is {}", candidate.target.getName(), candidate.proximityClue);
			}
		}
		
		return newLinks;
	}

	/**
	 * 
	 * @param bestCandidates
	 * @param bestProximity
	 * @param source
	 * @param currentStep
	 * @param createdLinks output parameter - to add new links
	 */
	protected Collection<Link> globalLinkDeduction(FeatureManager store, Set<Candidate> allCandidates, Set<Candidate> bestCandidates, int bestProximity, Reference source) {
		Collection<Link> newLinks = new LinkedList<>();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Best candidates are {}", bestCandidates);
		}
		
		if (bestProximity >= SAME_AS_THRESHOLD) {
			assert !bestCandidates.isEmpty();

			if (MANY_TO_ONE.equals(mode) || ONE_TO_ONE.equals(mode)) {
    			if (bestCandidates.size() == 1) {
    				Candidate candidate = bestCandidates.iterator().next();
    				newLinks.add(Link.create(Link.Type.SAME_AS, source, candidate));
    				if(LOGGER.isDebugEnabled()) {
    					LOGGER.debug("SameAs {}", candidate.target);
    				}
    			} else {
    				newLinks.addAll(this.handleNToOneSameAsConflict(source, bestCandidates));
    			}
			}
    				
		} else if (this.SUGGESTED_ENABLED && bestProximity >= SUGGESTED_THRESHOLD) {
			for (Candidate candidate : bestCandidates) {
				Link l = Link.create(Link.Type.SUGGESTED, source, candidate);
				newLinks.add(l);
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Suggests {}", candidate.target);
				}
			}
		}
		
		return newLinks;
	}

	@Override
	public Set<Link> checkAndHandleOneToNSameAsConflict(Set<Link> actualLinks) {
		if(ONE_TO_MANY.equals(this.getMode()) || ONE_TO_ONE.equals(this.getMode())) {
			Set<Link> realLinks = new HashSet<>();
			Map<Reference, Set<Link>> linkByTarget = new HashMap<>();
			
			for (Link link : actualLinks) {
				if (Link.Type.SAME_AS.equals(link.getType())) {
					Set<Link> set = linkByTarget.get(link.getTarget());
					if(set == null) {
						set = new HashSet<>();
						linkByTarget.put(link.getTarget(), set);
					}
					set.add(link);
				} else {
					realLinks.add(link);
				}
			}
			
			for(Map.Entry<Reference, Set<Link>> e : linkByTarget.entrySet()) {
				if(e.getValue().size() == 1) {
					realLinks.addAll(e.getValue());
				} else if (e.getValue().size() > 1) {
					realLinks.addAll(this.handleOneToNSameAsConflict(e.getKey(), e.getValue()));
				} else {
					LOGGER.error("should not happen");
				}
			}
			return realLinks;
		} else {
			return actualLinks;
		}
	}
	
	/**
	 * Handles links in conflict due to the OneTo* constraint.
	 * @param target the related target
	 * @param actualLinks the set of links related to the specified target
	 * @return a list a link related to the specified target without conflicts.
	 */
	protected Set<Link> handleOneToNSameAsConflict(Reference target, Set<Link> actualLinks) {
		Set<Link> realLinks = new HashSet<>();

		int bestProximity = 0;
		Set<Link> bestLinks = new HashSet<>();
		for (Link link : actualLinks) {
			if (Link.Type.SAME_AS.equals(link.getType())) {
				// handle suggested 
				if (!this.KEEP_ONLY_BEST_SUGGESTIONS_ENABLED && link.getConfidence() >= SUGGESTED_THRESHOLD) {
					if (link.getConfidence() > bestProximity) {
						// recovery old best candidates
						for (Link l : bestLinks) {
							if(l.getConfidence() >= SUGGESTED_THRESHOLD) {
								realLinks.add(downgradeSameAsLinkBecause(l, "There is an other target with best confidence"));
							}
						}
					} else if (link.getConfidence() < bestProximity) {
						// put current candidate as suggested
						realLinks.add(downgradeSameAsLinkBecause(link, "There is an other target with best confidence"));
					}
				}
				// save best candidates
				if (link.getConfidence() >= bestProximity) {
					if (link.getConfidence() > bestProximity) {
						bestProximity = link.getConfidence();
						bestLinks.clear();
					}
					bestLinks.add(link);
				}
			}	
		}
		
		
		if(bestLinks.size() > 1) {
    		Set<Reference> candidates = bestLinks.stream().map(link -> link.getSource()).collect(Collectors.toSet());
    		String candidatesListAsString = toListCandidatesString(candidates);
    		String toAddInWhy = "There are several sources with the same confidence: " + candidatesListAsString;
    		
    		for(Link l : bestLinks) {
    			realLinks.add(downgradeSameAsLinkBecause(l, toAddInWhy));
    				}
    				
    				if(LOGGER.isWarnEnabled()) {
    			LOGGER.warn("Suspicion of duplicated source related to this target {}: {}", target, candidatesListAsString);
    				}
		} else {
			realLinks.addAll(bestLinks);
    			}
		return realLinks;
			}

	/**
	 * Handles links in conflict due to the *ToOne constraint.
	 * @param source the related source
	 * @param bestCandidates the set of best candidates related to the specified source
	 * @return a list a link related to the specified source without conflicts.
	 */
	protected Set<Link> handleNToOneSameAsConflict(Reference source, Set<Candidate> bestCandidates) {
		Set<Link> realLinks = new HashSet<>();
		if(SUGGESTED_ENABLED) {
    		Set<Reference> candidates = bestCandidates.stream().map(candidate -> candidate.target).collect(Collectors.toSet());
    		String candidatesListAsString = toListCandidatesString(candidates);
    		String toAddInWhy = "There are several targets with the same confidence: " + candidatesListAsString;
			for (Candidate candidate : bestCandidates) {
    			Link l = Link.create(Link.Type.SUGGESTED, source, candidate);
    			l = downgradeSameAsLinkBecause(l, toAddInWhy);
    			realLinks.add(l);
				}
    		if(LOGGER.isWarnEnabled()) {
    			LOGGER.warn("Suspicion of duplicated target related to this source {}: {}", source, candidatesListAsString);
			}
		}
		
		return realLinks;
	}

	protected static Link downgradeSameAsLinkBecause(Link l, String toAddInWhy) {
		return Link.create(
				Link.Type.SUGGESTED, 
				l.getSource(),
				l.getTarget(),
				l.getStep(),
				l.getConfidence(), 
				l.getWhySameAs(),
				l.getWhyDiffFrom(),
				toAddInWhy);
	}
	
	protected static Link upgradeSuggestedSameAsLinkBecause(Link l, String toAddInWhy) {
		return Link.create(
				Link.Type.SAME_AS, 
				l.getSource(),
				l.getTarget(),
				l.getStep(),
				l.getConfidence(), 
				l.getWhySameAs(),
				l.getWhyDiffFrom(),
				toAddInWhy);
	}
	
	protected static Link upgradeDiffFromLinkBecause(Link l, String toAddInWhy) {
		l.getWhyDiffFrom().append("heuristic", toAddInWhy);
		return l;
	}

	@Override
	public int computeProximity(Reference source, RuleEngine.Result sameAsClue, Reference target,
	    RuleEngine.Result diffFromClue) {
		int proximity;

		// Calcul du lien rc/ra (on lève les incohérences)
		if (sameAsClue.value() != DiscretCompType.NOT_COMPARABLE && sameAsClue.value() > DiscretCompType.NEUTRAL) {
			// on a un indice de rapprochement
			if (diffFromClue.value() != DiscretCompType.NOT_COMPARABLE
			    && diffFromClue.value() != DiscretCompType.NEUTRAL) {
				// on a aussi un indice d'eloignement
				int delta = computeDeltaSameAsDiffFrom(sameAsClue.value(), diffFromClue.value());
				if (sameAsClue.value() == DiscretCompType.ALWAYS && diffFromClue.value() == DiscretCompType.ALWAYS) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("UNRESOLVABLE CONTRADICTION for pair ({}, {})", source.getName(), target.getName());
					}
					proximity = DiscretCompType.INCOHERENT;
				} else if (sameAsClue.value() == DiscretCompType.ALWAYS) {
					proximity = DiscretCompType.ALWAYS;
				} else if (diffFromClue.value() == DiscretCompType.ALWAYS) {
					proximity = DiscretCompType.NEVER;
				} else if (delta <= 1) { // TODO check meaning
					proximity = DiscretCompType.NEUTRAL;

					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Strong contradiction (sameAs={}, diffFrom={}) for pair ({}, {})\n"
						            + "? :- sameAs(X,Y,{}) because: {}\n"
						            + "? :- diffFrom(X,Y,{}) because: {}\n",
						    sameAsClue.value(), diffFromClue.value(), source.getName(), target.getName(),
						    sameAsClue.value(), sameAsClue.why(), diffFromClue.value(), diffFromClue.why());
					}
				} else if (delta <= 2) { // TODO check meaning
					if (sameAsClue.value() > diffFromClue.value()) {
						proximity = sameAsClue.value() - 1;
					} else {
						proximity = -(diffFromClue.value() - 1);
					}
					if (LOGGER.isWarnEnabled()) {
						LOGGER.info("Medium contradiction (sameAs={}, diffFrom={}) for pair ({}, {})\n"
						            + "? :- sameAs(X,Y,{}) because: {}\n"
						            + "? :- diffFrom(X,Y,{}) because: {}\n",
						    sameAsClue.value(), diffFromClue.value(), source.getName(), target.getName(),
						    sameAsClue.value(), sameAsClue.why(), diffFromClue.value(), diffFromClue.why());
					}

				} else {
					if (LOGGER.isWarnEnabled() && delta <= 3) {
						LOGGER.info("Weak contradiction (sameAs={}, diffFrom={}) for pair ({}, {})\n"
						            + "? :- sameAs(X,Y,{}) because: {}\n"
						            + "? :- diffFrom(X,Y,{}) because: {}\n",
						    sameAsClue.value(), diffFromClue.value(), source.getName(), target.getName(),
						    sameAsClue.value(), sameAsClue.why(), diffFromClue.value(), diffFromClue.why());
					}
					// qu'il y ait ou non une weak contradiction, on conserve la valeur du plus fort
					if (sameAsClue.value() > diffFromClue.value()) {
						proximity = sameAsClue.value();
					} else {
						proximity = -diffFromClue.value();
					}
				}
			} else {
				proximity = sameAsClue.value();
			}
		} else if (diffFromClue.value() == DiscretCompType.NOT_COMPARABLE
		           || diffFromClue.value() == DiscretCompType.NEUTRAL) {
			proximity = DiscretCompType.NEUTRAL;
		} else {
			proximity = - diffFromClue.value();
		}

		return proximity;
	}
	
	protected int computeDeltaSameAsDiffFrom(int sameAsClue, int diffFromClue) {
		return Math.abs(sameAsClue - diffFromClue);
	}
	
	private static String toListCandidatesString(Iterable<Reference> candidates) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Reference  c : candidates) {
			if(!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(c.getName());
		}
		return sb.toString();
	}
}
