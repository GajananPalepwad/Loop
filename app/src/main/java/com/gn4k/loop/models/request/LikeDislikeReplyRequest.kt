package com.gn4k.loop.models.request

data class LikeDislikeReplyRequest(
    var reply_id: Int,
    var login_id: Int
)
