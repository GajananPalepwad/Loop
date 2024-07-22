package com.gn4k.loop.models.request

data class LikeDislikeCommentRequest(
    var comment_id: Int,
    var login_id: Int
)
