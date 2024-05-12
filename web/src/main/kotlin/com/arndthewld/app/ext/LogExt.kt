package com.arndthewld.app.ext

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@JvmSynthetic
fun Any.createLogger(): Lazy<Logger> = lazy { createLogger(this) }

/**
 * Creates an instance of [Logger] with the name of [name]
 */
public fun createLogger(name: String): Logger = LoggerFactory.getLogger(name)

/**
 * Creates an instance of [Logger] which prepends the simple name of class [target].
 */
public fun createLogger(target: Any): Logger = LoggerFactory.getLogger(target.javaClass.simpleName)

/** Logs the [msg] using the given [level]. */
public fun Logger.log(level: Level, msg: String?) {
    when (level) {
        Level.TRACE -> trace(msg)
        Level.DEBUG -> debug(msg)
        Level.INFO -> info(msg)
        Level.WARN -> warn(msg)
        Level.ERROR -> error(msg)
    }
}

/** Logs the given [format] replaced by the [arguments] using the given [level]. */
public fun Logger.log(level: Level, format: String?, vararg arguments: Any?) {
    when (level) {
        Level.TRACE -> trace(format, arguments)
        Level.DEBUG -> debug(format, arguments)
        Level.INFO -> info(format, arguments)
        Level.WARN -> warn(format, arguments)
        Level.ERROR -> error(format, arguments)
    }
}

/** Logs the given [msg] using the given [level] with an optional [Throwable]. */
public fun Logger.log(level: Level, msg: String?, t: Throwable?) {
    when (level) {
        Level.TRACE -> trace(msg, t)
        Level.DEBUG -> debug(msg, t)
        Level.INFO -> info(msg, t)
        Level.WARN -> warn(msg, t)
        Level.ERROR -> error(msg, t)
    }
}
