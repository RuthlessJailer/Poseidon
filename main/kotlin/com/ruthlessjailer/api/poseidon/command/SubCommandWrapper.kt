package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.theseus.ReflectUtil
import java.lang.reflect.Method

/**
 * @author RuthlessJailer
 */
data class SubCommandWrapper(val parent: CommandBase, val arguments: List<Argument<*>>, val variableTypes: List<Class<*>>, /*val enumTypes: List<KClass<out Enum<*>>>,*/ val method: Method) {
	companion object {
		val EMPTY = SubCommandWrapper(object : CommandBase(hashCode().toString()) {}, emptyList(), emptyList(), ReflectUtil.getMethod(Companion::class.java, "method"))
		private fun method() {}
	}
}