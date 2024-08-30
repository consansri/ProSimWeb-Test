package cengine.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectState(
    val name: String,
    val absRootPath: String,
    val target: String
)
