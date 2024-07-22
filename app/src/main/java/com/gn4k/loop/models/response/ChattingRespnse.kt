package com.gn4k.loop.models.response

data class ChattingRespnse(
    val status: String,
    val messages: List<Msg>
)

data class Msg(
    val id: Int,
    val conversation_id: Int,
    val sender_id: Int,
    val message: String,
    val sent_at: String
)
