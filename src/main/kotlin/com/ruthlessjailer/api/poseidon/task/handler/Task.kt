package com.ruthlessjailer.api.poseidon.task.handler

/**
 * @author RuthlessJailer
 */
abstract class Task(val repeat: Int = -1) {
	@Volatile
	var runs = 0
		private set

	@Synchronized
	fun increment() = runs++
}