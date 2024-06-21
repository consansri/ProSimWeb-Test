package prosim.ui.components.controls.buttons

import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import prosim.ui.Events
import prosim.ui.States
import prosim.ui.States.save
import prosim.uilib.UIStates
import prosim.uilib.styled.*
import prosim.uilib.styled.params.FontType
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.SwingUtilities

class Settings : CIconButton(UIStates.icon.get().settings) {
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
                    SwingUtilities.invokeLater {
                        this@toSwing.set(arch, it)
                        this@toSwing.save(arch)
                        Events.archSettingChange.triggerEvent(arch)
                    }
                }
            }

            is SetupSetting.Enumeration<*> -> {
                CComboBox(this.enumValues.toTypedArray(), FontType.BASIC).apply {
                    this.selectedItem = this@toSwing.get()
                    addItemListener {e ->
                        SwingUtilities.invokeLater {
                            this@toSwing.loadFromString(arch, (e.item as Enum<*>).name)
                            this@toSwing.save(arch)
                            Events.archSettingChange.triggerEvent(arch)
                        }
                    }
                }
            }

            is SetupSetting.Any -> {
                CTextField(this.valueToString(), FontType.CODE).apply {
                    addFocusListener(object : FocusAdapter() {
                        override fun focusLost(e: FocusEvent?) {
                            SwingUtilities.invokeLater {
                                this@toSwing.loadFromString(arch, this@apply.text)
                                this@toSwing.save(arch)
                                Events.archSettingChange.triggerEvent(arch)
                            }
                        }
                    })
                }
            }
        }
        gbc.gridx = 1
        panel.add(component, gbc)
    }
}