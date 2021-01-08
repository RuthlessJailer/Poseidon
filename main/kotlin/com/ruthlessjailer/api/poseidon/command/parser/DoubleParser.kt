package com.ruthlessjailer.api.poseidon.command.parser

/**
 * @author RuthlessJailer
 */
class DoubleParser : ArgumentParser<Double>(type = Double::class.java, format = "double") {

	override fun isValid(argument: String): Boolean = try {
		argument.toDouble()
		true
	} catch (e: NumberFormatException) {
		false
	}

	override fun parse(argument: String): Double = if (!isValid(argument)) -1.0 else argument.toDouble()
	override fun possibilities(): List<String> = emptyList()

}