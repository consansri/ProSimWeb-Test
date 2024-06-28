package prosim.uilib.theme.core

import com.formdev.flatlaf.extras.FlatSVGIcon
import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface
import prosim.uilib.theme.core.style.*
import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException
import java.io.InputStream

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


    companion object{
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

        fun loadSkiaTF(url: String): Typeface? {
            try {
                val resource = this::class.java.classLoader.getResourceAsStream(url)

                if (resource != null) {
                    return Typeface.makeFromData(Data.makeFromBytes(resource.readBytes()))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            return null
        }
    }

}