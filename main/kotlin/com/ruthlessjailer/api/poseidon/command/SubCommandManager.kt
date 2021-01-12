package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.poseidon.Chat
import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.poseidon.command.parser.ArgumentParser
import com.ruthlessjailer.api.poseidon.command.parser.EnumParser
import com.ruthlessjailer.api.poseidon.command.parser.StringParser
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

	val parsers = HashMap<Class<*>, ArgumentParser<*>>()

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
				try {
					wrappers.add(parseMethod(command, method, method.getAnnotation(SubCommand::class.java) as SubCommand))
				} catch (e: SubCommandException) {//add empty one so that this doesn't run every time the command is executed
					wrappers.add(SubCommandWrapper.EMPTY)

					synchronized(subCommands) {
						subCommands.put(command, wrappers)
					}

					throw e
				}
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

		var counter = 0
		var variableCounter = 0//all variables that must be parsed and included as parameters on method

		for (arg in format) {
			for (entry in parsers.entries) {
				if (entry.value.isFormatValid(arg)) {
					variableCounter++
					break
				}
			}
			counter++
		}

		//check that the method meets the basic requirements before continuing

		Checks.verify(
				variableCounter == method.parameterCount - 2,
				"$INVALID_USAGE Method parameters do not match format '${annotation.format}' in method ${getMethodPath(method)}.",
				SubCommandException::class.java)

		//initialize variables
		var v = 0
		val arguments = Array<Argument<*>>(counter) { i -> if (EnumParser.GENERIC.isFormatValid(format[i])) Argument.GENERIC else Argument.EMPTY }
		val variables = Array<Class<*>>(variableCounter) { i ->

			format@ for (arg in format) {
				for (entry in parsers.entries) {
					if (!entry.value.isFormatValid(format[v])) {
						v++
						break@format
					}
				}
			}
			
			if (EnumParser.GENERIC.isFormatValid(format[v])) {
				val enumType = method.parameterTypes[i + 2]//+2 for sender and args

				println(enumType)

				Checks.verify(
						enumType.isEnum,
						"$INVALID_USAGE Method parameter '$enumType' from format '${annotation.format}' in method ${getMethodPath(method)} is not an enum!",
						SubCommandException::class.java)

				if (parsers[enumType] == null) {//register parser for that type if not already
					parsers[enumType] = EnumParser(enumType as Class<out Enum<*>>)
				}

				enumType as Class<out Enum<*>>
			} else Void.TYPE
		}

		counter = 0
		variableCounter = 0

		//parse the format

		format@ for (arg in format) {
			for (entry in parsers) {
				val parser = entry.value

				if (!parser.isFormatValid(arg)) continue
				if (ClassUtils.primitiveToWrapper(parser.type) != ClassUtils.primitiveToWrapper(method.parameterTypes[variableCounter + 2])) continue

				arguments[counter++] = Argument(parser, true)
				variables[variableCounter++] = parser.type

				continue@format
			}

			arguments[counter++] = Argument(parsers[String::class.java] ?: StringParser().register(), false, arg.split("|"))
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

			if (variables[i - 2] == Void.TYPE) continue//void ones are unparsed (non-variables just static args)

			Checks.verify(
					ClassUtils.primitiveToWrapper(type) == ClassUtils.primitiveToWrapper(variables[i - 2]),//-2 since first two params are sender and args
					"$INVALID_USAGE Method parameter ${ReflectUtil.getPath(type)} at index $i does not match expected type ${variables[i - 2]} from format ${format[i - 2]}.",
					SubCommandException::class.java)
		}

		return SubCommandWrapper(parent, arguments.toList(), variables.toList(), /*enumTypes.toList(),*/ method)
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

			Checks.nullCheck(
					subCommands[command],
					"Internal error: unable to register command /${command.label}.")
		}

		wrappers@ for (wrapper in subCommands[command]!!) {
			if (args.size < wrapper.arguments.size) {
				continue
			}

			val variableTypes = wrapper.variableTypes

			val parameters = arrayOfNulls<Any>(variableTypes.size + 2)//+2: sender args

			parameters[0] = sender
			parameters[1] = args

			var v = 2
			for ((a, argument) in wrapper.arguments.withIndex()) {
				if (!argument.isValidIgnoreCase(args[a])) continue@wrappers
				if (argument.isVariable) {
					parameters[v] = argument.parser.parse(args[a])
					v++
				} //+2: sender args
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

	private fun getMethodPath(method: Method): String = "${ReflectUtil.getPath(method.declaringClass)}#${method.name}(${method.parameterTypes.joinToString(", ")})"
}