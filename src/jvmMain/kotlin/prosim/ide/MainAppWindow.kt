package prosim.ide

import cengine.lang.asm.AsmLang
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.vfs.VirtualFile
import emulator.archs.ArchRV32
import prosim.ide.editor.CDraggableTabbedEditorPane
import prosim.ide.editor.code.PerformantCodeEditor
import prosim.ide.filetree.FileTree
import prosim.ide.filetree.FileTreeUIAdapter
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CResizableBorderPanel
import prosim.uilib.styled.ColouredPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout

class MainAppWindow : CPanel() {

    val project = Project(ProjectState("docs"), CownLang, AsmLang(ArchRV32().assembler))

    val fileTree = FileTree(project).apply {
        setFileTreeListener(object : FileTreeUIAdapter() {
            override fun onOpenRequest(file: VirtualFile) {
                editorPanel.addTab(PerformantCodeEditor(file, project))
            }
        })
    }

    private val northPane = NORTHControls()
    private val eastPane = EASTControls()
    private val southPane = SOUTHControls()
    private val westPane = WESTControls()

    val contentPane = CResizableBorderPanel()
    val editorPanel = CDraggableTabbedEditorPane() // CAdvancedTabPane(tabsAreCloseable = true, primary = true, emptyMessage = "Open Files through File tree.")
    val leftContentPane = fileTree.createContainer()
    val rightContentPane = CLabel("Right Content", FontType.TITLE)
    val bottomContentPane = CLabel("Bottom Content", FontType.TITLE)

    init {
        setupLayout()
    }

    private fun setupLayout() {
        layout = BorderLayout()

        // Inner ContentPanel

        contentPane.add(editorPanel, BorderLayout.CENTER)
        contentPane.add(leftContentPane, BorderLayout.WEST)
        contentPane.add(bottomContentPane, BorderLayout.SOUTH)
        contentPane.add(rightContentPane, BorderLayout.EAST)

        // Outer Main Panel

        add(northPane, BorderLayout.NORTH)
        add(eastPane, BorderLayout.EAST)
        add(southPane, BorderLayout.SOUTH)
        add(westPane, BorderLayout.WEST)
        add(contentPane, BorderLayout.CENTER)
    }

    private inner class EASTControls() : CPanel(borderMode = BorderMode.WEST) {
        init {

        }
    }

    private inner class WESTControls() : CPanel(borderMode = BorderMode.EAST) {

    }

    private inner class NORTHControls() : CPanel(borderMode = BorderMode.SOUTH) {

    }

    private inner class SOUTHControls() : ColouredPanel() {

    }

}