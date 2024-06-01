package me.c3.ui.components.tree

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.manager.MainManager
import me.c3.ui.manager.ThemeManager
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CScrollPane
import me.c3.ui.styled.CTextButton
import me.c3.ui.styled.COptionPane
import me.c3.ui.styled.params.FontType
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory

/**
 * Represents a panel containing a file tree component for displaying and navigating project files.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 */
class FileTree() : CPanel( true) {
    private val projectButton = CTextButton( "Project", FontType.TITLE)
    private val title = CPanel( false)
    private val content = CScrollPane( false)

    init {
        attachMouseListener()

        MainManager.addWSChangedListener {
            refreshWSTree()
        }

        refreshWSTree()
        setTreeDefaults()
    }

    /**
     * Attaches a mouse listener to the project button to handle directory selection.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun attachMouseListener() {
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                CoroutineScope(Dispatchers.Default).launch {
                    val file = COptionPane.showDirectoryChooser(this@FileTree, "Workspace").await()
                    file?.let {
                        MainManager.setCurrWS(file.absolutePath)
                    }
                }
            }
        })
    }

    /**
     * Refreshes the workspace tree view based on changes in the current workspace.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun refreshWSTree() {
        content.setViewportView(MainManager.currWS().tree)
        content.revalidate()
        content.repaint()
    }

    /**
     * Sets default properties and layout for the file tree panel.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun setTreeDefaults() {
        projectButton.foreground = ThemeManager.curr.textLaF.base
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        this.border = BorderFactory.createEmptyBorder()
    }
}