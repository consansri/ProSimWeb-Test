package me.c3.ui.components.tree

import me.c3.ui.UIManager
import me.c3.ui.components.styled.Panel
import me.c3.ui.components.styled.ScrollPane
import me.c3.ui.components.styled.TextButton
import me.c3.ui.components.styled.Tree
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JFileChooser
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class FileTree(uiManager: UIManager) : Panel(uiManager, true) {
    private val projectButton = TextButton(uiManager, "Project")
    private val title = Panel(uiManager, false)
    private val content = ScrollPane(uiManager, true)
    private var currentTree: Tree? = null

    init {
        projectButton.foreground = uiManager.currTheme().textStyle.base
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

        content.setFocusPainted(false)

        // Layout
        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)
    }

    fun setWorkspace(uiManager: UIManager, workspace: String) {
        val rootDir = File(workspace)
        val rootNode = DefaultMutableTreeNode(rootDir.name)
        val treeModel = DefaultTreeModel(rootNode)

        buildFileTree(rootDir, rootNode)

        currentTree = Tree(treeModel)

        // Add Listeners
        uiManager.themeManager.addThemeChangeListener {
            currentTree?.background = it.globalStyle.bgSecondary
        }

        // Apply Standards
        currentTree?.background = uiManager.currTheme().globalStyle.bgSecondary
        currentTree?.setFocusPainted(false)

        currentTree?.isFocusable = false
        content.setViewportView(currentTree)
        content.revalidate()
        content.repaint()
    }

    fun buildFileTree(file: File, parentNode: DefaultMutableTreeNode) {
        val files = file.listFiles() ?: return

        println("Found files: ${files.joinToString { it.name }}")

        for (childFile in files) {
            val childNode = DefaultMutableTreeNode(childFile.name)
            parentNode.add(childNode)

            if (childFile.isDirectory) {
                buildFileTree(childFile, childNode)
            }
        }
    }
}