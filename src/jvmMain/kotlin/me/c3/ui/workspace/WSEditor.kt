package me.c3.ui.workspace

import java.io.File

interface WSEditor {

    fun openFile(file: File)

    fun updateFile(file: File)

}