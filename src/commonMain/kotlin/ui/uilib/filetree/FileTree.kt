package ui.uilib.filetree

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cengine.vfs.FPath
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.dialog.InputDialog
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.params.FontType
import ui.uilib.params.IconType

@Composable
fun FileTree(vfs: VFileSystem) {

    val expandedItems = remember { mutableStateListOf<VirtualFile>() }
    var root by remember { mutableStateOf(vfs.root) }

    var selectedFile by remember { mutableStateOf<VirtualFile?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }
    var showInputDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var onConfirm = remember { mutableStateOf<(String) -> Unit>({}) }

    if (showMenu && selectedFile != null) {
        FileContextMenu(
            file = selectedFile!!,
            position = contextMenuPosition,
            onDismiss = { showMenu = false },
            onRename = { file ->
                dialogTitle = "Rename File"
                onConfirm.value = { newName ->
                    vfs.renameFile(file.path, newName)
                    root = vfs.root // Force recomposition
                    showInputDialog = false
                }
                showInputDialog = true
            },
            onCreate = { file, isDirectory ->
                dialogTitle = if (isDirectory) "Create Directory" else "Create File"
                onConfirm.value = { newName ->
                    vfs.createFile(file.path + FPath.delimited(newName), isDirectory)
                    root = vfs.root // Force recomposition
                    showInputDialog = false
                }
                showInputDialog = true
            },
            onDelete = { file ->
                vfs.deleteFile(file.path)
                root = vfs.root // Force recomposition
                showMenu = false
            }
        )
    }

    // Input Dialog for creating or renaming files
    if (showInputDialog) {
        InputDialog(
            dialogTitle,
            onConfirm.value,
            onDismiss = {
                showInputDialog = false
            },
            valid = {
                val path = selectedFile!!.path + it
                vfs.findFile(path) != null
            }
        )
    }

    LazyColumn {
        node(
            root,
            isExpanded = {
                expandedItems.contains(it)
            },
            toggleExpanded = {
                if (expandedItems.contains(it)) {
                    expandedItems.remove(it)
                } else {
                    expandedItems.add(it)
                }
            },
            onClick = {
                nativeLog("Left Click ${it.name}")
            },
            onRightClick = { file, offset ->
                selectedFile = file
                contextMenuPosition = offset
                showMenu = true
            },
            depth = 0.dp,
            expandWidth = IconType.SMALL.getSize() + UIState.Scale.value.SIZE_INSET_MEDIUM * 2
        )
    }

    LaunchedEffect(Unit) {
        vfs.addChangeListener(object : FileChangeListener {
            override fun onFileChanged(file: VirtualFile) {
                root = vfs.root
            }

            override fun onFileCreated(file: VirtualFile) {
                root = vfs.root
            }

            override fun onFileDeleted(file: VirtualFile) {
                root = vfs.root
            }
        })
    }
}

fun LazyListScope.nodes(
    nodes: List<VirtualFile>,
    isExpanded: (VirtualFile) -> Boolean,
    toggleExpanded: (VirtualFile) -> Unit,
    onClick: (VirtualFile) -> Unit,
    onRightClick: (VirtualFile, Offset) -> Unit,
    depth: Dp,
    expandWidth: Dp
) {
    nodes.forEach {
        node(
            it,
            isExpanded = isExpanded,
            toggleExpanded = toggleExpanded,
            onClick = onClick,
            onRightClick = onRightClick,
            depth = depth,
            expandWidth = expandWidth
        )
    }
}

/**
 * @return the size of the expand button
 */
fun LazyListScope.node(
    file: VirtualFile,
    isExpanded: (VirtualFile) -> Boolean,
    toggleExpanded: (VirtualFile) -> Unit,
    onClick: (VirtualFile) -> Unit,
    onRightClick: (VirtualFile, Offset) -> Unit,
    depth: Dp,
    expandWidth: Dp
) {
    val icon = UIState.Icon.value

    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)

                            when {
                                event.type == PointerEventType.Press && event.buttons.isSecondaryPressed -> {
                                    val offset = event.changes.firstOrNull()?.position ?: Offset.Zero
                                    event.changes.forEach { it.consume() }
                                    onRightClick(file, offset)
                                }

                                event.type == PointerEventType.Press && event.buttons.isPrimaryPressed -> {
                                    event.changes.forEach { it.consume() }
                                    onClick(file)
                                }
                            }
                        }
                    }
                },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(depth))

            if (file.isDirectory) {
                CButton(onClick = {
                    toggleExpanded(file)
                }, icon = if (isExpanded(file)) icon.chevronDown else icon.chevronRight, iconType = IconType.SMALL, withPressedBg = false, withHoverBg = false)
            } else {
                Spacer(Modifier.width(expandWidth))
            }

            CLabel(
                icon = when {
                    file.isDirectory -> icon.folder
                    file.name.endsWith(".s") || file.name.endsWith(".S") -> icon.asmFile
                    else -> icon.file
                },
                iconType = IconType.SMALL,
                text = file.name,
                fontType = FontType.MEDIUM
            )
        }
    }
    if (isExpanded(file)) {
        nodes(
            file.getChildren(),
            isExpanded,
            toggleExpanded,
            onClick,
            onRightClick,
            depth + expandWidth,
            expandWidth = expandWidth
        )
    }
}

