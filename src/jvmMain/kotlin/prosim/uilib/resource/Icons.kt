package prosim.uilib.resource

import com.formdev.flatlaf.extras.FlatSVGIcon

interface Icons {

    val name: String

    val appLogo: FlatSVGIcon
    val add: FlatSVGIcon
    val autoscroll: FlatSVGIcon
    val backwards: FlatSVGIcon
    val bars: FlatSVGIcon
    val build: FlatSVGIcon
    val cancel: FlatSVGIcon
    val clearStorage: FlatSVGIcon
    val combineCells: FlatSVGIcon
    val continuousExe: FlatSVGIcon
    val console: FlatSVGIcon
    val darkmode: FlatSVGIcon
    val deleteBlack: FlatSVGIcon
    val deleteRed: FlatSVGIcon
    val disassembler: FlatSVGIcon
    val edit: FlatSVGIcon
    val export: FlatSVGIcon
    val fileCompiled: FlatSVGIcon
    val fileNotCompiled: FlatSVGIcon
    val forwards: FlatSVGIcon
    val home: FlatSVGIcon
    val import: FlatSVGIcon
    val info: FlatSVGIcon
    val lightmode: FlatSVGIcon
    val logo: FlatSVGIcon
    val pin: FlatSVGIcon
    val processor: FlatSVGIcon
    val processorBold: FlatSVGIcon
    val processorLight: FlatSVGIcon
    val recompile: FlatSVGIcon
    val refresh: FlatSVGIcon
    val reportBug: FlatSVGIcon
    val returnSubroutine: FlatSVGIcon
    val reverse: FlatSVGIcon
    val settings: FlatSVGIcon
    val singleExe: FlatSVGIcon
    val splitCells: FlatSVGIcon
    val statusError: FlatSVGIcon
    val statusFine: FlatSVGIcon
    val statusLoading: FlatSVGIcon
    val stepInto: FlatSVGIcon
    val stepMultiple: FlatSVGIcon
    val stepOut: FlatSVGIcon
    val stepOver: FlatSVGIcon
    val tag: FlatSVGIcon

    // Controls
    val switchOn: FlatSVGIcon
    val switchOff: FlatSVGIcon

    // Window Decorations
    val decrease: FlatSVGIcon
    val increase: FlatSVGIcon
    val close: FlatSVGIcon

    // File Tree Icons
    val folder: FlatSVGIcon
    val file: FlatSVGIcon
    val asmFile: FlatSVGIcon
    val folderClosed: FlatSVGIcon
    val folderOpen: FlatSVGIcon

    fun getLightMode(): FlatSVGIcon = lightmode

    fun getDarkMode(): FlatSVGIcon = darkmode

}