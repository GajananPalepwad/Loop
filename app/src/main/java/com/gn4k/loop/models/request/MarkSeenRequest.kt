package com.gn4k.loop.models.request

data class MarkSeenRequest(
    val user_id: Int,
    val message_id: Int
)
