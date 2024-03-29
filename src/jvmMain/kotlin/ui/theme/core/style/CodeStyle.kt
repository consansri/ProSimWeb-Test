package me.c3.ui.theme.core.style

import emulator.kit.assembly.Compiler
import java.awt.Color
import java.awt.Font

data class CodeStyle(
    val font: Font,
    val codeStyle: (Compiler.CodeStyle?) -> Color
)