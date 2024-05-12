package com.arndthewld.app.web.controller

import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.domain.UserCredentials
import com.arndthewld.app.domain.service.auth.AuthService
import com.arndthewld.app.ext.createLogger
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.javalin.JavalinWebContext
import org.pac4j.jee.context.session.JEESessionStore

/**
 * Handles authentication of existing users.
 */
class AuthController(private val service: AuthService) {
    private val logger by createLogger()

    /**
     * Register with plaintext password
     */
    fun register(ctx: Context) {
        ctx.bodyValidator<UserCredentials>()
            .check({ !it.email.isNullOrBlank() }, "email is blank")
            .check({ !it.username.isNullOrBlank() }, "username is blank")
            .check({ !it.password.isNullOrBlank() }, "password is blank")
            .get().also {
                val token = service.register(
                    UserCredentials(email = it.email, username = it.username, password = it.password)
                )
                ctx.json(mapOf("token" to token))
            }
    }

    /**
     * Login with plaintext password
     */
    fun login(ctx: Context) {
        logger.info("login")

        ctx.bodyValidator<UserCredentials>()
            .check({ it.email != null || it.username != null }, "no email or username specified")
            .check({ it.password != null }, "no password specified")
            .get().also {
                val token = service.authenticate(
                    UserCredentials(email = it.email, username = it.username, password = it.password)
                )
                ctx.json(mapOf("token" to token))
            }
    }

    fun oAuthLogin(ctx: Context) {
        ctx.json(mapOf("token" to oAuthAuthenticate(ctx, false)))
    }

    fun oAuthRegister(ctx: Context) {
        ctx.json(mapOf("token" to oAuthAuthenticate(ctx, true)))
    }

    /**
     * Recursive endpoint which is initially called empty,
     * then returned with an authenticated OAuth profile.
     */
    private fun oAuthAuthenticate(ctx: Context, createProfile: Boolean): String {
        val source = OAuthProviderSource.valueOf(ctx.pathParam("provider").uppercase())

        val manager = ProfileManager(JavalinWebContext(ctx), JEESessionStore.INSTANCE)
        val optProfile = manager.getProfile(CommonProfile::class.java)

        if (optProfile.isEmpty) {
            // We are serving them for the first time, direct them to login.
            service.fetchOAuthProfile(ctx, source)
            return ""
        }

        val profile = optProfile.get()
        return if (createProfile) {
            ""
        } else {
            service.authenticate(
                UserCredentials(email = profile.email, oAuthProviderSource = source, oAuthProviderId = profile.id)
            )
        }
    }

    fun oauthCallback(ctx: Context) {
        service.oAuthCallback(ctx)
    }

    fun logout(ctx: Context) {
        service.logout(ctx)
    }
}