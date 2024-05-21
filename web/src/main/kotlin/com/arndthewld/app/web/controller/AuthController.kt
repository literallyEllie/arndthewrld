package com.arndthewld.app.web.controller

import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.domain.UserCredentials
import com.arndthewld.app.domain.service.UserService
import com.arndthewld.app.domain.service.auth.AuthService
import com.arndthewld.app.web.request.AuthenticatedResponse
import com.arndthewld.app.web.request.OAuth2AuthenticationRequest
import com.arndthewld.app.web.request.PasswordAuthenticationRequest
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.javalin.JavalinWebContext
import org.pac4j.jee.context.session.JEESessionStore

/**
 * Handles authentication of existing users.
 */
class AuthController(
    private val authService: AuthService, private val userService: UserService
) {
    /**
     * Register with plaintext password
     */
    fun register(ctx: Context) {
        ctx.bodyValidator<PasswordAuthenticationRequest>()
            .check({ !it.email.isNullOrBlank() }, "email is blank")
            .check({ !it.username.isNullOrBlank() }, "username is blank")
            .check({ !it.password.isNullOrBlank() }, "password is blank")
            .get().also {
                val createdUser = userService.create(it)
                val token = authService.authenticate(createdUser.userId, UserCredentials(it.password))
                val profile = userService.getProfileById(createdUser.userId)

                ctx.json(AuthenticatedResponse(createdUser, profile, token))
            }
    }

    /**
     * Login with plaintext password
     */
    fun login(ctx: Context) {
        ctx.bodyValidator<PasswordAuthenticationRequest>()
            .check({ (!it.email.isNullOrBlank()) || !(it.username.isNullOrBlank()) }, "email or username is blank")
            .check({ !it.password.isNullOrBlank() }, "password is blank")
            .get().also {
                val userId = userService.getUserId(it)
                val token = authService.authenticate(userId, UserCredentials(it.password))
                val user = userService.login(userId)
                val profile = userService.getProfileById(user.userId)

                ctx.json(AuthenticatedResponse(user, profile, token))
            }
    }

    fun oAuthRegister(ctx: Context) {
        val (authProfile, source) = requireOAuth2Profile(ctx) ?: return
        val request =
            OAuth2AuthenticationRequest(
                email = authProfile.email,
                username = null,
                oAuthProviderSource = source,
                oAuthProviderId = authProfile.id,
            )

        val createdUser = userService.create(request)
        val token =
            authService.authenticate(
                createdUser.userId,
                UserCredentials(request.oAuthProviderSource, request.oAuthProviderId),
            )
        val profile = userService.getProfileById(createdUser.userId)

        ctx.json(AuthenticatedResponse(createdUser, profile, token))
    }

    fun oAuthLogin(ctx: Context) {
        val (authProfile, source) = requireOAuth2Profile(ctx) ?: return
        val request =
            OAuth2AuthenticationRequest(
                email = authProfile.email,
                username = null,
                oAuthProviderSource = source,
                oAuthProviderId = authProfile.id,
            )

        val userId = userService.getUserId(request)
        val token =
            authService.authenticate(userId, UserCredentials(request.oAuthProviderSource, request.oAuthProviderId))
        val user = userService.login(userId)
        val profile = userService.getProfileById(user.userId)

        ctx.json(AuthenticatedResponse(user, profile, token))
    }

    private fun requireOAuth2Profile(ctx: Context): Pair<CommonProfile, OAuthProviderSource>? {
        val source = OAuthProviderSource.valueOf(ctx.pathParam("provider").uppercase())

        val manager = ProfileManager(JavalinWebContext(ctx), JEESessionStore.INSTANCE)
        val optProfile = manager.getProfile(CommonProfile::class.java)

        if (optProfile.isEmpty) {
            // We are serving them for the first time, direct them to login.
            authService.fetchOAuthProfile(ctx, source)
            return null
        }

        return Pair(optProfile.get(), source)
    }

    fun oauthCallback(ctx: Context) {
        authService.oAuthCallback(ctx)
    }

    fun logout(ctx: Context) {
        authService.logout(ctx)
    }
}
