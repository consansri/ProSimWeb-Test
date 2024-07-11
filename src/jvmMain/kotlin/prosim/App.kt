package prosim

import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.vfs.VFileSystem
import com.formdev.flatlaf.util.SystemInfo
import emulator.kit.nativeError
import prosim.ui.components.NativeFrame
import prosim.uilib.styled.editor3.CEditorArea
import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JFrame

/**
 * The main entry point for the ProSim application.
 */
fun main() {
    // Print a startup message to the console.
    println("#######################################\nProSimWeb has a jvm ui?\nCrazy :D\n#######################################")

    // Check if the application is running on Linux.
    if (SystemInfo.isLinux) {
        // Set the default look and feel for JFrame and JDialog on Linux.
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testNewEditor()
    // Initialize and test the base application.
    //launchBaseApp()
}

/**
 * Initializes the base application for testing purposes.
 */
fun launchBaseApp() {
    // Create and display the main application frame using NativeFrame.
    NativeFrame()

    // Uncomment the following line if BaseFrame with UIManager is needed.
    // BaseFrame()
}

fun testNewEditor() {
    val frame = JFrame()

    val project = Project("docs", CownLang)

    val file = project.fileSystem.findFile(VFileSystem.DELIMITER + "test.cown")
    file?.let {
        val editor = CEditorArea(file, project)
        frame.contentPane = editor.scrollPane
        frame.title = "${file.path}, lang: ${editor.psiManager?.lang?.name}"
    } ?: {
        nativeError("Couldn't open File!")
    }
    frame.size = Dimension(1600, 1200)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}



