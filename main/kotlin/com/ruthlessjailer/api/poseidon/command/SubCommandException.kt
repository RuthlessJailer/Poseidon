package com.ruthlessjailer.api.poseidon.command

/**
 * @author RuthlessJailer
 */
class SubCommandException : RuntimeException {

	constructor(message: String, cause: Throwable) : super(message, cause)
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super(cause)

}