package me.c3.ui.theme.core

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.style.*
import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException
import java.io.InputStream
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

interface Theme {
    val name: String
    val icon: FlatSVGIcon
    val dark: Boolean
    val codeLaF: CodeLaF
    val dataLaF: DataLaF
    val globalLaF: GlobalLaF
    val iconLaF: IconLaF
    val textLaF: TextLaF
    val exeStyle: ExeLaF

    fun loadFont(url: String): Font {
        val inputStream: InputStream? = this::class.java.classLoader.getResourceAsStream(url)

        requireNotNull(inputStream) { "Font file not found: $url" }

        return try {
            Font.createFont(Font.TRUETYPE_FONT, inputStream)
        } catch (e: FontFormatException) {
            throw RuntimeException("Error loading font", e)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}