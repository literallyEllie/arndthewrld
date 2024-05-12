package com.arndthewld.app.config.environment

import io.avaje.config.Config
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Basic app config
 */
object AppEnvConfig {
    private val logger = LoggerFactory.getLogger(AppEnvConfig::class.java)
    private var initialised = AtomicBoolean(false)

    lateinit var instanceEnvironment: InstanceEnvironment

    fun load() {
        if (!initialised.compareAndSet(false, true)) {
            throw RuntimeException("app config already initialised")
        }

        loadInstanceEnvironment()

        logger.info("AppConfig loaded. \n\tEnvironment: $instanceEnvironment")
    }

    public fun getValue(key: String): String? = Config.getNullable(key)

    public fun assertValue(key: String): String {
        return getValue(key) ?: throw NullPointerException("env key $key is not defined")
    }

    public operator fun get(key: String): String? = getValue(key)

    private fun loadInstanceEnvironment() {
        instanceEnvironment = Config.getEnum(
            InstanceEnvironment::class.java, "instance.environment", InstanceEnvironment.LOCAL
        )
    }
}