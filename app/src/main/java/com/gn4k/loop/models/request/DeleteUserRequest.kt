package com.gn4k.loop.models.request

import com.google.gson.annotations.SerializedName

data class DeleteUserRequest(
    @SerializedName("user_id")
    val userId: Int,
    val password: String
)
