package prosim.ui

import emulator.kit.Architecture
import emulator.kit.assembler.Process
import prosim.ui.components.editor.EditorFile
import prosim.uilib.state.EventManager

object Events {

    val archSettingChange = EventManager<Architecture>("Architecture Setting Changed")
    val archFeatureChange = EventManager<Architecture>("Architecture Feature Changed")
    val compile = EventManager<Process.Result>("Assembly Compiled")
    val exe = EventManager<Architecture>("Processor Executed")
    val fileEdit = EventManager<EditorFile>("File Edited")

}