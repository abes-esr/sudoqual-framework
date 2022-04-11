/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads;

/**
 * This task will kill a Consumer.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class PoisonTask extends AbstractTask implements Task {

    @Override
    public RETURN_STATUS exec() {
        return Task.RETURN_STATUS.SUCCESS;
    } 

    @Override
    public String toString() {
        return "TASK POISON";
    }
}
