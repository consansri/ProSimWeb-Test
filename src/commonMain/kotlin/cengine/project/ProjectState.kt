package cengine.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectState(
    val absRootPath: String,
    val target: String
)
