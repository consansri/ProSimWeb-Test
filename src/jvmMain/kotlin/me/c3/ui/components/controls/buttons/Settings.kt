package me.c3.ui.components.controls.buttons

import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.ui.States.save
import me.c3.ui.styled.*
import me.c3.ui.styled.params.FontType
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
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
            val arch = States.arch.get()

            val (dialog, content) = CDialog.createWithTitle("${arch.description.name} - Settings", this) {
                lastDialog = null
            }

            val gbc = GridBagConstraints()
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            content.layout = GridBagLayout()
            arch.settings.forEach {
                gbc.gridy++
                it.toSwing(arch, content, gbc)
            }

            lastDialog = dialog
            dialog.isVisible = true
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

            is SetupSetting.Enumeration<*> -> {
                CComboBox(this.enumValues.toTypedArray(), FontType.BASIC).apply {
                    this.selectedItem = this@toSwing.get()
                    addItemListener {e ->
                        this@toSwing.loadFromString(arch, (e.item as Enum<*>).name)
                        this@toSwing.save(arch)
                        Events.archSettingChange.triggerEvent(arch)
                    }
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