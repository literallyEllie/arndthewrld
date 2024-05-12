package com.arndthewld.app.config

import com.arndthewld.app.config.environment.AppEnvConfig
import com.arndthewld.app.config.oauth.OAuthConfig
import com.arndthewld.app.config.security.Cipher
import com.arndthewld.app.config.security.JwtProvider
import com.arndthewld.app.config.security.StandardCipher
import com.arndthewld.app.domain.repository.AuthRepository
import com.arndthewld.app.domain.repository.UserRepository
import com.arndthewld.app.domain.service.UserService
import com.arndthewld.app.domain.service.auth.AuthService
import com.arndthewld.app.web.Router
import com.arndthewld.app.web.controller.AuthController
import com.arndthewld.app.web.controller.UserController
import org.koin.dsl.module
import org.pac4j.oauth.client.Google2Client

object ModulesConfig {
    private val configModule = module {
        single { AppConfig() }
        // security
        factory<Cipher> { StandardCipher() }
        single { JwtProvider(get()) }
        single { AuthConfig(get()) }
        // oauth
        single {
            Google2Client(
                AppEnvConfig["auth.oauth.platforms.google.key"], AppEnvConfig["auth.oauth.platforms.google.secret"]
            )
        }
        single { OAuthConfig(get()) }
        // db
        single {
            SqlConfig(
                AppEnvConfig.assertValue("db.mysql.url"),
                AppEnvConfig.assertValue("db.mysql.username"),
                AppEnvConfig.assertValue("db.mysql.password")
            ).getDataSource()
        }
        // http
        single { Router(get(), get()) }
    }
    private val authModules = module {
        single { AuthController(get()) }
        single { AuthService(get(), get(), get(), get()) }
        single { AuthRepository(get()) }
    }
    private val userModules = module {
        single { UserController(get()) }
        single { UserService(get()) }
        single { UserRepository() }
    }


    internal val allModules = listOf(
        configModule,
        authModules,
        userModules
    )
}