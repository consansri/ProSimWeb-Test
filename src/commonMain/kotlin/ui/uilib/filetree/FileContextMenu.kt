package ui.uilib.filetree

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.project.Project
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.menu.Menu
import ui.uilib.menu.MenuItem

@Composable
fun FileContextMenu(
    file: VirtualFile,
    project: Project,
    position: Offset,
    onDismiss: () -> Unit,
    onRename: (VirtualFile) -> Unit,
    onDelete: (VirtualFile) -> Unit,
    onCreate: (VirtualFile, isDirectory: Boolean) -> Unit
) {
    Menu(
        position,
        onDismiss = onDismiss,
    ) {
        MenuItem(UIState.Icon.value.edit, "Rename") {
            onDismiss()
            onRename(file)
        }

        MenuItem(UIState.Icon.value.deleteBlack, "Delete") {
            onDismiss()
            onDelete(file)
        }

        val lang = project.getLang(file)
        lang?.runConfigurations?.forEach {
            if (it is RunConfiguration.FileRun<LanguageService>) {
                MenuItem(UIState.Icon.value.build, it.name) {
                    onDismiss()
                    it.run(file, lang, project.fileSystem)
                }
            }
        }

        if (file.isDirectory) {
            MenuItem(UIState.Icon.value.file, "Create New File") {
                onDismiss()
                onCreate(file, false)
            }
            MenuItem(UIState.Icon.value.folder, "Create New Folder") {
                nativeLog("Create New Folder")
                onDismiss()
                onCreate(file, true)
            }
        }
    }

}