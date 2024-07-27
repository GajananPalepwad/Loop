package com.gn4k.loop.models.request

data class UpdateProjectRequest(
    val project_id: Int,
    val title: String,
    val description: String,
    val status: String,
    val link_preview: String,
    val tags: List<String>,
)
