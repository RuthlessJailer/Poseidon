package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.poseidon.command.parser.ArgumentParser
import com.ruthlessjailer.api.poseidon.command.parser.EnumParser
import com.ruthlessjailer.api.theseus.Common

/**
 * @author RuthlessJailer
 */
data class Argument<T>(val parser: ArgumentParser<T>, val isVariable: Boolean, val possibilities: List<String> = parser.possibilities()) {

//	fun update(possibilities: List<String>) {
//		this.possibilities = possibilities
//	}

	companion object {
		internal val EMPTY = Argument(ArgumentParser.EMPTY, true)
		internal val GENERIC = Argument(EnumParser.GENERIC, true)
	}

	fun isValid(argument: String): Boolean = if (isVariable) parser.isValid(argument) else possibilities.contains(argument)
	fun isValidIgnoreCase(argument: String): Boolean = if (isVariable) parser.isValidIgnoreCase(argument) else Common.convert(possibilities, String::toLowerCase).contains(argument.toLowerCase())

}