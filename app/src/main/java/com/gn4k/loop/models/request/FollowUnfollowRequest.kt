package com.gn4k.loop.models.request

data class FollowUnfollowRequest (
    val followerId: Int,
    val followingId: Int,
    val action: String,
    )