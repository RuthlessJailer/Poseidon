package com.ruthlessjailer.api.poseidon.command.parser

/**
 * @author RuthlessJailer
 */
abstract class BooleanParser(input: String) : ArgumentParser<Boolean>(Regex("%bool=[a-zA-Z]+[.][a-zA-Z]+"), Boolean::class.java) {

	init {
		TODO("This will not work with the current system.")
	}

	private val t: String
	private val f: String

	init {
		val tf = input.split("=")[1].split(".")
		t = tf[0]
		f = tf[1]
	}

	override fun isValid(argument: String): Boolean = argument == t || argument == f

	override fun parse(argument: String): Boolean? = when (argument) {
		t    -> true
		f    -> false
		else -> null
	}

	override fun possibilities(): List<String> = listOf(t, f)

	class OnOff : BooleanParser("%bool=on.off")
	class YesNo : BooleanParser("%bool=yes.no")
	class TrueFalse : BooleanParser("%bool=true.false")
}