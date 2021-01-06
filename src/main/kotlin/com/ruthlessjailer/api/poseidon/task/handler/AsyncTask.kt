package com.ruthlessjailer.api.poseidon.task.handler

import java.util.concurrent.ScheduledFuture

/**
 * @author RuthlessJailer
 */
class AsyncTask<T>(val task: ScheduledFuture<T?>, repeat: Int = -1) : Task(repeat)