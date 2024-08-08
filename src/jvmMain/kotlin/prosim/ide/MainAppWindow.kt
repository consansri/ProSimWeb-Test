package prosim.ide

import cengine.lang.asm.AsmLang
import cengine.lang.asm.target.riscv.rv64.RV64Spec
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.vfs.VirtualFile
import com.formdev.flatlaf.extras.FlatSVGIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.ide.editor.CDraggableTabbedEditorPane
import prosim.ide.editor.code.PerformantCodeEditor
import prosim.ide.filetree.FileTree
import prosim.ide.filetree.FileTreeUIAdapter
import prosim.uilib.UIResource
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.styled.*
import prosim.uilib.styled.editor.CConsole
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.params.IconSize
import java.awt.*

class MainAppWindow : CPanel() {

    private val overlayScope = CoroutineScope(Dispatchers.Default)

    val project = Project(ProjectState("docs"), CownLang, AsmLang(RV64Spec))

    val console = CConsole()

    val fileTree = FileTree(project).apply {
        setFileTreeListener(object : FileTreeUIAdapter() {
            override fun onOpenRequest(file: VirtualFile) {
                editorPanel.addTab(PerformantCodeEditor(file, project))
            }
        })
    }

    val problemView = CToolView("Problems") {
        project.currentEditors.mapNotNull { it as? PerformantCodeEditor }
    }

    private val northPane = NORTHControls()
    private val eastPane = EASTControls()
    private val southPane = SOUTHControls()
    private val westPane = WESTControls()

    val contentPane = CResizableBorderPanel()
    val editorPanel = CDraggableTabbedEditorPane() // CAdvancedTabPane(tabsAreCloseable = true, primary = true, emptyMessage = "Open Files through File tree.")

    init {
        setupLayout()
    }

    private fun setupLayout() {
        layout = BorderLayout()

        // Inner ContentPanel

        contentPane.add(editorPanel, BorderLayout.CENTER)

        // Outer Main Panel

        add(northPane, BorderLayout.NORTH)
        add(eastPane, BorderLayout.EAST)
        add(southPane, BorderLayout.SOUTH)
        add(westPane, BorderLayout.WEST)
        add(contentPane, BorderLayout.CENTER)
    }

    /**
     * Is deciding on the content of the left and bottom content Panel.
     */
    private inner class EASTControls : CPanel(borderMode = BorderMode.WEST) {
        private fun setRightContent(content: Component?) {
            if (content == null) return contentPane.removeComponentsAtConstraint(BorderLayout.EAST)
            contentPane.add(content, BorderLayout.EAST)
        }
    }

    /**
     * Is deciding on the content of the right content Panel.
     */
    private inner class WESTControls : CPanel(borderMode = BorderMode.EAST) {

        private val openFileTree = CIconToggle(UIStates.icon.get().folder, false, IconSize.PRIMARY_NORMAL) {
            if (it) {
                setLeftContent(fileTree.createContainer())
            } else {
                setLeftContent(null)
            }
        }

        private val emptyFiller = object : CPanel() {
            override val customBG: Color = Color(0, 0, 0, 0)
        }

        private val consoleToggle = CIconToggle(UIStates.icon.get().console, false, IconSize.PRIMARY_NORMAL) {
            if (it) {
                setBottomContent(console)
            } else {
                setBottomContent(null)
            }
        }

        private val problemToggle = CIconToggle(UIStates.icon.get().info, false, IconSize.PRIMARY_NORMAL) {
            if (it) {
                problemView.update()
                setBottomContent(problemView)
            } else {
                setBottomContent(null)
            }
        }

        init {
            layout = GridBagLayout()

            val gbc = GridBagConstraints()
            gbc.gridy = 0
            gbc.gridx = 0
            gbc.weighty = 0.0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(openFileTree, gbc)

            gbc.gridy += 1
            gbc.gridx = 0
            gbc.weighty = 1.0
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.BOTH
            add(emptyFiller, gbc)

            gbc.gridy += 1
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(consoleToggle, gbc)

            gbc.gridy += 1
            add(problemToggle, gbc)
        }

        private fun setBottomContent(content: Component?) {
            if (content == null) return contentPane.removeComponentsAtConstraint(BorderLayout.SOUTH)
            contentPane.add(content, BorderLayout.SOUTH)
        }

        private fun setLeftContent(content: Component?) {
            if (content == null) return contentPane.removeComponentsAtConstraint(BorderLayout.WEST)
            contentPane.add(content, BorderLayout.WEST)
        }
    }

    /**
     * Is only for global controls.
     */
    private inner class NORTHControls() : CPanel(borderMode = BorderMode.SOUTH) {

        private var currOverlay: CDialog? = null

        private val emptyFiller = object : CPanel() {
            override val customBG: Color = Color(0, 0, 0, 0)
        }

        private val scaleSwitch = CChooser<Scaling>(CChooser.Model(UIResource.scalings, UIStates.scale.get()), FontType.TITLE, {
            UIStates.scale.set(it)
        })

        private val themeButton = object : CIconButton(UIStates.theme.get().icon) {
            override val iconSupplier: FlatSVGIcon
                get() = UIStates.theme.get().icon
        }.apply {
            addActionListener {
                currOverlay?.dispose()
                showThemeSelector()
            }
        }

        private val settingsButton = CIconButton(UIStates.icon.get().settings).apply {
            addActionListener {
                currOverlay?.dispose()
                showSettings()
            }
        }

        init {
            layout = GridBagLayout()

            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.NONE

            gbc.gridx += 1
            gbc.weightx = 1.0
            gbc.weighty = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(emptyFiller, gbc)

            gbc.gridx += 1
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.NONE

            add(scaleSwitch, gbc)

            gbc.gridx += 1
            add(themeButton, gbc)

            gbc.gridx += 1
            add(settingsButton, gbc)
        }

        private fun setNorthContent(content: Component?) {
            if (content == null) return contentPane.removeComponentsAtConstraint(BorderLayout.NORTH)
            contentPane.add(content, BorderLayout.NORTH)
        }

        private fun showSettings() {
            overlayScope.launch {

            }
        }

        private fun showThemeSelector() {
            overlayScope.launch {
                val (dialog, theme) = COptionPane.showSelector(this@MainAppWindow, "Theme", UIResource.themes)
                val newTheme = theme.await()
                if (newTheme != null) {
                    UIStates.theme.set(newTheme)
                    revalidate()
                    repaint()
                }
            }
        }
    }

    /**
     * Is mainly a display for global information.
     */
    private inner class SOUTHControls() : ColouredPanel() {

    }

}