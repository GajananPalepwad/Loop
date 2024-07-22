package com.gn4k.loop.models.response
import com.google.gson.annotations.SerializedName

data class UserAllDataResponse(
    val status: String,
    val message: String,
    val user: User,
    val is_following: Boolean, //i am following other
    val is_followed_by: Boolean, // other following me
    )

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val name: String,
    val badges: List<String>?,
    val about: String?,
    val photo_url: String?,
    val created_at: String,
    val updated_at: String,
    val last_login: String?,
    val status: String?,
    val role: String?,
    val location: String?,
    val website: String?,
    val skills: List<String>?,
    val followers_count: Int,
    val following_count: Int
)
