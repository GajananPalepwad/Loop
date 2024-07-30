package com.gn4k.loop.models.response

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id") val postId: String,
    @SerializedName("author_id") val authorId: String,
    val time: String,
    val context: String,
    val type: String,
    val link: String?,
    @SerializedName("like_count") var likeCount: Int,
    @SerializedName("comment_count") var commentCount: Int,
    @SerializedName("reports") val reports: String,
    val visibility: String,
    val tags: String?,
    val edited: String,
    @SerializedName("parent_post_id") val parentPostId: String?,
    val author_name: String,
    val author_photo_url: String?,
    @SerializedName("is_liked") var isLiked: Boolean?,
    val liked_by: String?,
)

data class Posts(
    val posts: List<Post>
)