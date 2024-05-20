package com.arndthewld.app.web.controller

import com.arndthewld.app.domain.service.UserService
import com.arndthewld.app.ext.userId
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse

class UserController(private val userService: UserService) {
    fun getCurrent(ctx: Context) {
        val userId = ctx.userId() ?: throw UnauthorizedResponse()
        ctx.json(userService.getByUserId(userId))
    }

    fun getProfile(ctx: Context) {
        ctx.json(userService.getProfileByUsername(ctx.pathParam("username")))
    }
}
