package com.arndthewld.app.domain

import kotlinx.datetime.LocalDateTime

enum class ProfileRole {
    USER,
    CONTRIBUTOR,
    MODERATOR,
    ADMIN,
    ;

    val isStaff: Boolean
        get() = this == ADMIN

    fun isEqualOrGreaterThan(role: ProfileRole): Boolean {
        return this.ordinal >= role.ordinal
    }
}

data class User(
    val userId: Long,
    val email: String,
    val username: String?,
    val role: ProfileRole = ProfileRole.USER,
    val createdAt: LocalDateTime? = null,
    val lastLogin: LocalDateTime? = null,
)
