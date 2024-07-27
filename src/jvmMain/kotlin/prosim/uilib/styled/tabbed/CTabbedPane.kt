package prosim.uilib.styled.tabbed

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.params.IconSize
import java.awt.*
import java.awt.event.MouseListener
import javax.swing.JTabbedPane
import javax.swing.border.Border

open class CTabbedPane(val primary: Boolean = true, val fontType: FontType = FontType.BASIC) : JTabbedPane() {

    val dndHandler = DnDHandler(this) // Reorderable Tabs functionality.

    init {
        this.setUI(CTabbedPaneUI())
    }

    fun addClosableTab(tab: TabProvider, mouseListener: MouseListener? = null) {
        val tabComponent = CPanel(primary).apply {
            layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        }

        val closeButton = CIconButton(UIStates.icon.get().close, IconSize.SECONDARY_SMALL).apply {
            addActionListener {
                val index = indexOfTabComponent(tabComponent)
                if (index != -1) {
                    removeTabAt(index)
                }
            }
        }

        val title = object : CLabel(tab.title, svgIcon = tab.icon, iconSize = IconSize.PRIMARY_SMALL) {
            override fun getText(): String {
                return tab.title
            }
        }

        tabComponent.add(title)
        tabComponent.add(closeButton)
        if (mouseListener != null) tabComponent.addMouseListener(mouseListener)

        addTab(tab.title, tab.component)
        setTabComponentAt(tabCount - 1, tabComponent)
        setToolTipTextAt(tabCount - 1, tab.tooltip)
    }

    fun moveTab(fromIndex: Int, toIndex: Int) {
        val component = getComponentAt(fromIndex)
        val title = getTitleAt(fromIndex)
        removeTabAt(fromIndex)
        insertTab(title, null, component, null, toIndex)
        selectedIndex = toIndex
    }

    fun copyTabs(source: CTabbedPane) {
        for (i in 0 until source.tabCount) {
            addTab(source.getTitleAt(i), source.getComponentAt(i))
        }
    }

    override fun paintComponent(g: Graphics) {
        // Custom background for the empty content
        g.color = background
        g.fillRect(0, 0, width, height)

        super.paintComponent(g)
    }

    override fun getBackground(): Color {
        return if (primary) UIStates.theme.get().COLOR_BG_0 else UIStates.theme.get().COLOR_BG_1
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getBorder(): Border {
        return BorderMode.NONE.getBorder()
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    interface TabProvider {
        val title: String
        val icon: FlatSVGIcon?
        val component: Component
        val tooltip: String?
    }


}