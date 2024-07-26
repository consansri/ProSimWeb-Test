package prosim.ide

import cengine.lang.asm.AsmLang
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import emulator.archs.ArchRV32
import prosim.ide.filetree.FileTree
import prosim.uilib.styled.*
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout

class MainAppWindow : CPanel() {

    val project = Project(ProjectState("docs"), CownLang, AsmLang(ArchRV32().assembler))
    val fileTree = FileTree(project)

    val northPane = CPanel()
    val eastPane = CPanel()
    val southPane = ColouredPanel(false)
    val westPane = CPanel()

    val contentPane = CResizableBorderPanel()
    val editorPanel = CAdvancedTabPane(tabsAreCloseable = true, primary = true, emptyMessage = "Open Files through File tree.")
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

}