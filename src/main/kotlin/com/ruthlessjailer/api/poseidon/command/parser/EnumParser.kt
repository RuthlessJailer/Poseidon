package com.ruthlessjailer.api.poseidon.command.parser

import com.ruthlessjailer.api.theseus.Common
import com.ruthlessjailer.api.theseus.ReflectUtil
import java.util.*

/**
 * @author RuthlessJailer
 */
open class EnumParser<E : Enum<E>>(type: Class<E>) : ArgumentParser<E>(true, type, "enum") {

	override fun isValid(argument: String): Boolean = parse(argument) != null

	override fun parse(argument: String): E? = ReflectUtil.getEnum(type, argument)

	override fun possibilities(): List<String> = Common.convert(ReflectUtil.getEnumValues(type).toList(), Enum<E>::name)

}