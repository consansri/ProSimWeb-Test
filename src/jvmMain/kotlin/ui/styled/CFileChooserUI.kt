package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Font
import javax.swing.*
import javax.swing.plaf.basic.BasicFileChooserUI

class CFileChooserUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, b: CFileChooser) : BasicFileChooserUI(b) {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        themeManager.addThemeChangeListener {
            setDefaults()
        }

        scaleManager.addScaleChangeEvent {
            setDefaults()
        }

        setDefaults()
        customizeComponents()
    }

    private fun setDefaults() {

    }

    private fun customizeComponents() {
        // Accessing the directory combo box
        val directoryComboBox = findComponentByName(fileChooser, "DirectoryComboBox")
        if (directoryComboBox is JComboBox<*>) {
            customizeComboBox(directoryComboBox)
        }

        // Accessing the file name text field
        val fileNameTextField = findComponentByName(fileChooser, "fileNameTextField")
        if (fileNameTextField is JTextField) {
            customizeTextField(fileNameTextField)
        }

        // Accessing the filter combo box
        val filterComboBox = findComponentByName(fileChooser, "filterComboBox")
        if (filterComboBox is JComboBox<*>) {
            customizeComboBox(filterComboBox)
        }
    }

    private fun findComponentByName(parent: Container, name: String): Component? {
        val components = parent.components
        for (component in components) {
            if (name == component.name) {
                return component
            } else if (component is Container) {
                val result = findComponentByName(component, name)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun customizeComboBox(comboBox: JComboBox<*>) {
        comboBox.font = Font("Arial", Font.PLAIN, 14)
        comboBox.background = Color.WHITE
        comboBox.foreground = Color.BLACK
        comboBox.border = BorderFactory.createLineBorder(Color.GRAY)
    }

    private fun customizeTextField(textField: JTextField) {
        textField.font = Font("Arial", Font.PLAIN, 14)
        textField.background = Color.WHITE
        textField.foreground = Color.BLACK
        textField.border = BorderFactory.createLineBorder(Color.GRAY)
    }


}