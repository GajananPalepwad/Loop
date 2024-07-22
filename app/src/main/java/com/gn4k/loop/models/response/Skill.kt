package com.gn4k.loop.models.response


data class Skill(
    val message: String,
    val skills: List<Skills>
)

data class Skills(
    val skill_id: Int,
    val skill: String
)