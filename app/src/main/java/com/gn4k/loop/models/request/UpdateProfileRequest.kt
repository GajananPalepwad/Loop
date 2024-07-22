package com.gn4k.loop.models.request

data class UpdateProfileRequest(
    val userId: Int,
    val name: String,
    val about: String,
    val location: String,
    val website: String
)
