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
import cengine.vfs.VirtualFile
import ui.uilib.UIState
import ui.uilib.editor.CodeEditor
import ui.uilib.editor.ObjectEditor
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CToggle
import ui.uilib.layout.*

@Composable
fun IDEView(project: Project, viewType: MutableState<ViewType>, close: () -> Unit) {
    val theme = UIState.Theme.value
    val icons = UIState.Icon.value

    val fileEditors = remember { mutableStateListOf<TabItem<VirtualFile>>() }
    var leftContentType by remember { mutableStateOf<ToolContentType?>(null) }
    var rightContentType by remember { mutableStateOf<ToolContentType?>(null) }
    var bottomContentType by remember { mutableStateOf<ToolContentType?>(null) }

    val fileTree: (@Composable BoxScope.() -> Unit) = {
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
                if (file.isFile && !fileEditors.any { it.value == file }) {
                    fileEditors.add(TabItem(file, icons.file, file.name))
                }
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
                    ToolContentType.FileTree -> fileTree
                    null -> null
                },
                centerContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {
                        // Center content
                        TabbedPane(fileEditors, content = { index ->
                            // Display File Content
                            key(fileEditors[index].value.path) {
                                when {
                                    fileEditors[index].value.name.endsWith(".o") -> {
                                        ObjectEditor(
                                            fileEditors[index].value,
                                            project
                                        )
                                    }

                                    else -> {
                                        CodeEditor(
                                            fileEditors[index].value,
                                            project
                                        )
                                    }
                                }
                            }

                        }) {
                            fileEditors.remove(it)
                        }
                    }
                },
                rightContent = when (rightContentType) {
                    ToolContentType.FileTree -> fileTree
                    null -> null
                },
                bottomContent = when (bottomContentType) {
                    ToolContentType.FileTree -> fileTree
                    null -> null
                }
            )
        },
        left = {
            VerticalToolBar(
                upper = {
                    CToggle(onClick = {
                        leftContentType = if (leftContentType != ToolContentType.FileTree) {
                            ToolContentType.FileTree
                        } else null
                    }, initialToggle = false, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {

                    }, initialToggle = false, icon = icons.statusError)

                    CToggle(onClick = {

                    }, initialToggle = false, icon = icons.console)

                }
            )
        },
        right = {
            VerticalToolBar(
                upper = {

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

enum class ToolContentType {
    FileTree;
}