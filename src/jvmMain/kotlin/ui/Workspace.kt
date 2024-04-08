package me.c3.ui

import emulator.kit.assembly.Compiler
import emulator.kit.nativeError
import emulator.kit.toCompilerFile
import io.nacular.doodle.controls.form.files
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.styled.CTree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class Workspace(private val path: String, codeEditor: CodeEditor, uiManager: UIManager) {
    private val rootDir = File(path)
    private val rootNode = DefaultMutableTreeNode(TreeFile(rootDir))
    private val treeModel = DefaultTreeModel(rootNode)
    val tree: CTree

    init {
        val treeModel = DefaultTreeModel(rootNode)

        buildFileTree(rootDir, rootNode)

        tree = CTree(uiManager, treeModel)

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    tree.let { currTree ->
                        val selectedNode = currTree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return@let
                        val uobj = selectedNode.userObject

                        if (uobj is TreeFile && uobj.file.isFile) {
                            codeEditor.fileManager.openFile(uobj.file)
                        } else {
                            nativeError("Couldn't open File! ($uobj)")
                        }
                    }
                }
            }
        })

        setTreeLook(uiManager)
    }

    fun getAllFiles(): List<File> {
        return getAllFiles(rootDir)
    }

    fun getCompilerFiles(exclude: File): List<Compiler.CompilerFile> = getAllFiles(rootDir).filter { it != exclude && it.isFile && it.name.endsWith(".s") }.map { it.toCompilerFile() }

    private fun getAllFiles(directory: File): List<File> {
        val files = mutableListOf<File>()

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                files.addAll(getAllFiles(file))
            } else {
                files.add(file)
            }
        }

        return files
    }

    private fun setTreeLook(uiManager: UIManager) {
        uiManager.themeManager.addThemeChangeListener {
            tree.background = it.globalLaF.bgSecondary
        }
        tree.background = uiManager.currTheme().globalLaF.bgSecondary
        tree.setFocusPainted(false)
        tree.isFocusable = false
    }

    private fun buildFileTree(file: File, parentNode: DefaultMutableTreeNode) {
        val files = file.listFiles() ?: return

        for (childFile in files) {
            val childNode = DefaultMutableTreeNode(TreeFile(childFile))
            parentNode.add(childNode)

            if (childFile.isDirectory) {
                buildFileTree(childFile, childNode)
            }
        }
    }

    data class TreeFile(val file: File) {
        override fun toString(): String {
            return file.name
        }
    }


}

