package prosim.ui.components.tree

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.ui.States
import prosim.ui.States.setFromPath
import prosim.uilib.UIStates
import prosim.uilib.state.StateListener
import prosim.uilib.state.WSEditor
import prosim.uilib.state.WSLogger
import prosim.uilib.styled.*
import prosim.uilib.styled.params.FontType
import prosim.uilib.workspace.Workspace
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.SwingUtilities

/**
 * Represents a panel containing a file tree component for displaying and navigating project files.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 */
class FileTree(val editor: WSEditor?, val logger: WSLogger?) : CPanel(true), StateListener<Workspace?> {
    private val projectButton = CTextButton(Workspace::class.simpleName.toString(), FontType.TITLE)
    private val emptyWorkspace = CTextField(FontType.CODE).apply {
        isEditable = false
        text = "No ${Workspace::class.simpleName.toString()} selected!"
    }
    private val title = CPanel(false)
    private val content = CScrollPane()

    init {
        attachMouseListener()

        States.ws.addEvent(this)

        refreshWSTree()
        setTreeDefaults()
    }

    /**
     * Attaches a mouse listener to the project button to handle directory selection.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun attachMouseListener() {
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (States.ws.get() == null) {
                    CoroutineScope(Dispatchers.Default).launch {
                        val file = COptionPane.showDirectoryChooser(this@FileTree, Workspace::class.simpleName.toString()).await()
                        file?.let {
                            States.ws.setFromPath(file.absolutePath, editor, logger)
                        }
                    }
                } else {
                    SwingUtilities.invokeLater {
                        val menu = CPopupMenu()
                        val openProject = CMenuItem("Open ${Workspace::class.simpleName.toString()}")
                        val closeProject = CMenuItem("Close ${Workspace::class.simpleName.toString()}")

                        openProject.addActionListener {
                            CoroutineScope(Dispatchers.Default).launch {
                                val file = COptionPane.showDirectoryChooser(this@FileTree, Workspace::class.simpleName.toString()).await()
                                file?.let {
                                    States.ws.setFromPath(file.absolutePath, editor, logger)
                                }
                            }
                        }

                        closeProject.addActionListener {
                            States.ws.set(null)
                        }

                        menu.add(openProject)
                        menu.add(closeProject)

                        menu.show(this@FileTree, e.x, e.y)
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
        val ws = States.ws.get()
        if (ws != null) {
            content.setViewportView(ws.tree)
        } else {
            content.setViewportView(emptyWorkspace)
        }
        content.revalidate()
        content.repaint()
    }

    /**
     * Sets default properties and layout for the file tree panel.
     * @param mainManager The main manager responsible for coordinating UI components and actions.
     */
    private fun setTreeDefaults() {
        projectButton.foreground = UIStates.theme.get().COLOR_FG_0
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        this.border = BorderFactory.createEmptyBorder()
    }

    override suspend fun onStateChange(newVal: Workspace?) {
        SwingUtilities.invokeLater {
            refreshWSTree()
        }
    }
}