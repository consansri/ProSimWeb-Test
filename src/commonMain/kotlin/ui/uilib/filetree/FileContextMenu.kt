package ui.uilib.filetree

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.menu.Menu
import ui.uilib.menu.MenuItem

@Composable
fun FileContextMenu(
    file: VirtualFile,
    position: Offset,
    onDismiss: () -> Unit,
    onRename: (VirtualFile) -> Unit,
    onDelete: (VirtualFile) -> Unit,
    onCreate: (VirtualFile, isDirectory: Boolean) -> Unit
) {

    Menu(
        position,
        onDismiss = onDismiss,
    ){
        MenuItem(UIState.Icon.value.edit, "Rename"){
            onDismiss()
            onRename(file)
        }

        MenuItem(UIState.Icon.value.deleteBlack, "Delete"){
            onDismiss()
            onDelete(file)
        }

        if(file.isDirectory){
            MenuItem(UIState.Icon.value.file, "Create New File"){
                onDismiss()
                onCreate(file, false)
            }
            MenuItem(UIState.Icon.value.folder, "Create New Folder"){
                nativeLog("Create New Folder")
                onDismiss()
                onCreate(file, true)
            }
        }
    }

}