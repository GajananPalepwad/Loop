package com.gn4k.loop.models.request

data class AddBadgeRequest(
    val userId: Int,
    val badge: String
)