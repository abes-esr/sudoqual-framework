/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public abstract class AbstractTask implements Task {
    
    private final AtomicBoolean done = new AtomicBoolean(false);
    protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
    
    @Override
    public boolean isDone() {
        return this.done.get();
    }
    
    @Override
    public void done() {
        this.done.set(true);
    }
    
    @Override
    public void cancel() {
    	this.isCancelled.set(true);
    }
    
    @Override
    public boolean isCancelled() {
    	return this.isCancelled.get();
    }
}
