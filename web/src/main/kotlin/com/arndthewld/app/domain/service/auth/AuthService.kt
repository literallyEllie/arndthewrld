package com.arndthewld.app.domain.service.auth

import co.touchlab.kermit.Logger
import com.arndthewld.app.config.RoleState
import com.arndthewld.app.config.oauth.OAuthConfig
import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.config.security.Cipher
import com.arndthewld.app.config.security.JwtProvider
import com.arndthewld.app.domain.UserCredentials
import com.arndthewld.app.domain.repository.AuthRepository
import com.arndthewld.app.web.request.AuthenticationRequest
import com.arndthewld.app.web.request.OAuth2AuthenticationRequest
import com.arndthewld.app.web.request.PasswordAuthenticationRequest
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

/**
 * Registers and authenticates user credentials then returns a JWT token.
 * Also, responsible for OAuth2 flow.
 */
class AuthService(
    private val authRepository: AuthRepository,
    private val authProviders: OAuthConfig,
    private val cipher: Cipher,
    private val jwtProvider: JwtProvider,
) {
    // Credentials authenticators
    private val plaintextValidator = PlaintextValidator(cipher)
    private val oAuth2Validator = OAuth2Validator()

    /**
     * Registers the user authentication details and returns a token.
     */
    fun register(
        userId: Long,
        request: AuthenticationRequest,
    ) {
        Logger.withTag("auth").d { "register($userId, $request)" }

        // Check dupes
        authRepository.findByUserId(userId).takeIf { it != null }?.apply {
            throw BadRequestResponse("user already has credentials set")
        }

        val credentials =
            when (request) {
                is PasswordAuthenticationRequest -> request.toCredentials()
                is OAuth2AuthenticationRequest -> request.toCredentials()
            }

        // Insert
        Logger.withTag("auth").d { "Registration success, inserting $userId" }
        authRepository.insert(userId, credentials)
    }

    /**
     * Authenticates a credentials pair to get a token.
     *
     * It will match by either the email then username,
     * before validating the claim with the matched credentials.
     *
     * Finally, a token will be returned.
     */
    fun authenticate(
        userId: Long,
        credentialsClaim: UserCredentials,
    ): String {
        Logger.withTag("auth").d { "authenticate($userId, $credentialsClaim)" }

        // Get matching credentials
        val matchCredentials =
            authRepository.findByUserId(userId) ?: throw NotFoundResponse("incorrect credentials")

        // Validate credentials
        if (credentialsClaim.isOAuth()) {
            oAuth2Validator.authenticate(credentialsClaim, matchCredentials)
        } else if (credentialsClaim.isPlaintext()) {
            plaintextValidator.authenticate(credentialsClaim, matchCredentials)
        } else {
            throw BadRequestResponse("unsupported credentials")
        }

        Logger.withTag("auth").d { "Login success, generating token for $userId" }
        return generateJwtToken(userId)
    }

    /**
     * Redirects the context to third party authentication.
     */
    fun fetchOAuthProfile(
        ctx: Context,
        provider: OAuthProviderSource,
    ) {
        authProviders.handleLogin(ctx, provider)
    }

    /**
     * Third party callback with their details
     */
    fun oAuthCallback(ctx: Context) {
        authProviders.handleLoginCallback(ctx)
    }

    fun logout(ctx: Context) {
        authProviders.handleLogout(ctx)
    }

    private fun generateJwtToken(userId: Long): String {
        Logger.withTag("auth").d { "Generating token for $userId" }

        return jwtProvider.createJWT(userId, RoleState.AUTHENTICATED)
    }

    private fun validatePassword(password: String?): Boolean {
        return !password.isNullOrBlank()
    }

    private fun PasswordAuthenticationRequest.toCredentials(): UserCredentials {
        if (!validatePassword(password)) {
            throw BadRequestResponse("invalid password")
        }

        return UserCredentials(String(cipher.encryptBase64(password!!)))
    }

    private fun OAuth2AuthenticationRequest.toCredentials(): UserCredentials {
        this.oAuthProviderSource ?: throw BadRequestResponse("oAuthProviderId not set")
        this.oAuthProviderId ?: throw BadRequestResponse("oAuthProviderId not set")

        return UserCredentials(this.oAuthProviderSource, this.oAuthProviderId)
    }
}
