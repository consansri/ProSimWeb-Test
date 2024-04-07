package me.c3.ui.components.tree

import emulator.kit.nativeError
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import me.c3.ui.UIManager
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.components.styled.CTree
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class FileTree(uiManager: UIManager, defaultWorkspace: String) : CPanel(uiManager, true) {
    private val projectButton = CTextButton(uiManager, "Project")
    private val title = CPanel(uiManager, false)
    private val content = CScrollPane(uiManager, true)
    private var currentCTree: CTree? = null

    init {
        projectButton.foreground = uiManager.currTheme().textLaF.base
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)

                val fileChooser = JFileChooser()
                fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val result = fileChooser.showOpenDialog(this@FileTree)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val selectedFile = fileChooser.selectedFile
                    setWorkspace(uiManager, selectedFile.absolutePath)
                }
            }
        })

        // Layout
        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        setWorkspace(uiManager, defaultWorkspace)
    }

    fun setWorkspace(uiManager: UIManager, workspace: String) {
        val rootDir = File(workspace)
        val rootNode = DefaultMutableTreeNode(rootDir.name)
        val treeModel = DefaultTreeModel(rootNode)

        buildFileTree(rootDir, rootNode)

        currentCTree = CTree(uiManager, treeModel)

        // Add Listeners
        uiManager.themeManager.addThemeChangeListener {
            currentCTree?.background = it.globalLaF.bgSecondary
        }

        currentCTree?.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    currentCTree?.let { currTree ->
                        val selectedNode = currTree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return@let
                        val uobj = selectedNode.userObject

                        if (uobj is CTree.TreeFile) {
                            uiManager.fileManager.openFile(uobj.file)
                        } else {
                            nativeError("Couldn't open File! ($uobj)")
                        }
                    }
                }
            }
        })

        // Apply Standards
        currentCTree?.background = uiManager.currTheme().globalLaF.bgSecondary
        currentCTree?.setFocusPainted(false)

        currentCTree?.isFocusable = false
        content.setViewportView(currentCTree)
        content.revalidate()
        content.repaint()
    }

    fun buildFileTree(file: File, parentNode: DefaultMutableTreeNode) {
        val files = file.listFiles() ?: return

        println("Found files: ${files.joinToString { it.name }}")

        for (childFile in files) {
            val childNode = DefaultMutableTreeNode(CTree.TreeFile(childFile))
            parentNode.add(childNode)

            if (childFile.isDirectory) {
                buildFileTree(childFile, childNode)
            }
        }
    }

}