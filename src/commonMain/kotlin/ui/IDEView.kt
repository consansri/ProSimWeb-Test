package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import cengine.lang.RunConfiguration
import cengine.project.Project
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import kotlinx.serialization.Serializable
import ui.uilib.UIState
import ui.uilib.editor.CodeEditor
import ui.uilib.editor.ObjectEditor
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.*

@Composable
fun IDEView(
    project: Project,
    viewType: MutableState<ViewType>,
    close: () -> Unit
) {
    val theme = UIState.Theme.value
    val icons = UIState.Icon.value

    val projectState = project.projectState
    val ideState = projectState.ide

    val baseStyle = UIState.BaseStyle.current
    val codeStyle = UIState.CodeStyle.current
    val baseLargeStyle = UIState.BaseLargeStyle.current
    val codeSmallStyle = UIState.CodeSmallStyle.current
    val baseSmallStyle = UIState.BaseSmallStyle.current

    val fileEditors = remember {
        mutableStateListOf<TabItem<VirtualFile>>(*ideState.openFiles.mapNotNull { path ->
            nativeLog("Path: $path -> ${project.fileSystem.findFile(path)}")
            project.fileSystem.findFile(path)
        }.map { file ->
            TabItem(file, icons.file, file.name)
        }.toTypedArray())
    }
    var leftContentType by remember { mutableStateOf<ToolContentType?>(ideState.leftContent) }
    var rightContentType by remember { mutableStateOf<ToolContentType?>(ideState.rightContent) }
    var bottomContentType by remember { mutableStateOf<ToolContentType?>(ideState.bottomContent) }

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
                    ideState.openFiles = fileEditors.map { it.value.path }
                }
            }
        }
    }

    BorderLayout(
        Modifier.fillMaxSize().background(theme.COLOR_BG_0),
        top = {
            TopBar(project, viewType, onClose = { close() }) {
                // Run Configurations Menu
                val runConfigs = project.services.flatMap { it.runConfigurations.filterIsInstance<RunConfiguration.ProjectRun<*>>() }
                var runConfigExpanded by remember { mutableStateOf(false) }
                CButton(onClick = { runConfigExpanded = true }, icon = icons.build)

                DropdownMenu(
                    expanded = runConfigExpanded,
                    onDismissRequest = { runConfigExpanded = false }
                ) {
                    runConfigs.forEach { config ->
                        DropdownMenuItem(onClick = {
                            config.run(project)
                            runConfigExpanded = false
                        }) {
                            CLabel(text = config.name, textStyle = baseStyle)
                        }
                    }
                }
            }
        },
        center = {
            with(LocalDensity.current) {
                ResizableBorderPanels(
                    Modifier.fillMaxSize(),
                    initialLeftWidth = ideState.leftWidth.toDp(),
                    initialBottomHeight = ideState.bottomHeight.toDp(),
                    initialRightWidth = ideState.rightWidth.toDp(),
                    leftContent = when (leftContentType) {
                        ToolContentType.FileTree -> fileTree
                        null -> null
                    },
                    centerContent = {
                        Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {
                            // Center content
                            TabbedPane(fileEditors, closeable = true, content = { index ->
                                // Display File Content
                                key(fileEditors[index].value.path) {
                                    when {
                                        fileEditors[index].value.name.endsWith(".o") -> {
                                            ObjectEditor(
                                                fileEditors[index].value,
                                                project,
                                                codeStyle,
                                                baseLargeStyle,
                                                baseStyle
                                            )
                                        }

                                        else -> {
                                            CodeEditor(
                                                fileEditors[index].value,
                                                project,
                                                codeStyle,
                                                codeSmallStyle,
                                                baseSmallStyle
                                            )
                                        }
                                    }
                                }

                            }, baseStyle) {
                                fileEditors.remove(it)
                                ideState.openFiles = fileEditors.map { it.value.path }
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
                    },
                    onLeftWidthChange = {
                        ideState.leftWidth = it.value
                    },
                    onBottomHeightChange = {
                        ideState.bottomHeight = it.value
                    },
                    onRightWidthChange = {
                        ideState.rightWidth = it.value
                    }
                )
            }

        },
        left = {
            VerticalToolBar(
                upper = {
                    CToggle(onClick = {
                        leftContentType = if (leftContentType != ToolContentType.FileTree) {
                            ToolContentType.FileTree
                        } else null
                    }, value = leftContentType == ToolContentType.FileTree, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {

                    }, value = false, icon = icons.statusError)

                    CToggle(onClick = {

                    }, value = false, icon = icons.console)

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

    LaunchedEffect(leftContentType) {
        ideState.leftContent = leftContentType
    }

    LaunchedEffect(rightContentType) {
        ideState.rightContent = rightContentType
    }

    LaunchedEffect(bottomContentType) {
        ideState.bottomContent = bottomContentType
    }
}

@Serializable
enum class ToolContentType {
    FileTree;
}