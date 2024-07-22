package com.gn4k.loop.models.response

data class FollowerResponse(
    val message: String,
    val followers: List<Follower>,
)

data class Follower(
    val id: Int,
    val name: String,
    val photo_url: String,
    val badges: List<String>,
)
