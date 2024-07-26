package com.gn4k.loop.models.request

data class AddProjectRequest(
    val title: String,
    val description: String,
    val link_preview: String,
    val status: String,
    val joined_persons: List<Person>,
    var requested_persons: List<Person>,
    val tags: List<String>,
    val author_id: Int
    )

data class Person(
    val id: Int
)

