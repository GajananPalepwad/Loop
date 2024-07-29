package com.gn4k.loop.models.request

data class NotificationRequest(
    val senderUserId: Int,
    val recipientUserId: Int,
    val postId: Int,
    val type: String,
    val message: String
)
