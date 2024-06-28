package prosim

import com.formdev.flatlaf.util.SystemInfo
import prosim.ui.components.NativeFrame
import prosim.uilib.styled.STextButton
import prosim.uilib.styled.params.FontType
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
        // Set default look and feel for JFrame and JDialog on Linux.
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    //testSkia()

    // Initialize and test the base application.
    launchBaseApp()
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

fun testSkia() {
    val frame = JFrame()
    frame.contentPane = STextButton("Crazy Button", FontType.CODE)
    frame.size = Dimension(800,600)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}



