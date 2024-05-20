package com.arndthewld.app.config.environment

public enum class InstanceEnvironment(val isManaged: Boolean = false) {
    LOCAL,
    DEVELOPMENT,
    STAGING(true),
    PRODUCTION(true),
}
