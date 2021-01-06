package com.ruthlessjailer.api.poseidon.task.manager

import com.ruthlessjailer.api.theseus.Checks
import com.ruthlessjailer.api.theseus.ReflectUtil
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * @author RuthlessJailer
 */
class SyncTaskManager : TaskManager {
	/**
	 * Runs a task given amount of ticks later.
	 *
	 * @param runnable the task to run
	 * @param delay    the delay, it ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	override fun delay(runnable: Runnable, delay: Int): BukkitTask = Bukkit.getScheduler().runTaskLater(Checks.instanceCheck(), runnable, delay.toLong())


	/**
	 * Repeats a task every given amount of ticks.
	 *
	 * @param runnable the task to run
	 * @param interval the interval and initial delay, in ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	override fun repeat(runnable: Runnable, interval: Int): BukkitTask = Bukkit.getScheduler().runTaskTimer(Checks.instanceCheck(), runnable, interval.toLong(), interval.toLong())


	/**
	 * Runs a task without the async catcher.
	 *
	 * @param runnable the task to run
	 */
	override fun unsafe(runnable: Runnable) {
		later {
			ReflectUtil.setField("org.spigotmc.AsyncCatcher", "enabled", null, false)
			try {
				runnable.run()
			} catch (t: Throwable) {
				t.printStackTrace()
			}
			ReflectUtil.setField("org.spigotmc.AsyncCatcher", "enabled", null, true)
		}
	}

	/**
	 * Runs a task given amount of ticks later.
	 *
	 * @param bukkit the task to run
	 * @param delay  the delay, it ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	override fun delay(bukkit: BukkitRunnable, delay: Int): BukkitTask = bukkit.runTaskLater(Checks.instanceCheck(), delay.toLong())


	/**
	 * Repeats a task every given amount of ticks.
	 *
	 * @param bukkit   the task to run
	 * @param interval the interval and initial delay, in ticks
	 *
	 * @return the [BukkitTask] representing the task
	 */
	override fun repeat(bukkit: BukkitRunnable, interval: Int): BukkitTask = bukkit.runTaskTimer(Checks.instanceCheck(), interval.toLong(), interval.toLong())

}