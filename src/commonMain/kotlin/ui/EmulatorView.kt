package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cengine.lang.RunConfiguration
import cengine.project.Project
import ui.uilib.UIState
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.HorizontalToolBar
import ui.uilib.layout.ResizableBorderPanels
import ui.uilib.layout.VerticalToolBar

@Composable
fun EmulatorView(project: Project, viewType: MutableState<ViewType>, close: () -> Unit) {

    val theme = UIState.Theme.value
    val icons = UIState.Icon.value

    var leftContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var rightContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var bottomContentType by remember { mutableStateOf<EmulatorContentView?>(null) }

    val objFileSelector: (@Composable BoxScope.() -> Unit) = {
        val leftVScrollState = rememberScrollState()
        val leftHScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIState.Theme.value.COLOR_BG_1)
                .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                .scrollable(leftHScrollState, Orientation.Horizontal)
                .scrollable(leftVScrollState, Orientation.Vertical)
        ) {
            // Left content
            FileTree(project) { file ->
                // TODO
            }
        }
    }

    BorderLayout(
        Modifier.fillMaxSize().background(theme.COLOR_BG_0),
        top = {
            TopBar(project, viewType, onClose = { close() }, project.services.flatMap { it.runConfigurations.filterIsInstance<RunConfiguration.ProjectRun<*>>() })
        },
        center = {
            ResizableBorderPanels(
                Modifier.fillMaxSize(),
                initialLeftWidth = 200.dp,
                initialBottomHeight = 200.dp,
                initialRightWidth = 200.dp,
                leftContent = when (leftContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    null -> null
                },
                centerContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {

                    }
                },
                rightContent = when (rightContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    null -> null
                },
                bottomContent = when (bottomContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    null -> null
                }
            )
        },
        left = {
            VerticalToolBar(
                upper = {
                    CToggle(onClick = {
                        leftContentType = if (leftContentType != EmulatorContentView.ObjFileSelection) {
                            EmulatorContentView.ObjFileSelection
                        } else null
                    }, initialToggle = false, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {

                    }, initialToggle = false, icon = icons.processor)
                }
            )
        },
        right = {
            VerticalToolBar(
                upper = {
                    CButton(icon = icons.singleExe, onClick = {

                    })
                    CButton(icon = icons.continuousExe, onClick = {

                    })
                    CButton(icon = icons.stepMultiple, onClick = {

                    })
                    CButton(icon = icons.stepInto, onClick = {

                    })
                    CButton(icon = icons.stepOver, onClick = {

                    })
                    CButton(icon = icons.stepOut, onClick = {

                    })
                    CButton(icon = icons.refresh, onClick = {

                    })
                },
                lower = {

                }
            )
        },
        bottom = {
            HorizontalToolBar(
                left = {

                },
                right = {

                }
            )
        },
        leftBg = theme.COLOR_BG_1,
        rightBg = theme.COLOR_BG_1,
        bottomBg = theme.COLOR_BG_1
    )


}

enum class EmulatorContentView {
    ObjFileSelection
}
