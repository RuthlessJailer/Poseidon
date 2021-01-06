package com.ruthlessjailer.api.poseidon.task.handler

import com.ruthlessjailer.api.poseidon.task.manager.TaskManager
import com.ruthlessjailer.api.theseus.Common
import com.ruthlessjailer.api.theseus.PluginBase
import lombok.Getter
import lombok.SneakyThrows
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.collections.HashMap

/**
 * @author RuthlessJailer
 */
class SyncFutureHandler internal constructor() : FutureHandler, Runnable {
	@Getter
	private var targetTPS = 18
	private var last = System.currentTimeMillis() //time//tps is low but last tick was ok//we're behind; take that from this tick's allocate//we're on schedule

	//1 tick = 50ms
	//goal is to go for 2 ticks (or whatever target tps is set to)
	//2 ticks is 100ms or 1/10 of a sec
	//0 is a perfect tick, negative we're behind, and positive ahead
	private var allocate = DEFAULT_ALLOCATE //ms
			.toLong()
		get() {
			//1 tick = 50ms
			//goal is to go for 2 ticks (or whatever target tps is set to)
			//2 ticks is 100ms or 1/10 of a sec
			val current = System.currentTimeMillis()
			val diff = DEFAULT_ALLOCATE + (last - current.also { last = it })
			//0 is a perfect tick, negative we're behind, and positive ahead
			if (diff == 0L) { //we're on schedule
				field = DEFAULT_ALLOCATE.toLong().coerceAtMost(field + 1)
			} else if (diff < 0) { //we're behind; take that from this tick's allocate
				field = (DEFAULT_ALLOCATE / 10).toLong().coerceAtLeast(field + diff)
			} else if (Common.hasTPSCounter() && !Common.getTPSCounter().isAbove(targetTPS.toDouble())) { //tps is low but last tick was ok
				field = (DEFAULT_ALLOCATE / 10).toLong().coerceAtLeast(field - 1)
			}
			return field
		}
	private val tasks: Deque<SyncTask<*>> = ConcurrentLinkedDeque()
	private val secondary: Deque<SyncTask<*>> = ConcurrentLinkedDeque()
	private val repeating: MutableMap<UUID, SyncTask<*>> = HashMap()

	override fun run() {
		check(PluginBase.isMainThread()) { "Async" }
		executeRepeating()
		execute(if (tasks.isEmpty()) secondary else tasks, last, allocate)
	}

	fun setTargetTPS(tps: Int) {
		require(tps <= MAX_TPS) { "TPS cannot exceed $MAX_TPS" }
		targetTPS = tps
	}

	private fun executeRepeating() {
		val iterator: MutableIterator<Map.Entry<UUID, SyncTask<*>>> = repeating.entries.iterator()
		var entry: Map.Entry<UUID, SyncTask<*>>
		while (iterator.hasNext()) {
			entry = iterator.next()
			val task = entry.value
			if (task.runs >= task.repeat && task.repeat != -1) {
				iterator.remove() //cancel the task concurrently
			} else {
				task.increment() //increment runs and execute
				task.rawFuture.get()
			}
		}
	}

	private fun execute(deque: Deque<SyncTask<*>>, start: Long, allocate: Long) {
		var task: SyncTask<*>?
		synchronized(deque) {
			do {
				task = deque.poll()

				if (System.currentTimeMillis() >= task?.`when`!!) {
					task!!.future.run()
				}

			} while (System.currentTimeMillis() - start <= allocate && deque.size >= 1)
		}
	}

	/**
	 * Runs the task `100` milliseconds later.
	 *
	 * @param supplier the task to run
	 *
	 * @return the [Future] representation of the [Supplier]
	 */
	override fun <T> supply(supplier: Supplier<T?>): Future<T?> = supply(supplier, 100)

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param supplier the task to run
	 * @param delay    the delay in milliseconds
	 *
	 * @return the [Future] representation of the [Supplier]
	 */
	override fun <T> supply(supplier: Supplier<T?>, delay: Long): Future<T?> = supply(supplier, delay, QueuePriority.NORMAL)

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param supplier the task to run
	 * @param delay    the delay in milliseconds
	 * @param priority the [QueuePriority] for the task
	 *
	 * @return the [Future] representation of the [Supplier]
	 */
	@SneakyThrows
	fun <T> supply(supplier: Supplier<T?>, delay: Long, priority: QueuePriority): Future<T?> {
		val task = SyncTask(supplier, System.currentTimeMillis() + delay)
		if (PluginBase.isMainThread()) {
			return task.future
		}
		when (priority) {
			QueuePriority.IMMEDIATE -> synchronized(tasks) { tasks.offerFirst(task) }
			QueuePriority.NORMAL -> synchronized(tasks) { tasks.offer(task) }
			QueuePriority.SECONDARY -> synchronized(secondary) { secondary.offer(task) }
		}
		return task.future
	}

