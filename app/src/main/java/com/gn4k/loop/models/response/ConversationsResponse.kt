package com.gn4k.loop.models.response

data class ConversationsResponse(
    val status: String,
    val conversations: List<Conversation>,
    val excluded_count: Int
)


data class Conversation(
    val conversation_id: Int,
    val opposite_user_id: Int,
    val opposite_user_name: String,
    val opposite_user_photo_url: String?,
    val last_message: String,
    val last_message_sent_at: String,
    val is_seen_by_user: Boolean
)
