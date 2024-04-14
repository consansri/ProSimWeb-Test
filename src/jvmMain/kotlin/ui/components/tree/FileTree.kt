package me.c3.ui.components.tree

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.styled.COptionPane
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JFileChooser

class FileTree(uiManager: UIManager) : CPanel(uiManager.themeManager, uiManager.scaleManager, true) {
    private val projectButton = CTextButton(uiManager.themeManager, uiManager.scaleManager, "Project")
    private val title = CPanel(uiManager.themeManager, uiManager.scaleManager, false)
    private val content = CScrollPane(uiManager.themeManager, uiManager.scaleManager, false)

    init {
        attachMouseListener(uiManager)

        uiManager.addWSChangedListener {
            refreshWSTree(uiManager)
        }

        refreshWSTree(uiManager)
        setTreeDefaults(uiManager)
    }

    private fun attachMouseListener(uiManager: UIManager) {
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    val file = COptionPane.showDirectoryChooser(uiManager.themeManager, uiManager.scaleManager, uiManager.icons, this@FileTree, "Workspace").await()
                    file?.let {
                        uiManager.setCurrWS(file.absolutePath)
                    }
                }
            }
        })
    }

    private fun refreshWSTree(uiManager: UIManager) {
        content.setViewportView(uiManager.currWS().tree)
        content.revalidate()
        content.repaint()
    }

    private fun setTreeDefaults(uiManager: UIManager) {
        projectButton.foreground = uiManager.currTheme().textLaF.base
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        this.border = BorderFactory.createEmptyBorder()
    }

}