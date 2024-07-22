package com.gn4k.loop.models.request

data class LikeDislikeRequest(
    var post_id: Int,
    var login_id: Int
)
