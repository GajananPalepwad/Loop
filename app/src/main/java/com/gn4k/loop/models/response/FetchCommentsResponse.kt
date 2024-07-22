package com.gn4k.loop.models.response

data class FetchCommentsResponse(
    val comments: List<Comment>
)

data class Comment(
    val id: Int,
    val post_id: Int,
    val author_id: Int,
    val comment_text: String,
    val created_at: String,
    val updated_at: String,
    var like_count: Int,
//    val liked_by: List<String>?,
    val report_count: Int,
    var comment_count: Int,
    val author_name: String,
    val author_photo_url: String,
    var liked: Boolean
)

