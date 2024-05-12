package com.arndthewld.app.domain

import java.time.LocalDateTime

/**
 * A profile in the perspective of a [User].
 */
data class Profile(
    val username: String?, val createdAt: LocalDateTime? = null, val lastLogin: LocalDateTime? = null,
    val bio: String? = null, val avatar: String? = null,
)