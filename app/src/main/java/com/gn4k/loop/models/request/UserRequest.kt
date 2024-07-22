package com.gn4k.loop.models.request

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("targetId")
    val targetId: Int,
)
