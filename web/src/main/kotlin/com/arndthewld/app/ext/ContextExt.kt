package com.arndthewld.app.ext

import com.arndthewld.app.config.RoleState
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.http.Context

private const val keyId = "id"
private const val keyToken = "token"
private const val keyRole = "role"

fun Context.setAttributes(token: DecodedJWT) {
    val userRole = token.getClaim(keyRole)?.asString() ?: return

    sessionAttribute(keyId, token.subject)
    sessionAttribute(keyToken, token.token)
    sessionAttribute(keyRole, RoleState.valueOf(userRole))
}

fun Context.isAuthenticated(): Boolean {
    return (role() ?: RoleState.ANYONE) != RoleState.ANYONE
}

fun Context.id(): String? {
    return this[keyId]
}

fun Context.token(): String? {
    return this[keyToken]
}

fun Context.role(): RoleState? {
    return this[keyRole]
}

operator fun <T> Context.get(key: String): T? {
    return this.sessionAttribute<T>(key)
}

operator fun Context.set(key: String, value: Any?) {
    this.sessionAttribute(key, value)
}