	/**
	 * Runs the task `100` milliseconds later.
	 *
	 * @param callable the task to run
	 *
	 * @return the [Future] representation of the [Callable]
	 */
	override fun <T> call(callable: Callable<T?>): Future<T?> = call(callable, 100)

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param callable the task to run
	 * @param delay    the amount of milliseconds to wait before running the task
	 *
	 * @return the [Future] representation of the [Callable]
	 */
	override fun <T> call(callable: Callable<T?>, delay: Long): Future<T?> = call(callable, delay, QueuePriority.NORMAL)
	
	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param callable the task to run
	 * @param delay    the delay in milliseconds
	 * @param priority the [QueuePriority] for the task
	 *
	 * @return the [Future] representation of the [Callable]
	 */
	fun <T> call(callable: Callable<T?>, delay: Long, priority: QueuePriority): Future<T?> {
		val task = SyncTask({
								try {
									callable.call()
								} catch (e: Exception) {
									throw UnsupportedOperationException("Exception in callable.", e)
								}
							}, System.currentTimeMillis() + delay)
		if (PluginBase.isMainThread()) {
			return task.future
		}
		when (priority) {
			QueuePriority.IMMEDIATE -> synchronized(tasks) { tasks.offerFirst(task) }
			QueuePriority.NORMAL -> synchronized(tasks) { tasks.offer(task) }
			QueuePriority.SECONDARY -> synchronized(secondary) { secondary.offer(task) }
		}
		return task.future
	}

	/**
	 * Runs the task `100` milliseconds later.
	 *
	 * @param runnable the task to run
	 * @param value    the value
	 *
	 * @return the [Future] representation of the [Runnable]
	 */
	override fun <T> run(runnable: Runnable, value: T?): Future<T?> = run(runnable, value, 100)

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param runnable the task to run
	 * @param value    the value
	 * @param delay    the amount of milliseconds to wait before running the task
	 *
	 * @return the [Future] representation of the [Runnable]
	 */
	override fun <T> run(runnable: Runnable, value: T?, delay: Long): Future<T?> = run(runnable, value, delay, QueuePriority.NORMAL)

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param runnable the task to run
	 * @param value    the value
	 * @param delay    the delay in milliseconds
	 * @param priority the [QueuePriority] for the task
	 *
	 * @return the [Future] representation of the [Runnable]
	 */
	fun <T> run(runnable: Runnable, value: T, delay: Long, priority: QueuePriority): Future<T?> {
		val task = SyncTask({
								runnable.run()
								value
							}, System.currentTimeMillis() + delay)
		if (PluginBase.isMainThread()) {
			return task.future
		}
		when (priority) {
			QueuePriority.IMMEDIATE -> synchronized(tasks) { tasks.offerFirst(task) }
			QueuePriority.NORMAL -> synchronized(tasks) { tasks.offer(task) }
			QueuePriority.SECONDARY -> synchronized(secondary) { secondary.offer(task) }
		}
		return task.future
	}

	/**
	 * Runs the task given amount of milliseconds later.
	 *
	 * @param consumer the task to run
	 * @param interval the amount of milliseconds to wait in between executions
	 *
	 * @return the id of the task
	 *
	 * @see FutureHandler.cancel
	 */
	override fun repeat(consumer: Consumer<UUID>, interval: Long): UUID = repeat(consumer, interval, -1)


	/**
	 * Runs the task given amount of milliseconds later for a given amount of iterations.
	 *
	 * @param consumer the task to run
	 * @param interval the amount of milliseconds to wait in between executions
	 * @param count    the amount of times to repeat the task before auto-cancelling
	 *
	 * @return the id of the task
	 *
	 * @see FutureHandler.cancel
	 */
	override fun repeat(consumer: Consumer<UUID>, interval: Long, count: Int): UUID {
		val id = UUID.randomUUID()
		val supplier: Supplier<Any?> = Supplier {
			consumer.accept(id)
			null
		}
		synchronized(repeating) { repeating.put(id, SyncTask(supplier, count)) }
		return id
	}

	/**
	 * Cancel a repeating task.
	 *
	 * @param id the id of the task, obtained from [scheduling a repeating task][FutureHandler.repeat]
	 *
	 * @see FutureHandler.repeat
	 */
	override fun cancel(id: UUID) {
		synchronized(repeating) { repeating.remove(id) }
	}

	companion object {
		private const val MAX_TPS = 20
		private const val DEFAULT_ALLOCATE = 50 //ms
		private const val MIN_ALLOCATE = 5 //ms
	}

	init {
		TaskManager.sync.repeat(this, 1)
	}
}