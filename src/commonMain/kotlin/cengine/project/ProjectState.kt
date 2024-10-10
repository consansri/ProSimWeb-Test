package cengine.project

import kotlinx.serialization.Serializable
import ui.ViewType

@Serializable
data class ProjectState(
    val absRootPath: String,
    val target: String,
    val viewType: ViewType
)
