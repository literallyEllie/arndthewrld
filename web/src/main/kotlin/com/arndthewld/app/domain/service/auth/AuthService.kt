package com.arndthewld.app.domain.service.auth

import com.arndthewld.app.config.RoleState
import com.arndthewld.app.config.oauth.OAuthConfig
import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.config.security.Cipher
import com.arndthewld.app.config.security.JwtProvider
import com.arndthewld.app.domain.UserCredentials
import com.arndthewld.app.domain.repository.AuthRepository
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

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
     * Registers the user and returns a token.
     */
    fun register(credentials: UserCredentials): String {
        if (!credentials.isValid()) {
            throw BadRequestResponse("invalid credentials")
        }

        if (credentials.email.isNullOrBlank()) {
            throw BadRequestResponse("invalid email")
        }

        // Check dupes
        authRepository.getByEmail(credentials.email).takeIf { it != null }?.apply {
            throw BadRequestResponse("email already in use")
        }

        if (credentials.username != null) {
            if (credentials.username.isBlank()) {
                throw BadRequestResponse("invalid username")
            }

            authRepository.getByUsername(credentials.username).takeIf { it != null }?.apply {
                throw BadRequestResponse("username already in use")
            }
        }

        // Pass checks
        val finalCredentials = if (credentials.isOAuth()) {
            // todo validate oauth?

            credentials
        } else if (credentials.isPlaintext()) {
            if (credentials.password.isNullOrBlank()) {
                throw BadRequestResponse("invalid password")
            }

            credentials.copy(password = String(cipher.encryptBase64(credentials.password)))
        } else {
            throw BadRequestResponse("unsupported credentials")
        }

        // Insert
        authRepository.insert(finalCredentials)

        // OK!
        return generateJwtToken(finalCredentials)
    }

    /**
     * Authenticates a credentials pair to get a token.
     *
     * It will match by either the email then username,
     * before validating the claim with the matched credentials.
     *
     * Finally, a token will be returned.
     */
    fun authenticate(credentialsClaim: UserCredentials): String {
        if (!credentialsClaim.isValid()) {
            throw BadRequestResponse("user credentials empty")
        }

        // Get matching credentials
        val matchCredentials = if (credentialsClaim.email != null) {
            authRepository.getByEmail(credentialsClaim.email)
        } else if (credentialsClaim.username != null) {
            authRepository.getByUsername(credentialsClaim.username)
        } else {
            throw BadRequestResponse("no email or username specified")
        }

        // Ensure they actually exist
        matchCredentials ?: throw NotFoundResponse("no such user")

        // Validate credentials
        if (credentialsClaim.isOAuth()) {
            oAuth2Validator.authenticate(credentialsClaim, matchCredentials)
        } else if (credentialsClaim.isPlaintext()) {
            plaintextValidator.authenticate(credentialsClaim, matchCredentials)
        } else {
            throw BadRequestResponse("unsupported credentials")
        }

        return generateJwtToken(matchCredentials)
    }

    /**
     * Redirects the context to third party authentication.
     */
    fun fetchOAuthProfile(ctx: Context, provider: OAuthProviderSource) {
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

    private fun generateJwtToken(user: UserCredentials): String {
        return jwtProvider.createJWT(user, RoleState.AUTHENTICATED)
    }
}