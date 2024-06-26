package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JSpinner.DefaultEditor
import javax.swing.plaf.basic.BasicSpinnerUI

class CSpinnerUI : BasicSpinnerUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val spinner = c as? CSpinner
        if (spinner == null) {
            return
        }

        spinner.removeAll()
        setDefaults(spinner)

        spinner.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        spinner.maybeAdd(createEditor(), gbc)
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.VERTICAL
        gbc.gridx = 1
        spinner.maybeAdd(createPreviousButton(), gbc)
        gbc.gridx = 2
        spinner.maybeAdd(createNextButton(), gbc)

        UIStates.theme.addEvent(WeakReference(spinner)) {
            setDefaults(spinner)
        }

        UIStates.scale.addEvent(WeakReference(spinner)) {
            setDefaults(spinner)
        }
    }

    fun setDefaults(spinner: CSpinner) {
        spinner.isOpaque = false
        spinner.background = UIStates.theme.get().globalLaF.bgOverlay
        spinner.foreground = UIStates.theme.get().textLaF.base
        spinner.border = spinner.borderMode.getBorder()

        val editor = spinner.editor as DefaultEditor
        editor.border = BorderFactory.createEmptyBorder()
        editor.background = spinner.background
        editor.foreground = spinner.foreground
        editor.font = spinner.fontType.getFont()

        editor.textField.border = BorderFactory.createEmptyBorder()
        editor.textField.background = spinner.background
        editor.textField.foreground = spinner.foreground
        editor.textField.caretColor = spinner.foreground
        editor.textField.font = spinner.fontType.getFont()

        spinner.revalidate()
        spinner.repaint()
    }

    override fun createNextButton(): Component {
        val button = CIconButton(UIStates.icon.get().increase, CIconButton.Mode.PRIMARY_SMALL, hasHoverEffect = false)
        button.addActionListener {
            try {
                spinner.value = spinner.nextValue
                spinner.commitEdit()
            } catch (_: Exception) {
            }
        }
        return button
    }

    override fun createPreviousButton(): Component {
        val button = CIconButton(UIStates.icon.get().decrease, CIconButton.Mode.PRIMARY_SMALL, hasHoverEffect = false)
        button.addActionListener {
            try {
                spinner.value = spinner.previousValue
                spinner.commitEdit()
            } catch (_: Exception) {
            }
        }
        return button
    }

    private fun CSpinner.maybeAdd(c: Component?, gbc: GridBagConstraints) {
        if (c != null) {
            this.add(c, gbc)
        }
    }

}