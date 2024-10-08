package prosim.ui.components.docs


import emulator.kit.Architecture
import emulator.kit.common.Docs
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CAdvancedTabPane
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CTextArea
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.SwingConstants

/**
 * Represents a panel for displaying documentation.
 * @property mainManager The main manager instance.
 */
class InfoView() : CPanel(primary = false), StateListener<Architecture> {

    // Tabbed pane for displaying documentation
    private val docTabs = CAdvancedTabPane(primary = false, tabsAreCloseable = false).apply {
        contentPane.verticalScrollBar.unitIncrement = UIStates.scale.get().SIZE_CONTROL_MEDIUM
    }

    init {
        attachComponents()
        attachListeners()
    }

    /**
     * Updates the documentation based on the current architecture.
     * @param mainManager The main manager instance.
     */
    private fun updateDocs() {
        docTabs.removeAllTabs()
        val docs = States.arch.get().description.docs
        docs.files.filterIsInstance<Docs.DocFile.DefinedFile>().forEach {
            docTabs.addTab(CLabel(it.title, FontType.BASIC), CDocFile(it))
        }
    }

    /**
     * Attaches listeners for architecture change events.
     * @param mainManager The main manager instance.
     */
    private fun attachListeners() {
        States.arch.addEvent(this)

        updateDocs()
    }

    override suspend fun onStateChange(newVal: Architecture) {
        updateDocs()
    }

    /**
     * Attaches components to the panel.
     */
    private fun attachComponents() {
        layout = BorderLayout()
        add(docTabs, BorderLayout.CENTER)
    }

    /**
     * Represents a panel for displaying a documentation file.
     * @property docFile The documentation file.
     */
    inner class CDocFile(private val docFile: Docs.DocFile.DefinedFile) : CPanel(primary = false, BorderMode.THICKNESS) {

        val titlePane = CLabel(docFile.title, FontType.TITLE).apply {
            horizontalAlignment = SwingConstants.CENTER
        }

        val gbc = GridBagConstraints()

        init {
            layout = GridBagLayout()

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weighty = 0.0
            gbc.weightx = 1.0
            gbc.insets = UIStates.scale.get().INSETS_MEDIUM
            gbc.fill = GridBagConstraints.HORIZONTAL

            add(titlePane, gbc)
            gbc.gridy++

            appendElements()
        }

        /**
         * Appends elements to the documentation file panel.
         */
        private fun appendElements() {
            docFile.chapters.forEach {
                it.add(this, gbc)
                gbc.gridy++
            }
        }

        /**
         * Adds a component to the documentation file panel.
         * @param component The component to add.
         * @param gbc The GridBagConstraints for layout.
         */
        private fun Docs.DocComponent.add(component: JComponent, gbc: GridBagConstraints) {
            when (this) {
                is Docs.DocComponent.Chapter -> {
                    val panel = CPanel(false, BorderMode.NORTH, roundCorners = true)
                    panel.layout = GridBagLayout()
                    val subGbc = GridBagConstraints()
                    subGbc.gridx = 0
                    subGbc.gridy = 0
                    subGbc.insets = UIStates.scale.get().INSETS_MEDIUM
                    subGbc.weightx = 0.0
                    subGbc.weighty = 1.0
                    subGbc.fill = GridBagConstraints.CENTER

                    val title = CLabel(this.chapterTitle, FontType.TITLE).apply { horizontalAlignment = SwingConstants.CENTER }
                    panel.add(title, subGbc)

                    for (content in this.chapterContent) {
                        subGbc.gridy++
                        content.add(panel, subGbc)
                    }

                    component.add(panel, gbc)
                }

                is Docs.DocComponent.Code -> {
                    val panel = CPanel(primary = true, roundCorners = true, borderMode = BorderMode.THICKNESS)
                    panel.layout = BorderLayout()
                    val area = CTextArea(FontType.CODE, true, BorderMode.THICKNESS).apply {
                        text = this@add.content
                        isEditable = false
                    }
                    panel.add(area, BorderLayout.CENTER)
                    component.add(panel, gbc)
                }

                is Docs.DocComponent.Section -> {
                    val panel = CPanel(false, BorderMode.NONE, roundCorners = true)
                    panel.layout = GridBagLayout()
                    val subGbc = GridBagConstraints()
                    subGbc.gridx = 0
                    subGbc.gridy = 0
                    subGbc.insets = UIStates.scale.get().INSETS_MEDIUM
                    subGbc.weightx = 0.0
                    subGbc.weighty = 1.0
                    subGbc.fill = GridBagConstraints.CENTER

                    val title = CLabel(this.sectionTitle, FontType.TITLE).apply { horizontalAlignment = SwingConstants.CENTER }
                    panel.add(title, subGbc)
                    subGbc.gridy++

                    for (content in this.sectionContent) {
                        content.add(panel, subGbc)
                        subGbc.gridy++
                    }

                    component.add(panel, gbc)
                }

                is Docs.DocComponent.Table -> {
                    val panel = CPanel(true, BorderMode.THICKNESS, roundCorners = true)
                    panel.layout = GridBagLayout()
                    val subGbc = GridBagConstraints()
                    subGbc.gridx = 0
                    subGbc.gridy = 0
                    subGbc.insets = UIStates.scale.get().INSETS_MEDIUM
                    subGbc.weightx = 1.0
                    subGbc.weighty = 1.0
                    subGbc.fill = GridBagConstraints.HORIZONTAL

                    for (element in this.header) {
                        val label = CLabel(element, FontType.TITLE).apply {
                            horizontalAlignment = SwingConstants.CENTER
                        }
                        panel.add(label, subGbc)
                        subGbc.gridx++
                    }

                    subGbc.gridy++

                    for (row in this.contentRows) {
                        subGbc.gridx = 0
                        for (element in row) {
                            element.add(panel, subGbc)
                            subGbc.gridx++
                        }
                        subGbc.gridy++
                    }

                    component.add(panel, gbc)
                }

                is Docs.DocComponent.Text -> {
                    val text = CTextArea(FontType.BASIC).apply {
                        text = this@add.content
                        isEditable = false
                    }
                    component.add(text, gbc)
                }

                is Docs.DocComponent.UnlinkedList -> {
                    val panel = CPanel(false, BorderMode.THICKNESS, roundCorners = true)
                    panel.layout = GridBagLayout()

                    val subGbc = GridBagConstraints()
                    subGbc.gridx = 0
                    subGbc.gridy = 0
                    subGbc.insets = UIStates.scale.get().INSETS_MEDIUM
                    subGbc.fill = GridBagConstraints.HORIZONTAL
                    subGbc.weightx = 1.0
                    subGbc.anchor = GridBagConstraints.WEST // Align list items to the left

                    for (content in this.entrys) {
                        content.add(panel, subGbc)
                        subGbc.gridy++
                    }

                    component.add(panel, gbc)
                }
            }
        }
    }
}