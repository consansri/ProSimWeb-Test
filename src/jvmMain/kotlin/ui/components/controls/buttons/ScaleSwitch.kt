package ui.components.controls.buttons

import emulator.Link
import me.c3.ui.MainManager
import me.c3.ui.spacing.core.Scaling
import me.c3.ui.styled.CComboBox
import me.c3.ui.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ScaleSwitch(mainManager: MainManager) : CComboBox<Scaling>(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, mainManager.scaleManager.scalings.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ScaleSelectorListener(mainManager))
    }

    class ScaleSelectorListener(private val mainManager: MainManager): ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Scaling ?: return
            mainManager.scaleManager.curr = selectedMode
        }
    }

}