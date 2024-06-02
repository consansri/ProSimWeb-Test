package me.c3.ui

import emulator.kit.Architecture
import emulator.kit.assembler.Process
import me.c3.ui.components.editor.EditorFile
import me.c3.ui.state.Event

object Events {

    val featureChange = Event<Architecture>("Feature Changed")
    val compile = Event<Process.Result>("Assembly Compiled")
    val exe = Event<Architecture>("Processor Executed")
    val fileEdit = Event<EditorFile>("File Edited")

}