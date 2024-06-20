package me.c3.uilib.styled

import me.c3.uilib.UIManager
import me.c3.uilib.styled.params.FontType
import java.awt.*
import javax.swing.JDialog

class CDialog(parent: Component) : JDialog() {

    init {
        //rootPane = CRootPane(tm, sm)
        isAlwaysOnTop = true
        isUndecorated = true
        rootPane.isOpaque = false
        rootPane.background = Color(0, 0, 0, 0)
        contentPane.background = Color(0, 0, 0, 0)
    }

    companion object {

        /**
         * @return the created [CDialog] which needs to be set visible after adding all components to the
         * - content [CPanel] and the
         * - submit [CPanel]
         *
         */
        fun createWithTitle(title: String, parent: Component, onClose: (CDialog) -> Unit = {}): Triple<CDialog, CPanel, CPanel> {
            val dialog = CDialog(parent)

            // Add Components
            val tPane = CPanel(primary = true).apply {
                val name = CLabel(title, FontType.BASIC)
                val filler = CPanel(primary = true)
                val closeButton = CIconButton(UIManager.icon.get().close).apply {
                    addActionListener {
                        dialog.dispose()
                        onClose(dialog)
                    }
                }

                layout = GridBagLayout()
                val gbc = GridBagConstraints()
                add(name, gbc)
                gbc.gridx = 2
                add(closeButton, gbc)
                gbc.gridx = 1
                gbc.weightx = 1.0
                gbc.fill = GridBagConstraints.HORIZONTAL
                add(filler, gbc)
            }

            val cPane = CPanel(primary = true)
            val bPane = CPanel(primary = true)

            val contentPane = CPanel(isOverlay = true)
            contentPane.layout = BorderLayout()
            contentPane.add(tPane, BorderLayout.NORTH)
            contentPane.add(CScrollPane(true, cPane), BorderLayout.CENTER)
            contentPane.add(bPane, BorderLayout.SOUTH)

            // Add Content Panel to Dialog Frame
            dialog.layout = BorderLayout()
            dialog.add(contentPane, BorderLayout.CENTER)
            dialog.size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 4)
            dialog.setLocationRelativeTo(null)

            return Triple(dialog, cPane, bPane)
        }
    }

}