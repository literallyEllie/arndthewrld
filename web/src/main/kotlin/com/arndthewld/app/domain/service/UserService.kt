package com.arndthewld.app.domain.service

import com.arndthewld.app.domain.Profile
import com.arndthewld.app.domain.User
import com.arndthewld.app.domain.repository.UserRepository
import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import org.eclipse.jetty.http.HttpStatus
import org.gradle.internal.impldep.org.apache.http.client.HttpResponseException
import java.time.LocalDateTime

class UserService(private val repository: UserRepository) {

    /**
     * Creates a user.
     *
     * Note that password encryption is handled by the [AuthService]
     */
    fun create(user: User): User {
        // Check for dupes
        repository.findUserByEmail(user.email).takeIf { it != null }?.apply {
            throw HttpResponseException(
                HttpStatus.BAD_REQUEST_400,
                "Email already registered!",
            )
        }

        if (user.username != null) {
            repository.findUserByUsername(user.username).takeIf { it != null }?.apply {
                throw HttpResponseException(
                    HttpStatus.BAD_REQUEST_400,
                    "Username already in use!",
                )
            }
        }

        return user.copy(createdAt = LocalDateTime.now()).also {
            repository.insertUser(it)
        }
    }

    fun getByEmail(email: String?): User {
        if (email.isNullOrBlank()) {
            throw BadRequestResponse("invalid email")
        }

        // TODO do we need to insert the token?
        return repository.findUserByEmail(email) ?: throw NotFoundResponse()
    }

    fun getByUsername(username: String?): User {
        if (username.isNullOrBlank()) {
            throw BadRequestResponse("invalid username")
        }

        return repository.findUserByUsername(username) ?: throw NotFoundResponse()
    }

    fun getProfileByUsername(username: String?): Profile {
        if (username.isNullOrBlank()) {
            throw BadRequestResponse("invalid username")
        }

        return repository.findUserByUsername(username)?.toProfile() ?: throw NotFoundResponse()
    }
}