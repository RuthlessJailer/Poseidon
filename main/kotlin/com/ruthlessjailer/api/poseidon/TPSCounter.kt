package com.ruthlessjailer.api.poseidon

import com.ruthlessjailer.api.poseidon.task.manager.TaskManager
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @author RuthlessJailer
 */
class TPSCounter internal constructor() : Runnable {
	private val lock = ReentrantLock()
	private val history = DoubleArray(20)
	private var historyIndex = 0

	@Volatile
	private var tick: Long = 0

	@Volatile
	private var tickStart = System.currentTimeMillis() //ms

	@Volatile
	private var lastPoll = System.currentTimeMillis() //ms

	@Volatile
	private var lastPollTick: Long = 0

	@Volatile
	private var lastPollTPS = 20.0
	override fun run() {
		check(PluginBase.isMainThread()) { "Async" }
		synchronized(lock) {
			tickStart = System.currentTimeMillis()
			tick++
			val timeBetweenTicks = (tickStart - lastPoll).coerceAtLeast(1)
			val tps = POLL_INTERVAL * IDEAL_TIME_BETWEEN_TICKS * IDEAL_TPS / (timeBetweenTicks * POLL_INTERVAL)
			//polling interval * 20 tps * 50 ms = ideal tps over period of polling interval
			//average that by dividing the actual time between ticks times the polling interval to get tps
			history[historyIndex++] = tps
			if (historyIndex >= history.size) { //wrap-around history
				historyIndex = 0
			}
			lastPoll = tickStart
		}
	}

	fun getTPS(): Double {
		synchronized(lock) {
			if (tick < lastPollTick + POLL_INTERVAL) { //don't update unless it's been a few ticks
				return lastPollTPS
			}
			lastPollTick = tick
			lastPollTPS = Arrays.stream(history).average().orElse(20.0)
			return lastPollTPS
		}
	}

	fun isAbove(target: Double): Boolean {
		return if (target <= 0) {
			true
		} else getTPS() >= target
	}

	companion object {
		private const val POLL_INTERVAL = 5
		private const val IDEAL_TIME_BETWEEN_TICKS: Long = 50 //ms
		private const val IDEAL_TPS = 20.0
	}

	init {
		TaskManager.sync.repeat(this, 1)
		Arrays.fill(history, 20.0)
		Chat.debug("TPSCounter", "Initialized TPS Counter.")
	}
}