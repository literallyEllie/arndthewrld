package com.arndthewld.app.web

import com.arndthewld.app.config.RoleState
import com.arndthewld.app.web.controller.AuthController
import com.arndthewld.app.web.controller.UserController
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.config.RouterConfig
import org.koin.core.component.KoinComponent

class Router(
    private val authController: AuthController,
    private val userController: UserController,
) : KoinComponent {

    fun register(config: RouterConfig) {
        config.apiBuilder {

            path("/") {
                get {
                    it.json("insert world and stuff here")
                }
            }

            // Auth
            path("auth") {
                post("login", authController::login, RoleState.ANYONE)
                post("logout", authController::logout, RoleState.AUTHENTICATED)
                post("register", authController::register, RoleState.ANYONE)

                path("oauth") {
                    get("{provider}/login", authController::oAuthLogin, RoleState.ANYONE)
                    get("{provider}/register", authController::oAuthRegister, RoleState.ANYONE)
                    get("callback", authController::oauthCallback, RoleState.ANYONE)
                }
            }

            // Users
            path("/user") {
                get("self", userController::getCurrent, RoleState.AUTHENTICATED)
                get("{username}", userController::getUser, RoleState.AUTHENTICATED)
            }

            path("/profile") {

            }
        }
    }
}