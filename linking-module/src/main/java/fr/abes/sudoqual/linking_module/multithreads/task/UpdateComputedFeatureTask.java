/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads.task;

import java.util.Collection;
import java.util.Set;

import fr.abes.sudoqual.linking_module.multithreads.AbstractTask;
import fr.abes.sudoqual.linking_module.multithreads.Task;
import fr.abes.sudoqual.rule_engine.FeatureManager;
import fr.abes.sudoqual.rule_engine.Reference;
import fr.abes.sudoqual.rule_engine.feature.ComputedFeature;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class UpdateComputedFeatureTask extends AbstractTask implements Task {

	private final Reference ref;
	private final Collection<ComputedFeature<?>> featList;
	private final FeatureManager store;
	private final Set<Reference> newSameAs;
	private final Set<Reference> allSameAs;

	public UpdateComputedFeatureTask(FeatureManager store, Reference ref, Collection<ComputedFeature<?>> featList,
	    Set<Reference> newSameAs, Set<Reference> allSameAs) {
		this.ref = ref;
		this.featList = featList;
		this.store = store;
		this.newSameAs = newSameAs;
		this.allSameAs = allSameAs;
	}

	@Override
	public RETURN_STATUS exec() {
		if (this.isCancelled()) {
			return RETURN_STATUS.CANCELLED;
		}
		store.updateComputedFeatures(ref, featList, newSameAs, allSameAs);
		return RETURN_STATUS.SUCCESS;
	}

	@Override
	public String toString() {
		return "TASK provide comptued features for " + this.ref.getId();
	}
}
