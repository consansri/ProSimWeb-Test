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
import cengine.lang.RunConfiguration
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.editor.CodeEditor
import ui.uilib.editor.ObjectEditor
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CToggle
import ui.uilib.layout.*


@Composable
fun ProjectViewScreen(state: ProjectState, close: () -> Unit) {
    val theme = UIState.Theme.value
    val icons = UIState.Icon.value

    val fileEditors = remember { mutableStateListOf<TabItem<VirtualFile>>() }
    var leftContentType by remember { mutableStateOf<ToolContentType?>(null) }

    val project = Project(state, CownLang())

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
            TopBar(project, onClose = { close() }, project.services.flatMap { it.runConfigurations.filterIsInstance<RunConfiguration.ProjectRun<*>>() })
        },
        center = {
            ResizableBorderPanels(
                Modifier.fillMaxSize(),
                leftContent = when (leftContentType) {
                    ToolContentType.FileTree -> fileTree
                    null -> null
                },
                centerContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {
                        // Center content
                        TabbedPane(fileEditors, content = { index ->
                            // Display File Content
                            nativeLog("Displaying Editor for ${fileEditors[index].title}!")
                            key(fileEditors[index].value.path) {
                                when{
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
                rightContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_1)) {
                        // Right content

                    }
                },
                bottomContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_1)) {
                        // Bottom content
                    }
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

                    }, initialToggle = false, icon = icons.processor)

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
                    CToggle(onClick = {

                    }, initialToggle = false, icon = icons.processor)
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
