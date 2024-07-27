package prosim

import cengine.lang.asm.AsmLang
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.vfs.VirtualFile
import com.formdev.flatlaf.util.SystemInfo
import emulator.archs.ArchRV32
import prosim.ide.MainAppWindow
import prosim.ide.editor.code.PerformantCodeEditor
import prosim.ide.filetree.FileTree
import prosim.ide.filetree.FileTreeUIListener
import prosim.ui.components.NativeFrame
import prosim.uilib.UIStates
import prosim.uilib.styled.CSplitPane
import prosim.uilib.styled.CTextField
import prosim.uilib.styled.params.FontType
import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

/**
 * The main entry point for the ProSim application.
 */
fun main() {
    // Print a startup message to the console.
    println("---------------------------------------\nProSimWeb more like ProSimJVM\n---------------------------------------")

    // Check if the application is running on Linux.
    if (SystemInfo.isLinux) {
       // Set the default look and feel for JFrame and JDialog on Linux.
       JFrame.setDefaultLookAndFeelDecorated(true)
       JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testMainAppWindow()
    //testNewEditor()
    // Initialize and test the base application.
    //launchBaseApp()
}

/**
 * Initializes the base application for testing purposes.
 */
fun launchBaseApp() {
    // Create and display the main application frame using NativeFrame.
    SwingUtilities.invokeLater {
        NativeFrame()
    }

    // Uncomment the following line if BaseFrame with UIManager is needed.
    // BaseFrame()
}

fun testMainAppWindow(){

    val frame = object : JFrame() {
        val themeListener = UIStates.theme.createAndAddListener {
            revalidate()
            repaint()
        }
        val scaleListener = UIStates.scale.createAndAddListener {
            revalidate()
            repaint()
        }
    }

    frame.contentPane = MainAppWindow()
    frame.size = Dimension(1600, 1200)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}

fun testNewEditor() {
    val frame = JFrame()

    val project = Project(ProjectState("docs"), CownLang, AsmLang(ArchRV32().assembler))

    val fileTree = FileTree(project)

    val splitPane = CSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, fileTree.createContainer(), CTextField("Open File!",fontType = FontType.BASIC))

    fileTree.setFileTreeListener(object  : FileTreeUIListener {
        override fun onNodeSelected(file: VirtualFile) {

        }

        override fun onNodeExpanded(directory: VirtualFile) {

        }

        override fun onNodeCollapsed(directory: VirtualFile) {

        }

        override fun onCreateRequest(parentDirectory: VirtualFile, name: String, isDirectory: Boolean) {

        }

        override fun onDeleteRequest(file: VirtualFile) {

        }

        override fun onRenameRequest(file: VirtualFile, newName: String) {

        }

        override fun onOpenRequest(file: VirtualFile) {
            val editor = PerformantCodeEditor(file, project)
            splitPane.rightComponent = editor.createScrollPane()
            frame.title = "Editing: ${file.path}, lang: ${editor.psiManager?.lang?.name}"
        }
    })

    frame.contentPane = splitPane
    frame.size = Dimension(1600, 1200)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}


