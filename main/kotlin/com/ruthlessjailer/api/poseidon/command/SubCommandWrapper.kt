package com.ruthlessjailer.api.poseidon.command

import java.lang.reflect.Method

/**
 * @author RuthlessJailer
 */
data class SubCommandWrapper(val parent: CommandBase, val arguments: List<Argument<*>>, val variableTypes: List<Class<*>>, /*val enumTypes: List<KClass<out Enum<*>>>,*/ val method: Method)