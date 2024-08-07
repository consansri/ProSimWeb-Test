package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import emulator.kit.nativeLog
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class CToolView(val title: String, val viewProvider: () -> List<View>) : CPanel() {

    private val topPane = TopPane()
    val functionPane = FunctionPane()

    init {
        layout = BorderLayout()

        add(topPane, BorderLayout.NORTH)
        add(functionPane, BorderLayout.WEST)
    }

    fun update() {
        topPane.updateTabs()
    }

    private fun removeCenterComponent() {
        val component = (layout as? BorderLayout)?.getLayoutComponent(BorderLayout.CENTER)
        if (component != null) remove(component)
    }

    inner class TopPane() : CPanel(borderMode = BorderMode.SOUTH) {
        private val titlePanel = CLabel(title, FontType.TITLE)

        init {
            layout = GridBagLayout()

            updateTabs()
        }

        fun updateTabs() {
            removeAll()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(titlePanel, gbc)

            for (view in viewProvider()) {
                gbc.gridx += 1
                add(Tab(view), gbc)
            }
        }

        private inner class Tab(val view: View) : CLabel(view.viewname, fontType = FontType.TITLE, svgIcon = view.icon, borderMode = BorderMode.MEDIUM) {
            init {
                isFocusable = true
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        select()
                        nativeLog("Switching Content to ${view.viewname}")
                    }
                })
            }

            fun select() {
                removeCenterComponent()
                this@CToolView.add(view.content, BorderLayout.CENTER)
                this@TopPane.components.forEach {
                    if (it is Tab && it != this) {
                        it.unselect()
                    }
                }
                this@CToolView.repaint()
                borderMode = BorderMode.SOUTH
            }

            fun unselect() {
                borderMode = BorderMode.MEDIUM
            }
        }
    }

    open inner class FunctionPane() : CPanel(borderMode = BorderMode.EAST) {

    }

    interface View {
        val icon: FlatSVGIcon?
        val viewname: String
        val content: JComponent
    }
}