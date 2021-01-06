package com.ruthlessjailer.api.poseidon.command

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(val format: String, val enumTypes: Array<KClass<out Enum<*>>> = [], val description: String = "")