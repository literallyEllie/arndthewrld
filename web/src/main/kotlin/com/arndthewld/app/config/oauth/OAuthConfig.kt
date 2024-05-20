package com.arndthewld.app.config.oauth

import com.arndthewld.app.config.environment.AppEnvConfig
import io.javalin.http.Context
import org.pac4j.core.config.Config
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver
import org.pac4j.javalin.CallbackHandler
import org.pac4j.javalin.LogoutHandler
import org.pac4j.javalin.SecurityHandler
import org.pac4j.oauth.client.Google2Client

/**
 * Auth provider used to create an account.
 * TODO put somewhere else
 */
enum class OAuthProviderSource(val clientId: String) {
    GOOGLE("Google2Client"),
    DISCORD("DiscordClient"),
}

class OAuthConfig(google2Client: Google2Client) {
    private var config =
        Config(AppEnvConfig["auth.oauth.callback"], google2Client).also {
            it.clients.callbackUrlResolver = NoParameterCallbackUrlResolver()
        }
    private var callbackHandler = CallbackHandler(config, "/", true)
    private var logoutHandler: LogoutHandler =
        LogoutHandler(config, "/?").also {
            it.destroySession = true
        }

    fun handleLogin(
        context: Context,
        source: OAuthProviderSource,
    ) {
        SecurityHandler(config, source.clientId).handle(context)
    }

    /**
     * Handle a callback.
     * It will then redirect the user back to the login page.
     *
     * @param context Request context.
     */
    fun handleLoginCallback(context: Context) {
        callbackHandler.handle(context)
    }

    fun handleLogout(context: Context) {
        // TODO check if this sufficient
        logoutHandler.handle(context)
    }
}
