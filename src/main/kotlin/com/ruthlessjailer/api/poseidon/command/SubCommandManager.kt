package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.poseidon.Chat
import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.poseidon.command.parser.ArgumentParser
import com.ruthlessjailer.api.poseidon.command.parser.EnumParser
import com.ruthlessjailer.api.theseus.Checks
import com.ruthlessjailer.api.theseus.Common
import com.ruthlessjailer.api.theseus.ReflectUtil
import org.apache.commons.lang.ClassUtils
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import java.lang.reflect.Method

/**
 * @author RuthlessJailer
 */
object SubCommandManager {
	private const val INVALID_USAGE = "Invalid SubCommand annotation usage!"

	//	private const val MESSAGE_METHOD_PARAMETERS = "Include all variables in method parameters, but make sure the first two are CommandSender and Array<String>."
	private val VARIABLE_PATTERN = Regex("%[a-z]+(<[A-Z_a-z0-9]+>)?")

	private val subCommands = HashMap<CommandBase, List<SubCommandWrapper>>()

	private val parsers = HashMap<Class<*>, ArgumentParser<*>>()

	//TODO help menus

	fun <T> registerArgumentParser(type: Class<T>, parser: ArgumentParser<T>) {
		parsers[type] = parser
	}

	fun register(command: SuperiorCommand) {

//		Checks.verify(
//				command is CommandBase,
//				"${ReflectUtil.getPath(SuperiorCommand::class.java)} implementations must extend ${ReflectUtil.getPath(CommandBase::class.java)}.",
//				SubCommandException::class.java)

		if (command !is CommandBase) {
			throw SubCommandException("${ReflectUtil.getPath(SuperiorCommand::class.java)} implementations must extend ${ReflectUtil.getPath(CommandBase::class.java)}.")
		}

		if (!PluginBase.isMainThread()) {
			Chat.warning("Async call to command /${command.label} while registering.")
		}

		val wrappers = mutableListOf<SubCommandWrapper>()

		for (method in command.javaClass.declaredMethods) {
			if (method.isAnnotationPresent(SubCommand::class.java)) {
				wrappers.add(parseMethod(command, method, method.getAnnotation(SubCommand::class.java) as SubCommand))
			}
		}

		Chat.debug("SubCommands", "Registering methods ${Common.convert(wrappers) { it.method.name }.joinToString(", ")} in class ${ReflectUtil.getPath(command.javaClass)} as sub-commands.")

		synchronized(subCommands) {
			subCommands.put(command, wrappers)
		}
	}

