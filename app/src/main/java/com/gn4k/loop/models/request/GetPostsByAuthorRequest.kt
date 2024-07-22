package com.gn4k.loop.models.request

data class GetPostsByAuthorRequest(
    val author_id: String,
    val login_id: String
)
