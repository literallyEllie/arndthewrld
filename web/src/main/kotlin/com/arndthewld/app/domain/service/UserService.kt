package com.arndthewld.app.domain.service

import co.touchlab.kermit.Logger
import com.arndthewld.app.domain.Profile
import com.arndthewld.app.domain.User
import com.arndthewld.app.domain.repository.ProfileRepository
import com.arndthewld.app.domain.repository.UserRepository
import com.arndthewld.app.domain.service.auth.AuthService
import com.arndthewld.app.web.request.AuthenticationRequest
import io.javalin.http.BadRequestResponse
import io.javalin.http.HttpResponseException
import io.javalin.http.NotFoundResponse
import org.eclipse.jetty.http.HttpStatus

class UserService(
    private val repository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val authService: AuthService,
) {
    /**
     * Creates a user.
     */
    fun create(request: AuthenticationRequest): User {
        Logger.withTag("usr").d { "create($request)" }

        // email dupe
        repository.findUserByEmail(request.email!!).takeIf { it != null }?.apply {
            throw HttpResponseException(
                HttpStatus.BAD_REQUEST_400,
                "Email already registered!",
            )
        }

        // username dupe
        request.username?.also { username ->
            repository.findUserByUsername(username).takeIf { it != null }?.apply {
                throw HttpResponseException(
                    HttpStatus.BAD_REQUEST_400,
                    "Username already in use!",
                )
            }
        }

        val user = User(0, request.email!!, request.username)

        // insert user
        val userId = repository.insertUser(user)
        Logger.withTag("usr").d { "new user: $userId" }

        // insert credentials
        authService.register(userId, request)
        Logger.withTag("usr").d { "credentials registered: $userId" }

        return user.copy(userId = userId)
    }

    fun getUserId(request: AuthenticationRequest): Long {
        val userId =
            if (request.email != null) {
                repository.findUserIdByEmail(request.email!!)
            } else if (request.username != null) {
                repository.findUserIdByUsername(request.username!!)
            } else {
                throw BadRequestResponse("email or username not set")
            }

        return userId ?: throw NotFoundResponse("incorrect credentials")
    }

    fun login(userId: Long): User {
        val user = repository.findByUserId(userId) ?: throw NotFoundResponse("User not found")

        repository.markLastLogin(userId)

        return user
    }

    fun getByUserId(userId: Long): User {
        return repository.findByUserId(userId) ?: throw NotFoundResponse()
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

        val userId = repository.findUserIdByUsername(username) ?: throw NotFoundResponse()
        return profileRepository.findByUserId(userId) ?: Profile.empty(userId)
    }
}
