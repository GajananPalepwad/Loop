package com.gn4k.loop.models.response


data class NotificationListResponse(
    val message: String,
    val notifications: List<NotificationResponse>
)

data class NotificationResponse(
    val notification_id: Int,
    val recipient_user_id: Int,
    val sender_user_id: Int,
    val post_id: Int,
    val type: String,
    val message: String,
    val is_read: Int,
    val created_at: String,
    val updated_at: String,
    val sender_profile_url: String
)
