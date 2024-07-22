package com.gn4k.loop.models.response

data class FollowingResponse(
    val message: String,
    val following: List<Following>,
)

data class Following(
    val id: Int,
    val name: String,
    val photo_url: String,
    val badges: List<String>,
)
