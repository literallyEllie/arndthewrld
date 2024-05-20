package com.arndthewld.app.config.security

import com.arndthewld.app.config.environment.AppEnvConfig
import com.auth0.jwt.algorithms.Algorithm
import java.util.Base64

interface Cipher {
    val algorithm: Algorithm

    fun encrypt(data: String): ByteArray

    fun encryptBase64(data: String): ByteArray
}

class StandardCipher : Cipher {
    private val base64Encoder = Base64.getEncoder()

    override val algorithm: Algorithm = Algorithm.HMAC256(AppEnvConfig["auth.password.cipher"])

    override fun encrypt(data: String): ByteArray {
        return algorithm.sign(data.toByteArray())
    }

    override fun encryptBase64(data: String): ByteArray {
        return base64Encoder.encode(encrypt(data))
    }
}
