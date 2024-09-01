package ui

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.*
import cengine.lang.RunConfiguration
import cengine.project.Project
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.layout.AppBar
import ui.uilib.params.FontType
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.LightTheme

@Composable
fun TopBar(
    project: Project,
    onClose: () -> Unit,
    runConfigurations: List<RunConfiguration.ProjectRun<*>>
){

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    AppBar(
        icon = icons.appLogo,
        title = project.projectState.target,
        name = project.projectState.absRootPath,

        actions = {

            // Run Configurations Menu
            var runConfigExpanded by remember { mutableStateOf(false) }
            CButton(onClick = { runConfigExpanded = true }, icon = icons.build)

            DropdownMenu(
                expanded = runConfigExpanded,
                onDismissRequest = { runConfigExpanded = false }
            ) {
                runConfigurations.forEach { config ->
                    DropdownMenuItem(onClick = {
                        config.run(project)
                        runConfigExpanded = false
                    }) {
                        CLabel(text = config.name, fontType = FontType.MEDIUM)
                    }
                }
            }

            CButton(onClick = {
                if (theme == LightTheme) {
                    UIState.Theme.value = DarkTheme
                } else {
                    UIState.Theme.value = LightTheme
                }
            }, icon = theme.icon)

            // Close Button
            CButton(onClick = onClose, icon = icons.close)
        }
    )


}