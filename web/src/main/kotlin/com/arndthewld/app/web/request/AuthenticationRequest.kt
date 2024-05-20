package com.arndthewld.app.web.request

import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.domain.User

// Registration
sealed interface AuthenticationRequest {
    val email: String?
    val username: String?
}

data class PasswordAuthenticationRequest(
    override val email: String?,
    override val username: String?,
    val password: String?,
) : AuthenticationRequest

data class OAuth2AuthenticationRequest(
    override val email: String?,
    override val username: String?,
    val oAuthProviderSource: OAuthProviderSource?,
    val oAuthProviderId: String?,
) : AuthenticationRequest

// Login
sealed interface LoginRequest {
    val email: String?
}

data class AuthenticatedResponse(val userId: Long, val email: String, val username: String?, val token: String) {
    constructor(user: User, token: String) : this(user.userId, user.email, user.username, token)
}
