package com.gn4k.loop.models.request

data class RegisterRequest(
    val email: String,
    val username: String,
    val name: String,
    val password: String,

    )
