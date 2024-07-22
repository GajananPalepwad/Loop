package com.gn4k.loop.models.response

data class SearchUserResponse(
    val message: String,
    val results: List<SearchUser>
)

data class SearchUser(
    val id: Int,
    val username: String,
    val name: String,
    val photo_url: String?,
    val about: String?,
    val location: String?,
    val badges: List<String>?
)
