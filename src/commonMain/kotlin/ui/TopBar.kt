package ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import cengine.project.Project
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.layout.AppBar
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.LightTheme

@Composable
fun TopBar(
    project: Project,
    viewType: MutableState<ViewType>,
    onClose: () -> Unit,
    customContent: @Composable RowScope.() -> Unit = {}
) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    AppBar(
        icon = icons.appLogo,
        title = project.projectState.target,
        name = project.projectState.absRootPath,
        type = viewType.value.name,
        actions = {

            customContent()

            CButton(onClick = {
                if (theme == LightTheme) {
                    UIState.Theme.value = DarkTheme
                } else {
                    UIState.Theme.value = LightTheme
                }
            }, icon = theme.icon)

            CButton(onClick = {
                viewType.component2()(viewType.value.next())
            }, icon = viewType.value.next().icon)

            // Close Button
            CButton(onClick = onClose, icon = icons.close)
        }
    )


}