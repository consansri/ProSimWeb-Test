package me.c3.ui.components.processor

import emulator.kit.common.RegContainer
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTabbedPane
import me.c3.ui.styled.CTable
import java.util.Observable
import javax.swing.Box
import javax.swing.JTable
import javax.swing.table.JTableHeader
import kotlin.properties.Delegates

class RegisterView(private val uiManager: UIManager) : CPanel(uiManager, primary = true, BorderMode.SOUTH) {
    var registerPaneCount = 2
        set(value) {
            field = value
            createPanels()
        }

    init {

    }

    private fun createPanels() {
        removeAll()
        repeat(registerPaneCount) {
            addRegTable()
            if (it < registerPaneCount - 1) {
                add(Box.createHorizontalStrut(uiManager.currScale().borderScale.thickness))
            }
        }
    }

    private fun addRegTable() {
        val regTable = RegisterTable()
        add(regTable)
    }

    inner class RegisterTable : CTabbedPane(uiManager, primary = false) {

        init {
            addArchListeners()
        }

        private fun addArchListeners() {
            uiManager.archManager.addArchChangeListener {
                initializeRegFiles()
            }
        }

        private fun initializeRegFiles() {
            removeAll()
            uiManager.currArch().getAllRegFiles().forEach {
                addTab(it.name, RegTable(it))
            }
        }

        inner class RegTable(val registerFile: RegContainer.RegisterFile) : CTable(uiManager) {
            init {
                val observable by Delegates.observable(registerFile){property, oldValue, newValue ->

                }

            }

            fun updateValues() {

            }
        }

    }


}