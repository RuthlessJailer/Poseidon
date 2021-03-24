package com.ruthlessjailer.api.poseidon.command.parser

import com.ruthlessjailer.api.poseidon.command.SubCommandManager


val DESCRIPTION_PATTERN = Regex("(<[=A-Z_a-z.0-9]+>)?")
val FORMAT_PATTERN = Regex("[=A-Z_a-z.0-9]+")

/**
 * @author RuthlessJailer
 */
abstract class ArgumentParser<T>(protected val format: Regex, val type: Class<T>, val requiresClass: Boolean = false) {

	companion object {
		internal val EMPTY = object : ArgumentParser<Void>(Regex(hashCode().toString()), Void.TYPE, false) {
			override fun isValid(argument: String): Boolean = false
			override fun parse(argument: String): Void? = null
			override fun possibilities(): List<String> = emptyList()
		}
	}

//	init {
//		if (!FORMAT_PATTERN.matches(format) /*&& !EnumParser::class.java.isAssignableFrom(javaClass)*/) {//enum parser is an exception
//			throw IllegalArgumentException("Illegal format '$format'. Formats must match pattern '${FORMAT_PATTERN.pattern}'.")
//		}
//	}

	val pattern = Regex("$format$DESCRIPTION_PATTERN")

	open fun isFormatValid(format: String): Boolean = pattern.matches(format)
	open fun isValidIgnoreCase(argument: String) = isValid(argument.toLowerCase())
	abstract fun isValid(argument: String): Boolean
	abstract fun parse(argument: String): T?
	abstract fun possibilities(): List<String>

	fun register(): ArgumentParser<T> {
		SubCommandManager.registerArgumentParser(type, this)
		return this
	}

}