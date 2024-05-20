package com.arndthewld.app.ext

import com.arndthewld.app.config.RoleState
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.http.Context

private const val KEY_USER_ID = "id"
private const val KEY_TOKEN = "token"
private const val KEY_ROLE = "role"

fun Context.setAttributes(token: DecodedJWT) {
    val userRole = token.getClaim(KEY_ROLE)?.asString() ?: return

    sessionAttribute(KEY_USER_ID, token.subject.toLong())
    sessionAttribute(KEY_TOKEN, token.token)
    sessionAttribute(KEY_ROLE, RoleState.valueOf(userRole))
}

fun Context.isAuthenticated(): Boolean {
    return (role() ?: RoleState.ANYONE) != RoleState.ANYONE
}

fun Context.userId(): Long? {
    return this[KEY_USER_ID]
}

fun Context.token(): String? {
    return this[KEY_TOKEN]
}

fun Context.role(): RoleState? {
    return this[KEY_ROLE]
}

operator fun <T> Context.get(key: String): T? {
    return this.sessionAttribute<T>(key)
}

operator fun Context.set(
    key: String,
    value: Any?,
) {
    this.sessionAttribute(key, value)
}
