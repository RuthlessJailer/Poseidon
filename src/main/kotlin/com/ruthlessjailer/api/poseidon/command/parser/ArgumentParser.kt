package com.ruthlessjailer.api.poseidon.command.parser

import com.ruthlessjailer.api.poseidon.command.SubCommandManager


val DESCRIPTION_PATTERN = Regex("(<[A-Z_a-z0-9]+>)?")
val FORMAT_PATTERN = Regex("[a-z]+")

/**
 * @author RuthlessJailer
 */
abstract class ArgumentParser<T>(val requiresClass: Boolean = false, val type: Class<T>, format: String) {

	init {
		if (!FORMAT_PATTERN.matches(format)) {
			throw IllegalArgumentException("Illegal format '$format'. Formats must match pattern '${FORMAT_PATTERN.pattern}'.")
		}
	}

	val pattern = Regex("%$format$DESCRIPTION_PATTERN")

	open fun isFormatValid(format: String): Boolean = pattern.matches(format)
	open fun isValidIgnoreCase(argument: String) = isValid(argument.toLowerCase())
	abstract fun isValid(argument: String): Boolean
	abstract fun parse(argument: String): T?
	abstract fun possibilities(): List<String>

	fun register() {
		SubCommandManager.registerArgumentParser(type, this)
	}

}