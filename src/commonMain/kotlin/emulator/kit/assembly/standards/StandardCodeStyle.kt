package emulator.kit.assembly.standards

import emulator.kit.assembly.Compiler

data object StandardCodeStyle {
    val register = Compiler.CodeStyle.YELLOW
    val constant = Compiler.CodeStyle.BLUE

    val label = Compiler.CodeStyle.MAGENTA
    val directive = Compiler.CodeStyle.ORANGE
    val instruction = Compiler.CodeStyle.VIOLET

    val pre_import = Compiler.CodeStyle.GREEN
    val comment = Compiler.CodeStyle.BASE5
    val pre_equ = Compiler.CodeStyle.BASE2
    val pre_macro = Compiler.CodeStyle.BASE1

    val set_pc = Compiler.CodeStyle.GREEN
}