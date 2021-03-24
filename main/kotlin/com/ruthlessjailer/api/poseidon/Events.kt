package com.ruthlessjailer.api.poseidon

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.reflections.Reflections
import java.lang.reflect.Modifier

/**
 * @author RuthlessJailer
 */
class Events private constructor(private val executor: EventExecutor, private val listener: Listener, private val priority: EventPriority, private val ignoreCancelled: Boolean) {

	companion object {
		private val LISTENER = object : Listener {}

		fun listen(type: Class<out Event>, executor: EventExecutor, listener: Listener = LISTENER, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false): Events = Events(executor, listener, priority, ignoreCancelled).include(type)
	}

	private val excluded = mutableListOf<Class<out Event>>()
	private val included = mutableListOf<Class<out Event>>()

	fun exclude(type: Class<out Event>): Events {
		excluded.add(type)
		return this
	}

	fun include(type: Class<out Event>): Events {
		included.add(type)
		return this
	}

	fun register() {
		for (type in ArrayList(included)) {
			fill(type)
		}

		included.removeAll(excluded)

		for (type in included) {
			Bukkit.getPluginManager().registerEvent(type, listener, priority, executor, PluginBase.instance, ignoreCancelled)
		}
	}

	private fun fill(type: Class<out Event>) {
		if (!Modifier.isAbstract(type.modifiers)) {
			println(type.canonicalName)
			included.add(type)
			return
		}

		val reflections = Reflections(type.`package`.name)
		val subTypes = reflections.getSubTypesOf(type)

		for (subType in subTypes) {
			fill(subType)
		}

		included.remove(type)//remove abstract type
	}

}