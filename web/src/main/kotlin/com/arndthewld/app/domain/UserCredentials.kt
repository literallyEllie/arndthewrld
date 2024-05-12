package com.arndthewld.app.domain

import com.arndthewld.app.config.oauth.OAuthProviderSource

data class UserCredentials(
    val email: String?, val username: String? = null,
    val password: String? = null,
    val oAuthProviderSource: OAuthProviderSource? = null, val oAuthProviderId: String? = null,
) {

    fun isPlaintext(): Boolean {
        return password != null
    }

    fun isOAuth(): Boolean {
        return oAuthProviderSource != null && oAuthProviderId != null
    }

    fun isValid(): Boolean {
        return password != null || (oAuthProviderSource != null && oAuthProviderId != null)
    }

    fun getKey(): String {
        return email ?: username ?: throw NullPointerException("no email or username specified")
    }
}