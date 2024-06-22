package prosim

import com.formdev.flatlaf.util.SystemInfo
import prosim.ui.components.NativeFrame
import javax.swing.*

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

    // Initialize and test the base application.
    testBaseApp()
}

/**
 * Initializes the base application for testing purposes.
 */
fun testBaseApp() {
    // Create and display the main application frame using NativeFrame.
    NativeFrame()

    // Uncomment the following line if BaseFrame with UIManager is needed.
    // BaseFrame()
}



