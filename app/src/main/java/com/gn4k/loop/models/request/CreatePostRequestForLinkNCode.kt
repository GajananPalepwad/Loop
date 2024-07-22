package com.gn4k.loop.models.request

data class CreatePostRequestForLinkNCode(
    val author_id: String,
    val context: String,
    val type: String,
    val tags: String?,
    val parent_post_id: String?,
    val link: String?
)
