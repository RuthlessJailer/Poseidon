package com.ruthlessjailer.api.poseidon.task.handler

/**
 * @author RuthlessJailer
 */
enum class QueuePriority {
	/**
	 * Gets added to the start of the queue.
	 *
	 *
	 * Highest priority.
	 */
	IMMEDIATE,

	/**
	 * Gets added to the end of the queue.
	 *
	 *
	 * Standard priority.
	 */
	NORMAL,

	/**
	 * Gets added to the secondary queue.
	 *
	 *
	 * Lowest priority.
	 */
	SECONDARY
}