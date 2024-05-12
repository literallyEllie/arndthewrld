package com.arndthewld.app.config.security

import com.arndthewld.app.domain.UserCredentials
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.security.RouteRole
import java.util.*

class JwtProvider(private val cipher: Cipher) {

    fun decodeJWT(token: String): DecodedJWT = JWT.require(cipher.algorithm).build().verify(token)

    fun createJWT(user: UserCredentials, role: RouteRole): String =
        JWT.create()
            .withIssuedAt(Date())
            .withSubject(user.email)
            .withClaim("role", role.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000))
            .sign(cipher.algorithm)
}