package me.c3

import com.formdev.flatlaf.util.SystemInfo
import me.c3.ui.components.BaseFrame
import javax.swing.*

fun main() {
    println("#######################################\nWhaaaat ProSimWeb has a jvm ui?\nCrazy! :D\n#######################################")

    if (SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
    }

    testBaseApp()
}

fun testBaseApp() {
    BaseFrame("BaseApp")
}