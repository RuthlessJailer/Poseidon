package com.ruthlessjailer.api.poseidon.command.parser

/**
 * @author RuthlessJailer
 */
class StringParser : ArgumentParser<String>(Regex("%str"), String::class.java) {

	override fun isValid(argument: String): Boolean = true

	override fun parse(argument: String): String = argument

	override fun possibilities(): List<String> = emptyList()
}