package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cengine.project.Project
import emulator.EmuLink
import ui.uilib.UIState
import ui.uilib.emulator.ArchitectureOverview
import ui.uilib.emulator.MemView
import ui.uilib.emulator.RegView
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.HorizontalToolBar
import ui.uilib.layout.ResizableBorderPanels
import ui.uilib.layout.VerticalToolBar
import ui.uilib.params.FontType

@Composable
fun EmulatorView(project: Project, viewType: MutableState<ViewType>, emuLink: EmuLink?, close: () -> Unit) {

    val theme = UIState.Theme.value
    val icons = UIState.Icon.value
    val architecture = remember { emuLink?.load() }

    var stepCount by remember { mutableStateOf(4U) }
    var accumulatedScroll by remember { mutableStateOf(0f) }
    val scrollThreshold = 100f
    var leftContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var rightContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var bottomContentType by remember { mutableStateOf<EmulatorContentView?>(null) }

    val archOverview: (@Composable BoxScope.() -> Unit) = {
        ArchitectureOverview(architecture)
    }

    val memView: (@Composable BoxScope.() -> Unit) = {
        if (architecture != null) {
            MemView(architecture)
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "No Architecture Selected!")
            }
        }
    }

    val regView: (@Composable BoxScope.() -> Unit) = {
        if (architecture != null) {
            RegView(architecture)
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "No Architecture Selected!")
            }
        }
    }

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
            TopBar(project, viewType, onClose = { close() }) {
                CLabel(text = "PC: ${architecture?.regContainer?.pc?.variable?.state?.value?.toHex()}", fontType = FontType.CODE)
            }
        },
        center = {
            ResizableBorderPanels(
                Modifier.fillMaxSize(),
                initialLeftWidth = 200.dp,
                initialBottomHeight = 200.dp,
                initialRightWidth = 200.dp,
                leftContent = when (leftContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    EmulatorContentView.ArchOverview -> archOverview
                    EmulatorContentView.RegView -> regView
                    EmulatorContentView.MemView -> memView
                    null -> null
                },
                centerContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {

                    }
                },
                rightContent = when (rightContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    EmulatorContentView.ArchOverview -> archOverview
                    EmulatorContentView.RegView -> regView
                    EmulatorContentView.MemView -> memView
                    null -> null
                },
                bottomContent = when (bottomContentType) {
                    EmulatorContentView.ObjFileSelection -> objFileSelector
                    EmulatorContentView.ArchOverview -> archOverview
                    EmulatorContentView.RegView -> regView
                    EmulatorContentView.MemView -> memView
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
                    }, value = bottomContentType == EmulatorContentView.ObjFileSelection, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {
                        bottomContentType = if (it && bottomContentType != EmulatorContentView.ArchOverview) {
                            EmulatorContentView.ArchOverview
                        } else null
                    }, value = bottomContentType == EmulatorContentView.ArchOverview, icon = icons.settings)
                    CToggle(onClick = {
                        bottomContentType = if (it && bottomContentType != EmulatorContentView.MemView) {
                            EmulatorContentView.MemView
                        } else null
                    }, value = bottomContentType == EmulatorContentView.MemView, icon = icons.bars)
                }
            )
        },
        right = {
            VerticalToolBar(
                upper = {
                    CButton(icon = icons.singleExe, onClick = {
                        architecture?.exeSingleStep()
                    })
                    CButton(icon = icons.continuousExe, onClick = {
                        architecture?.exeContinuous()
                    })
                    CButton(modifier = Modifier
                        .scrollable(orientation = Orientation.Vertical,
                            state = rememberScrollableState { delta ->
                                accumulatedScroll += delta

                                // Increment stepCount when scroll threshold is crossed
                                if (accumulatedScroll <= -scrollThreshold) {
                                    stepCount = stepCount.dec().coerceAtLeast(1U)
                                    accumulatedScroll = 0f // Reset after increment
                                } else if (accumulatedScroll >= scrollThreshold) {
                                    stepCount = stepCount.inc()
                                    accumulatedScroll = 0f // Reset after decrement
                                }
                                delta
                            })
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                // Adjust stepCount based on the dragAmount
                                if (dragAmount < 0) {
                                    stepCount = stepCount.inc() // Scroll up to increase
                                } else if (dragAmount > 0) {
                                    stepCount = stepCount.dec().coerceAtLeast(1U) // Scroll down to decrease, ensure it's >= 1
                                }
                            }
                        }, icon = icons.stepMultiple, text = stepCount.toString(), onClick = {
                        architecture?.exeMultiStep(stepCount.toLong()) // TODO Allow increasing or decreasing stepCount on scrolling on the button (mouse wheel)
                    })
                    CButton(icon = icons.stepOver, onClick = {
                        architecture?.exeSkipSubroutine()
                    })
                    CButton(icon = icons.stepOut, onClick = {
                        architecture?.exeReturnFromSubroutine()
                    })
                    CButton(icon = icons.refresh, onClick = {
                        architecture?.exeReset()
                    })
                },
                lower = {
                    CToggle(onClick = {
                        rightContentType = if (it && rightContentType != EmulatorContentView.RegView) {
                            EmulatorContentView.RegView
                        } else null
                    }, value = rightContentType == EmulatorContentView.RegView, icon = icons.processorBold)
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
    ObjFileSelection,
    ArchOverview,
    RegView,
    MemView
}
