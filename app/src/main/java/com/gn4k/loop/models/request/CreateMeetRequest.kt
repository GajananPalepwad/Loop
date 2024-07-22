package com.gn4k.loop.models.request

data class CreateMeetRequest(
    val meeting_id: String,
    val title: String,
    val description: String,
    val start_time: String,
    val host_id: Int
)
