package com.ruthlessjailer.api.poseidon.task.manager

import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * @author RuthlessJailer
 */
interface TaskManager {


	//Runnable methods


	/**
	 * Runs a task `1` tick later.
	 *
	 * @param runnable the task to run
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun later(runnable: Runnable): BukkitTask = delay(runnable, 1)


	/**
	 * Runs a task given amount of ticks later.
	 *
	 * @param runnable the task to run
	 * @param delay    the delay, it ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun delay(runnable: Runnable, delay: Int): BukkitTask

	/**
	 * Repeats a task every given amount of ticks.
	 *
	 * @param runnable the task to run
	 * @param interval the interval and initial delay, in ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun repeat(runnable: Runnable, interval: Int): BukkitTask

	/**
	 * Runs a task without the async catcher.
	 *
	 * @param runnable the task to run
	 */
	fun unsafe(runnable: Runnable)


	//BukkitRunnable methods


	/**
	 * Runs a task `1` tick later.
	 *
	 * @param bukkit the task to run
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun later(bukkit: BukkitRunnable): BukkitTask = delay(bukkit, 1)


	/**
	 * Runs a task given amount of ticks later.
	 *
	 * @param bukkit the task to run
	 * @param delay  the delay, it ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun delay(bukkit: BukkitRunnable, delay: Int): BukkitTask

	/**
	 * Repeats a task every given amount of ticks.
	 *
	 * @param bukkit   the task to run
	 * @param interval the interval and initial delay, in ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	fun repeat(bukkit: BukkitRunnable, interval: Int): BukkitTask

	companion object {
		val async = AsyncTaskManager()
		val sync = SyncTaskManager()
	}
}