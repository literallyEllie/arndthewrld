package com.arndthewld.app.web

import com.arndthewld.app.ext.createLogger
import com.auth0.jwt.exceptions.JWTVerificationException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.javalin.Javalin
import io.javalin.http.*
import org.eclipse.jetty.http.HttpStatus
import org.jetbrains.exposed.exceptions.ExposedSQLException

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ErrorResponseMessage(
    val message: String
)

internal data class ErrorResponse(
    val errors: Map<String, List<String?>> = emptyMap(),
    @JsonProperty("REQUEST_BODY")
    val internalErrors: List<ErrorResponseMessage> = emptyList()
) {
    val allErrors = (errors["body"] ?: emptyList()) + internalErrors.map { it.message }
}

object ErrorExceptionMapping {
    private val log by createLogger()

    fun register(app: Javalin) {
        // TODO

        app.exception(Exception::class.java) { e, ctx ->
            log.error("Exception occurred for req -> ${ctx.url()}", e)
            val error = ErrorResponse(mapOf("Unknown Error" to listOf(e.message ?: "Error occurred!")))
            ctx.json(error).status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }

        app.exception(ExposedSQLException::class.java) { e, ctx ->
            log.error("Exception occurred for req -> ${ctx.url()}", e)
            val error = ErrorResponse(mapOf("Unknown Error" to listOf("Error occurred!")))
            ctx.json(error).status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }

        app.exception(BadRequestResponse::class.java) { e, ctx ->
            log.warn("BadRequestResponse occurred for req -> ${ctx.url()}")
            val error = ErrorResponse(mapOf("body" to listOf(e.message ?: "can't be empty or invalid")))
            ctx.json(error).status(HttpStatus.UNPROCESSABLE_ENTITY_422)
        }

        app.exception(UnauthorizedResponse::class.java) { e, ctx ->
            log.warn("UnauthorizedResponse occurred for req -> ${ctx.url()}")
            val error = ErrorResponse(mapOf("login" to listOf(e.message ?: "You cannot do this authenticated!")))
            ctx.json(error).status(HttpStatus.UNAUTHORIZED_401)
        }

        app.exception(ForbiddenResponse::class.java) { _, ctx ->
            log.warn("ForbiddenResponse occurred for req -> ${ctx.url()}")
            val error = ErrorResponse(mapOf("login" to listOf("User doesn't have permissions to perform the action!")))
            ctx.json(error).status(HttpStatus.FORBIDDEN_403)
        }

        app.exception(JWTVerificationException::class.java) { e, ctx ->
            log.error("JWTVerificationException occurred for req -> ${ctx.url()}", e)
            val error = ErrorResponse(mapOf("token" to listOf(e.message ?: "Invalid JWT token!")))
            ctx.json(error).status(HttpStatus.UNAUTHORIZED_401)
        }

        app.exception(NotFoundResponse::class.java) { e, ctx ->
            log.warn("NotFoundResponse occurred for req -> ${ctx.url()}")
            val error =
                ErrorResponse(mapOf("body" to listOf(e.message ?: "Resource can't be found to fulfill the request.")))
            ctx.json(error).status(HttpStatus.NOT_FOUND_404)
        }

        app.exception(HttpResponseException::class.java) { e, ctx ->
            log.warn("HttpResponseException occurred for req -> ${ctx.url()}")
            val error = ErrorResponse(mapOf("body" to listOf(e.message)))
            ctx.json(error).status(e.status)
        }
    }
}