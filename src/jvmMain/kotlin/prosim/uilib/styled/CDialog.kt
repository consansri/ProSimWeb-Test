package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.*
import javax.swing.JDialog

class CDialog(parent: Component) : JDialog() {

    init {
        //rootPane = CRootPane(tm, sm)
        layout = BorderLayout()
        isAlwaysOnTop = true
        isUndecorated = true
        rootPane.isOpaque = false
        rootPane.background = Color(0, 0, 0, 0)
        contentPane.background = Color(0, 0, 0, 0)

        defaultCloseOperation = DISPOSE_ON_CLOSE

        glassPane = object : Component() {
            override fun paint(g: Graphics) {
                val g2d = g as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Draw shadow
                val shadowSize = 5
                g2d.color = Color(0, 0, 0, 50)
                g2d.fillRoundRect(shadowSize, shadowSize, width - shadowSize * 2, height - shadowSize * 2, 10, 10)

                // Draw a rounded rectangle for the dialog
                g2d.color = background
                g2d.fillRoundRect(0, 0, width - shadowSize, height - shadowSize, 10, 10)
            }
        }
        glassPane.isVisible

    }

    override fun setVisible(b: Boolean) {
        if (b) {
            pack()
            setLocationRelativeTo(parent)
        }
        super.setVisible(b)
    }

    override fun setBackground(bgColor: Color?) {
        super.setBackground(bgColor)
        glassPane.repaint()
    }

    fun setContent(component: Component) {
        contentPane.removeAll()
        contentPane.add(component)
        pack()
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
                val closeButton = CIconButton(UIStates.icon.get().close).apply {
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
            cPane.layout = BorderLayout()
            cPane.preferredSize = Dimension(300,300)
            val bPane = CPanel(primary = true)
            bPane.layout = BorderLayout()

            val contentPane = CPanel(isOverlay = true)
            contentPane.layout = BorderLayout()
            contentPane.minimumSize = Dimension(300, 300)
            contentPane.add(tPane, BorderLayout.NORTH)
            val scrollPane = CScrollPane(true, cPane)
            scrollPane.minimumSize = Dimension(200,200)
            contentPane.add(scrollPane, BorderLayout.CENTER)
            contentPane.add(bPane, BorderLayout.SOUTH)

            // Add Content Panel to Dialog Frame
            dialog.layout = BorderLayout()
            contentPane.size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 4)
            dialog.add(contentPane, BorderLayout.CENTER)
            //dialog.revalidate()
            dialog.validate()
            dialog.setLocationRelativeTo(null)
            dialog.repaint()

            return Triple(dialog, cPane, bPane)
        }
    }

    override fun getMinimumSize(): Dimension {
        return contentPane.minimumSize
    }
}