package com.arndthewld.app.config

import com.arndthewld.app.config.security.JwtProvider
import com.arndthewld.app.ext.setAttributes
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.security.RouteRole

/**
 * Low-level page access restriction.
 */
enum class RoleState : RouteRole {
    ANYONE,
    AUTHENTICATED,
    ADMIN,
}

private const val HEADER_TOKEN_NAME = "Authorization"

class AuthConfig(private val jwtProvider: JwtProvider) {
    fun handleAccess(ctx: Context) {
        val permittedRoles = ctx.routeRoles()

        val jwtToken = getJwtTokenHeader(ctx)
        val userRole = getUserRole(jwtToken) ?: RoleState.ANYONE

        permittedRoles.takeIf { !it.contains(userRole) }?.apply { throw ForbiddenResponse() }
        if (jwtToken != null) {
            ctx.setAttributes(token = jwtToken)
        }
    }

    private fun getJwtTokenHeader(ctx: Context): DecodedJWT? {
        val tokenHeader =
            ctx.header(HEADER_TOKEN_NAME)?.substringAfter("Token")?.trim()
                ?: return null

        return jwtProvider.decodeJWT(tokenHeader)
    }

    private fun getUserRole(jwtToken: DecodedJWT?): RouteRole? {
        val userRole = jwtToken?.getClaim("role")?.asString() ?: return null
        return RoleState.valueOf(userRole)
    }
}
