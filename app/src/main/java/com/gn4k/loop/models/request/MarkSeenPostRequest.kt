package com.gn4k.loop.models.request

data class MarkSeenPostRequest(
    val login_id: Int,
    val post_ids: List<Int>
)
