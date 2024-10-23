package ui.uilib.filetree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.project.Project
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
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
    val ioScope = rememberCoroutineScope()

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

        if (file.isFile) {
            MenuItem(UIState.Icon.value.file, "Export") {
                onDismiss()
                ioScope.launch {
                    val baseName = file.name.substringBeforeLast('.')
                    val ext = file.name.substringAfterLast('.')
                    FileKit.saveFile(file.getContent(), baseName, ext)
                }
            }
        }

        if (file.isDirectory) {
            MenuItem(UIState.Icon.value.file, "Import") {
                onDismiss()
                ioScope.launch {
                    val fileData = FileKit.pickFile(PickerType.File(), PickerMode.Single,"Import File")
                    if (fileData != null) {
                        val path = file.path + fileData.name
                        val newFile = project.fileSystem.createFile(path)
                        newFile.setContent(fileData.readBytes())
                        nativeLog("Imported File $path!")
                    }else{
                        nativeWarn("No File Selected!")
                    }
                }
            }
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