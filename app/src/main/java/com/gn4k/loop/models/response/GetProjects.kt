package com.gn4k.loop.models.response

data class GetProjects(
    val message: String,
    val projects: List<Project>
)
data class Project(
    val project_id: Int,
    val title: String,
    val status: String,
    val description: String,
    val link_preview: String,
    val created_at: String,
    val joined_persons: List<ParticipantList>,
    val requested_people: List<ParticipantList>,
    val tags: List<String>,
    val author_id: Int,
    val updated_at: String
)


data class ParticipantList(
    val id: Int,
    val name: String,
    val photo_url: String
)

