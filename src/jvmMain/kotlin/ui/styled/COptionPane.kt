package me.c3.ui.styled

import kotlinx.coroutines.*
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JOptionPane

class COptionPane(themeManager: ThemeManager, scaleManager: ScaleManager) : JOptionPane() {

    companion object {
        fun showInputDialog(themeManager: ThemeManager, scaleManager: ScaleManager, parent: Component, message: String): Deferred<String> {
            val resultDeferred = CompletableDeferred<String>()

            val cDialog = CDialog(themeManager, scaleManager, parent)
            val cPanel = CPanel(themeManager, scaleManager, primary = false, isOverlay = true, roundCorners = true)
            val cLabel = CLabel(themeManager, scaleManager, message)
            val cTextArea = CTextField(themeManager, scaleManager, mode = CTextFieldUI.Type.TEXT)

            cTextArea.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) {
                        resultDeferred.complete(cTextArea.text)
                        cDialog.dispose()
                    }
                }
            })

            cDialog.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    resultDeferred.complete(cTextArea.text)
                    cDialog.dispose()
                }
            })

            cDialog.layout = BorderLayout()

            cPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL

            cPanel.add(cLabel, gbc)
            gbc.gridy = 1
            gbc.fill = GridBagConstraints.HORIZONTAL

            cPanel.add(cTextArea, gbc)

            cDialog.add(cPanel, BorderLayout.CENTER)
            cDialog.pack()
            cDialog.setLocationRelativeTo(null)
            cDialog.isVisible = true

            cTextArea.requestFocus()

            return resultDeferred
        }
    }

    init {
        this.setUI(COptionPaneUI(themeManager, scaleManager))
    }


}