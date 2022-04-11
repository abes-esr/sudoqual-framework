/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public interface Task {

	enum RETURN_STATUS {
						SUCCESS, ERROR, CANCELLED
	}

	/**
	 * Executes the task job.
	 * 
	 * @return 0 if the task terminated correctly. Other values is for error
	 *         reporting.
	 */
	RETURN_STATUS exec();

	/**
	 * Sets this task as done, should only be called by {@link Consumer}.
	 */
	void done();

	/**
	 * Asks if this task is done.
	 * 
	 * @return true if this task is done, false otherwise.
	 */
	boolean isDone();

	@Override
	String toString();

	/**
	 * Cancels the task
	 */
	void cancel();

	boolean isCancelled();
}