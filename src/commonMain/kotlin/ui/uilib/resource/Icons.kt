package ui.uilib.resource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Data
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skiko.loadBytesFromPath


interface Icons {

    val name: String

    // Define Icons as strings representing the resource paths
    val appLogo: String
    val add: String
    val autoscroll: String
    val backwards: String
    val bars: String
    val build: String
    val cancel: String
    val clearStorage: String
    val combineCells: String
    val continuousExe: String
    val console: String
    val darkmode: String
    val deleteBlack: String
    val deleteRed: String
    val disassembler: String
    val edit: String
    val export: String
    val fileCompiled: String
    val fileNotCompiled: String
    val forwards: String
    val home: String
    val import: String
    val info: String
    val lightmode: String
    val logo: String
    val pin: String
    val processor: String
    val processorBold: String
    val processorLight: String
    val recompile: String
    val refresh: String
    val reportBug: String
    val returnSubroutine: String
    val reverse: String
    val settings: String
    val singleExe: String
    val splitCells: String
    val statusError: String
    val statusFine: String
    val statusLoading: String
    val stepInto: String
    val stepMultiple: String
    val stepOut: String
    val stepOver: String
    val tag: String

    // Controls
    val switchOn: String
    val switchOff: String

    // Window Decorations
    val decrease: String
    val increase: String
    val close: String

    // File Tree Icons
    val folder: String
    val file: String
    val asmFile: String
    val folderClosed: String
    val folderOpen: String
    
    companion object {

        val resourceLoadScope = CoroutineScope(Dispatchers.Default)

        /**
         * Converts a ByteArray containing SVG data to a Painter.
         */
        @Composable
        fun loadSvgPainterFromBytes(bytes: ByteArray): Painter {
            return remember {
                val svg = try {
                    SVGDOM(Data.makeFromBytes(bytes))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                SvgPainter(svg)
            }
        }

        /**
         * Loads a file from a path into a ByteArray.
         *
         * @param path The path to the resource file.
         * @return ByteArray of the file content.
         */
        suspend fun loadByteArray(path: String): ByteArray {
            return withContext(Dispatchers.Default){
                loadBytesFromPath(path)
            }
        }
    }

}