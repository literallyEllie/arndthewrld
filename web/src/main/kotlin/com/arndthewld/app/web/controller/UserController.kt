package com.arndthewld.app.web.controller

import com.arndthewld.app.domain.Profile
import com.arndthewld.app.domain.User
import com.arndthewld.app.domain.service.UserService
import com.arndthewld.app.ext.id
import io.javalin.http.Context

class UserController(private val userService: UserService) {

    fun getCurrent(ctx: Context): User {
        return userService.getByEmail(ctx.id())
    }

    fun getUser(ctx: Context): Profile {
        return userService.getProfileByUsername(ctx.pathParam("username"))
    }
}