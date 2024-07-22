package com.gn4k.loop.models.request

data class SendMsgRequest(
    val sender_id: Int,
    val receiver_id: Int,
    val message: String
    )
