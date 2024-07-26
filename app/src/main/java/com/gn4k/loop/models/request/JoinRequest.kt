package com.gn4k.loop.models.request

data class JoinRequest(
    val userId: Int,
    val projectId: Int,
    val action: String

)
