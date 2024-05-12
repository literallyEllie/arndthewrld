package com.arndthewld.app.domain.service.auth

import com.arndthewld.app.config.security.Cipher
import com.arndthewld.app.domain.UserCredentials
import io.javalin.http.BadRequestResponse
import io.javalin.http.UnauthorizedResponse

private interface CredentialsValidator {
    fun authenticate(claim: UserCredentials, actual: UserCredentials)
}

class PlaintextValidator(private val cipher: Cipher) : CredentialsValidator {
    override fun authenticate(claim: UserCredentials, actual: UserCredentials) {
        if (!actual.isPlaintext() || claim.password == null) {
            throw BadRequestResponse("plaintext password not supported")
        }

        if (actual.password != String(cipher.encryptBase64(claim.password))) {
            throw UnauthorizedResponse("username or password incorrect")
        }
    }
}

class OAuth2Validator : CredentialsValidator {
    override fun authenticate(claim: UserCredentials, actual: UserCredentials) {
        if (!actual.isOAuth()) {
            throw BadRequestResponse("oauth not supported")
        }

        if (actual.oAuthProviderSource != claim.oAuthProviderSource) {
            throw UnauthorizedResponse("oauth source mismatch")
        }

        if (actual.oAuthProviderId != claim.oAuthProviderId) {
            throw UnauthorizedResponse("provider id mismatch")
        }
    }
}

