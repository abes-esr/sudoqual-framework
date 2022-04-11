/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.abes.sudoqual.linking_module.Link;
import fr.abes.sudoqual.linking_module.exception.LinkHeuristicException;
import fr.abes.sudoqual.linking_module.heuristic.Candidate;
import fr.abes.sudoqual.linking_module.heuristic.LinkHeuristic;
import fr.abes.sudoqual.linking_module.multithreads.AbstractTask;
import fr.abes.sudoqual.linking_module.multithreads.CanceledTaskException;
import fr.abes.sudoqual.linking_module.multithreads.Task;
import fr.abes.sudoqual.rule_engine.PredicateManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.RuleEngine;
import fr.abes.sudoqual.rule_engine.exception.RuleEngineException;

/**
 * This task implements a part of the main job of the SudoQual application. It
 * executes {@link SafeLinkHeuristic heuristic} on a RC to link or exclude some
 * RA. If the RC has been linked to an RA by this task, then it adds the RA into
 * toClean and the RC into toRemove.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class LinkingTask extends AbstractTask implements Task {

	private static final Logger logger = LoggerFactory.getLogger(LinkingTask.class);
	private static String SAME_AS = "sameAs";
	private static String DIFF_FROM = "diffFrom";
	
	private final Reference source;
	private final RuleEngine engine;
	private final List<Reference> candidates;
	private final Set<Link> createdLinks;
	private final LinkHeuristic heuristic;
	private final int step;
	private final PredicateManager manager;

	/**
	 * 
	 * @param store
	 * @param heuristic
	 * @param source
	 * @param engine
	 * @param cxa
	 * @param createdLinks
	 *                         a set of Links in which we have to add newly created
	 *                         links.
	 * @param step
	 */
	public LinkingTask(PredicateManager manager, RuleEngine engine, LinkHeuristic heuristic, Reference source,
	    List<Reference> candidates, Set<Link> createdLinks, int step) {
		this.heuristic = heuristic;
		this.source = source;
		this.engine = engine;
		this.candidates = candidates;
		this.createdLinks = createdLinks;
		this.manager = manager;
		this.step = step;
	}

	@Override
	public RETURN_STATUS exec() {
		if (logger.isDebugEnabled()) {
			logger.debug("LinkingTask for {}", source.getName());
		}
		if(this.isCancelled()) {
			return RETURN_STATUS.CANCELLED;
		}

		// call heuristic and gets results
		Collection<Link> resultat;
		try {
			resultat = this.findLinks(manager, engine, source, candidates);
		} catch (CanceledTaskException e) {
			return RETURN_STATUS.CANCELLED;
		} catch (LinkHeuristicException e) {
			logger.error("An error occured during heuristic call: ", e);
			return RETURN_STATUS.ERROR;
		}

		for (Link link : resultat) {
			link.setStep(step);
			createdLinks.add(link);
		}

		return RETURN_STATUS.SUCCESS;
	}

	@Override
	public String toString() {
		return "TASK rc linking " + source.getId();
	}
	
	private Collection<Link> findLinks(PredicateManager manager, RuleEngine engine, Reference source,
	    Collection<Reference> targetList) throws LinkHeuristicException, CanceledTaskException {
		Set<Candidate> allCandidates = new HashSet<>();
		for (Reference target : targetList) {
			if(isCancelled.get()) {
				throw new CanceledTaskException();
			}
			Candidate candidate = check(target);
			allCandidates.add(candidate);
		}
		
		return this.heuristic.findLinks(manager, engine, source, allCandidates, super.isCancelled);
	}
	
	protected Candidate check(Reference target) throws LinkHeuristicException {
		Candidate candidate = new Candidate(target);
		try {
			candidate.sameAsResult = engine.check(manager, SAME_AS, source, target);
			candidate.diffFromResult = engine.check(manager, DIFF_FROM, source, target);
			if(logger.isDebugEnabled()) {
				logger.debug("Raw result for {}: (SAME_AS: {}) / (DIFF_FROM: {})", target.getName(), candidate.sameAsResult, candidate.diffFromResult);
			}
		} catch (RuleEngineException e) {
			throw new LinkHeuristicException("Error while querying rule engine: ", e);
		}
		candidate.proximityClue = heuristic.computeProximity(source, candidate.sameAsResult, target, candidate.diffFromResult);
		if(logger.isDebugEnabled()) {
			logger.debug("Proximity for {}: {}", target.getName(), candidate.proximityClue);
		}
		return candidate;
	}

}
