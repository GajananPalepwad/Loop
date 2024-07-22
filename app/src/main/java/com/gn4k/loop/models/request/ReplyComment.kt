package com.gn4k.loop.models.request

data class ReplyComment(
    val comment_id: Int,
    val author_id: Int,
    val reply_text: String,
)
