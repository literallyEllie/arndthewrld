package com.arndthewld.app.domain

/**
 * A profile
 */
data class Profile(
    val userId: Long,
    val bio: String?,
    val avatar: String?,
) {
    companion object {
        fun empty(userId: Long): Profile = Profile(userId, null, null)
    }
}
