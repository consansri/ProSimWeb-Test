package cengine.project

import kotlinx.serialization.Serializable
import ui.uilib.scale.Scaling
import ui.uilib.theme.LightTheme
import ui.uilib.theme.Theme

@Serializable
data class AppState(
    val scale: Float = 1.0f,
    val theme: String = LightTheme.name,
    val currentlyOpened: Int? = null,
    val projectStates: List<ProjectState> = listOf()
) {

    companion object {
        val initial = AppState()
    }

    constructor(scaling: Scaling, theme: Theme, currentlyOpened: Int?, projectStates: List<ProjectState>) : this(scaling.scale, theme.name, currentlyOpened, projectStates)

    fun getTheme(): Theme = Theme.all.firstOrNull { it.name == theme } ?: LightTheme

    fun getScaling(): Scaling = Scaling(scale)

}
