package prosim.ui.impl

import emulator.kit.common.FileBuilder
import emulator.kit.toAsmFile
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.workspace.WSBehaviour
import prosim.uilib.workspace.WSFileAction
import prosim.uilib.workspace.Workspace

enum class WSBehaviourImpl : WSBehaviour {
    ASM() {
        override val actions: List<WSFileAction> = listOf(
            object : WSFileAction {
                override val name: String = "Build"

                override fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean = selectedFiles.filter { it.file.name.endsWith(".S") || it.file.name.endsWith(".s") }.isNotEmpty()

                override suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>) {
                    files.forEach {
                        val result = States.arch.get().compile(it.file.toAsmFile(it.file, ws.rootDir), ws.getImportableFiles(it.file), true)
                        Events.compile.triggerEvent(result)
                    }
                }
            },
            object : WSFileAction {
                override val name: String = "Generate MIF"

                override fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean = selectedFiles.filter { it.file.name.endsWith(".S") || it.file.name.endsWith(".s") }.isNotEmpty()

                override suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>) {
                    ws.showExportOverlay(FileBuilder.ExportFormat.MIF, files)
                }
            },
            object : WSFileAction {
                override val name: String = "Generate HexDump"
                override fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean = selectedFiles.filter { it.file.name.endsWith(".S") || it.file.name.endsWith(".s") }.isNotEmpty()
                override suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>) {
                    ws.showExportOverlay(FileBuilder.ExportFormat.HEXDUMP, files)
                }

            },
            object : WSFileAction {
                override val name: String = "Generate VHDL"
                override fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean = selectedFiles.filter { it.file.name.endsWith(".S") || it.file.name.endsWith(".s") }.isNotEmpty()
                override suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>) {
                    ws.showExportOverlay(FileBuilder.ExportFormat.VHDL, files)
                }

            },
            object : WSFileAction {
                override val name: String = "Generate Transcript"
                override fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean = selectedFiles.filter { it.file.name.endsWith(".S") || it.file.name.endsWith(".s") }.isNotEmpty()
                override suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>) {
                    ws.showExportOverlay(FileBuilder.ExportFormat.TRANSCRIPT, files)
                }

            },
        )
    },


}