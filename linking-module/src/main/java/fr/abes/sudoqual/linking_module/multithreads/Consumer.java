/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public class Consumer extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

	private final BlockingQueue<Task> queue;
	private final int id;

	private volatile boolean active = true;

	public Consumer(BlockingQueue<Task> queue, int id) {
		super("Consumer-" + id);
		this.queue = queue;
		this.id = id;
	}

	public Consumer(ThreadGroup group, BlockingQueue<Task> queue, int id) {
		super(group, "Consumer-" + id);
		this.queue = queue;
		this.id = id;
	}

	public void end() {
		this.active = false;
	}

	@Override
	public void run() {

		while (active) {
			Task t = null;
			try {
				t = queue.take();
				if (t != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("Consumer {}: on task {}", this.id, t.toString());
					}
					if (t instanceof PoisonTask) {
						active = false;
					} else {
						Task.RETURN_STATUS ret = t.exec();
						switch (ret) {
							case ERROR:
								logger.error("Consumer {} : error on task {}", this.id,  t);
								break;
							case CANCELLED:
								if (logger.isInfoEnabled()) {
									logger.info("Consumer {}: the task {} was cancelled.", this.id, t);
								}
								break;
							case SUCCESS:
							default:
								break;
						}
					}
				}
			} catch (Exception ex) {
				logger.warn("Consumer " + this.id + ": An error occured: ", ex);
			} finally {
				if (t != null) {
					synchronized (t) {
						t.done();
						t.notifyAll();
					}
				}
			}
		}
	}
}
