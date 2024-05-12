package com.arndthewld.app.domain

import java.time.LocalDateTime

enum class ProfileRole {
    USER, CONTRIBUTOR, MODERATOR, ADMIN;

    val isStaff: Boolean
        get() = this == ADMIN

    fun isEqualOrGreaterThan(role: ProfileRole): Boolean {
        return this.ordinal >= role.ordinal
    }
}

data class User(
    val email: String, val username: String?, val role: ProfileRole = ProfileRole.USER,
    @Transient val token: String? = null,

    val createdAt: LocalDateTime? = null, val lastLogin: LocalDateTime? = null,
    // Other stuff
    val biography: String? = null, val avatar: String? = null,
) {

    fun toProfile(): Profile {
        return Profile(username, createdAt, lastLogin, biography, avatar)
    }
}

