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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cengine.project.Project
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue
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
    var leftContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var rightContentType by remember { mutableStateOf<EmulatorContentView?>(null) }
    var bottomContentType by remember { mutableStateOf<EmulatorContentView?>(null) }

    val archOverview: (@Composable BoxScope.() -> Unit) = {
        ArchitectureOverview(architecture)
    }

    val memView: (@Composable BoxScope.() -> Unit) = {
        if (architecture != null) {
            MemView()
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
                    }, value = bottomContentType == EmulatorContentView.ArchOverview, icon = icons.processor)
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
                        architecture?.getRegByAddr(1.toValue(Size.Bit5))?.variable?.set(5.toValue())
                    })
                    CButton(icon = icons.continuousExe, onClick = {
                        architecture?.exeContinuous()
                        architecture?.memory?.store(Hex("8000", Size.Bit32), 5.toValue(Size.Bit8))
                    })
                    CButton(icon = icons.stepMultiple, text = stepCount.toString(), onClick = {
                        architecture?.exeMultiStep(stepCount.toLong())
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
