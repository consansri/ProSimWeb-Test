package me.c3.ui.components.controls.buttons

import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.States.save
import me.c3.ui.styled.*
import me.c3.ui.styled.params.FontType
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.SwingUtilities

class Settings : CIconButton(States.icon.get().settings) {
    var lastDialog: CDialog? = null

    init {
        addActionListener {
            if (lastDialog != null) {
                lastDialog?.dispose()
                lastDialog = null
            } else {
                showSettings()
            }
        }
    }

    private fun showSettings() {
        SwingUtilities.invokeLater {
            val dialog = CDialog(this)
            val ws = States.ws.get()
            val arch = States.arch.get()

            // Add Components
            val title = CPanel(primary = true).apply {
                val name = CLabel("${arch.description.name} - Settings", FontType.BASIC)
                val filler = CPanel(primary = true)
                val closeButton = CIconButton(States.icon.get().close).apply {
                    addActionListener {
                        dialog.dispose()
                        lastDialog = null
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

            val settingPane = CPanel(primary = true)
            val gbc = GridBagConstraints()
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            settingPane.layout = GridBagLayout()
            arch.settings.forEach {
                gbc.gridy++
                it.toSwing(arch, settingPane, gbc)
            }

            val contentPane = CPanel(isOverlay = true)
            contentPane.layout = BorderLayout()
            contentPane.add(title, BorderLayout.NORTH)
            contentPane.add(CScrollPane(true, settingPane), BorderLayout.CENTER)

            // Add Content Panel to Dialog Frame
            dialog.layout = BorderLayout()
            dialog.add(contentPane, BorderLayout.CENTER)
            dialog.size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 4)
            dialog.setLocationRelativeTo(null)
            dialog.isVisible = true

            lastDialog = dialog
        }
    }

    private fun SetupSetting<*>.toSwing(arch: Architecture, panel: CPanel, gbc: GridBagConstraints) {
        val name = CLabel(this.name, FontType.BASIC)
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.CENTER
        gbc.gridx = 0
        panel.add(name, gbc)

        val component: Component = when (this) {
            is SetupSetting.Bool -> {
                CSwitch(this.get(), Mode.PRIMARY_NORMAL) {
                    this@toSwing.set(arch, it)
                    this@toSwing.save(arch)
                    Events.archSettingChange.triggerEvent(arch)
                }
            }

            is SetupSetting.Any -> {
                CTextField(this.valueToString(), FontType.CODE).apply {
                    addFocusListener(object : FocusAdapter() {
                        override fun focusLost(e: FocusEvent?) {
                            this@toSwing.loadFromString(arch, this@apply.text)
                            this@toSwing.save(arch)
                            Events.archSettingChange.triggerEvent(arch)
                        }
                    })
                }
            }
        }
        gbc.gridx = 1
        panel.add(component, gbc)
    }


}