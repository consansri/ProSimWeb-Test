package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JTable
import javax.swing.SwingUtilities

open class CTable(uiManager: UIManager) : JTable(), UIAdapter {
    init {
        this.setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CTableUI(uiManager))

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {

    }


}