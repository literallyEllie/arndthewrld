package com.arndthewld.app.domain

import com.arndthewld.app.config.oauth.OAuthProviderSource

data class UserCredentials(
    val password: String?,
    val oAuthProviderSource: OAuthProviderSource?,
    val oAuthProviderId: String?,
) {
    constructor(password: String?) : this(password, null, null)

    constructor(providerSource: OAuthProviderSource?, providerId: String?) : this(null, providerSource, providerId)

    fun isPlaintext(): Boolean {
        return password != null
    }

    fun isOAuth(): Boolean {
        return oAuthProviderSource != null && oAuthProviderId != null
    }
}
