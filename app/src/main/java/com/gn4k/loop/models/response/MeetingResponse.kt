package com.gn4k.loop.models.response

data class MeetingResponse(
    val message: String,
    val meetings: List<MeetingObject>
    )


data class MeetingObject(
    val meeting_id: String,
    val title: String,
    val description: String,
    val start_time: String,
    val host_id: Int,
    val joined_count: Int,
    val joined_id_list: List<Int>,
    val interested_count: Int,
    val interested_id_list: List<Int>,
    )
