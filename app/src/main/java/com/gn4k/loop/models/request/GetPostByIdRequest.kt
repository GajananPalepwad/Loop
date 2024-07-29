package com.gn4k.loop.models.request

data class GetPostByIdRequest(
    val post_id: Int,
    val login_id: Int
)
