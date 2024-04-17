package me.c3.ui.components.tree

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.styled.COptionPane
import me.c3.ui.styled.params.FontType
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory

class FileTree(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, true) {
    private val projectButton = CTextButton(mainManager.themeManager, mainManager.scaleManager, "Project", FontType.TITLE)
    private val title = CPanel(mainManager.themeManager, mainManager.scaleManager, false)
    private val content = CScrollPane(mainManager.themeManager, mainManager.scaleManager, false)

    init {
        attachMouseListener(mainManager)

        mainManager.addWSChangedListener {
            refreshWSTree(mainManager)
        }

        refreshWSTree(mainManager)
        setTreeDefaults(mainManager)
    }

    private fun attachMouseListener(mainManager: MainManager) {
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    val file = COptionPane.showDirectoryChooser(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, this@FileTree, "Workspace").await()
                    file?.let {
                        mainManager.setCurrWS(file.absolutePath)
                    }
                }
            }
        })
    }

    private fun refreshWSTree(mainManager: MainManager) {
        content.setViewportView(mainManager.currWS().tree)
        content.revalidate()
        content.repaint()
    }

    private fun setTreeDefaults(mainManager: MainManager) {
        projectButton.foreground = mainManager.currTheme().textLaF.base
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        this.border = BorderFactory.createEmptyBorder()
    }

}