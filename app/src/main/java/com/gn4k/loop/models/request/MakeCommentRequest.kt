package com.gn4k.loop.models.request

data class MakeCommentRequest (
    val post_id: Int,
    val author_id: Int,
    val comment_text: String,
)