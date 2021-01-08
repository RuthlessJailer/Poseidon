package com.ruthlessjailer.api.poseidon.command.parser

/**
 * @author RuthlessJailer
 */
class IntParser : ArgumentParser<Int>(type = Int::class.java, format = "int") {

	override fun isValid(argument: String): Boolean = try {
		argument.toInt()
		true
	} catch (e: NumberFormatException) {
		false
	}

	override fun parse(argument: String): Int = if (!isValid(argument)) -1 else argument.toInt()
	override fun possibilities(): List<String> = emptyList()


}