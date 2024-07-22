package com.gn4k.loop.models.response

data class ReplyListResponse(
    val replies: List<Reply>
)

data class Reply(
    val id: Int,
    val comment_id: Int,
    val author_id: Int,
    val reply_text: String,
    val created_at: String,
    val updated_at: String,
    var like_count: Int,
    val liked_by: Any?,
    val report_count: Int,
    val reply_count: Int,
    val author_name: String,
    val author_photo_url: String,
    var liked: Boolean
)