	private fun parseMethod(parent: CommandBase, method: Method, annotation: SubCommand): SubCommandWrapper {
		val format = Checks.stringCheck(
				annotation.format,
				"$INVALID_USAGE Format on method ${getMethodPath(method)} is empty!",
				false)
				.split(" ")

		if (format[0].equals("help", true) && parent.autoGenerateHelpMenu) {
			Chat.warning("Sub-command ${getMethodPath(method)} overrides default help command. Disabling automatic help menu...")
			parent.autoGenerateHelpMenu = false
		}

		val enumTypes = annotation.enumTypes
//		val arguments = arrayOfNulls<Argument<*>>(format.size)
		val arguments = mutableListOf<Argument<*>>()

		var counter = 0
		var variableCounter = 0//all variables that must be parsed and included as parameters on method
		var enumTypeCounter = 0//all classes that are required to be provided inside the annotation, ie enums

		for (enumType in enumTypes) {//make sure the enumTypes are enums and make parsers for them if not exist already
			Checks.verify(
					enumType.java.isEnum,
					"$INVALID_USAGE Provided class ${enumType.simpleName} is not an enum.",
					SubCommandException::class.java)

			if (parsers[enumType.java] == null) {
				parsers[enumType.java] = EnumParser(enumType.java)
			}
		}

		for (arg in format) {
			for (entry in parsers.entries) {
				if (entry.value.isFormatValid(arg)) {
					if (entry.value.requiresClass) {
						enumTypeCounter++
					}

					variableCounter++
				}
			}
			counter++
		}

		//check that the method meets the basic requirements before continuing

		Checks.verify(
				enumTypeCounter == enumTypes.size,
				"$INVALID_USAGE Provided classes do not match required classes in format '${annotation.format}' on method ${getMethodPath(method)}.",
				SubCommandException::class.java)

		Checks.verify(
				variableCounter == method.parameterCount - 2,
				"$INVALID_USAGE Method parameters do not match format '${annotation.format}' in method ${getMethodPath(method)}.",
				SubCommandException::class.java)

		val variables = arrayOfNulls<Class<*>>(variableCounter)

		counter = 0
		variableCounter = 0
		enumTypeCounter = 0

		//parse the format

		for (arg in format) {
			var parsed = false
			for (entry in parsers) {
				val parser = entry.value

				if (!parser.isFormatValid(arg)) continue

				//				arguments[counter] = Argument(parser)

				if (parser.requiresClass) {
					val enumType = enumTypes[enumTypeCounter].java
					variables[variableCounter] = enumType
					arguments.add(Argument(parser, true))
					enumTypeCounter++
				} else {
					arguments.add(Argument(parser, true))
					variables[variableCounter] = parser.type
				}

				parsed = true
				variableCounter++
			}

			if (!parsed) {//it's not a variable
//				arguments[counter] = Argument(parsers[String::class.java]!!, arg.split("|"))
				arguments.add(Argument(parsers[String::class.java]!!, false, arg.split("|")))
			}

			counter++
		}

		//verify that the method's parameters actually match

		Checks.verify(
				method.parameterTypes[0] == CommandSender::class.java,
				"$INVALID_USAGE First parameter of method ${getMethodPath(method)} must be ${ReflectUtil.getPath(CommandSender::class.java)}.",
				SubCommandException::class.java)

		Checks.verify(
				method.parameterTypes[1] == arrayOf<String>()::class.java,
				"$INVALID_USAGE Second parameter of method ${getMethodPath(method)} must be a string array.",
				SubCommandException::class.java)

		for ((i, type) in method.parameterTypes.withIndex()) {
			if (i < 2) continue//start at 2 since first two parameters are reserved

			Checks.verify(
					ClassUtils.primitiveToWrapper(type) == ClassUtils.primitiveToWrapper(variables[i - 2]),//-2 since first two params are sender and args
					"$INVALID_USAGE Method parameter ${type.simpleName} at index $i does not match expected type ${variables[i - 2]} from format.",
					SubCommandException::class.java)
		}

		return SubCommandWrapper(parent, arguments, variables.toList() as List<Class<*>>, enumTypes.toList(), method)
	}

	fun executeFor(command: SuperiorCommand, sender: CommandSender, args: Array<String>) {
		if (command !is CommandBase) {
			throw SubCommandException("${ReflectUtil.getPath(SuperiorCommand::class.java)} implementations must extend ${ReflectUtil.getPath(CommandBase::class.java)}.")
		}

		if (command.autoGenerateHelpMenu && args.isNotEmpty() && args[0].equals("help", ignoreCase = true)) {//automatic help command
			TODO()
		}

		if (subCommands[command] == null) {
			register(command)

			Checks.verify(
					subCommands[command] != null,
					"Internal error: unable to register command /${command.label}.",
					SubCommandException::class.java)
		}

		wrappers@ for (wrapper in subCommands[command]!!) {

			val variableTypes = wrapper.variableTypes

			val parameters = arrayOfNulls<Any>(variableTypes.size + 2)//+2: sender args

			parameters[0] = sender
			parameters[1] = args

			if (args.size < wrapper.arguments.size) {
				continue
			}

			for ((a, argument) in wrapper.arguments.withIndex()) {
				if (!argument.isValidIgnoreCase(args[a])) continue@wrappers
				if (argument.isVariable) parameters[a + 2] = argument.parser.parse(args[a])//+2: sender args
			}

			Chat.debug("SubCommands", "Invoking method ${getMethodPath(wrapper.method)} for args '${args.joinToString(" ")}'.")
			
			try {
				ReflectUtil.invokeMethod<Any>(wrapper.method, command, *parameters)
			} catch (e: ReflectUtil.ReflectionException) {
				if (e.cause?.cause is CommandException) break
				e.cause?.cause?.printStackTrace()
			}
		}
	}

	private fun getMethodPath(method: Method): String = "${ReflectUtil.getPath(method.declaringClass)}#${method.name}"
}