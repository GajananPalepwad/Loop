package com.gn4k.loop.models.request

data class JoinLeaveMeetRequest(
    val meeting_id: String,
    val user_id: Int,
    val action: String,
)
