package emulator.kit.assembly.standards

import emulator.kit.assembly.Compiler

data object StandardCodeStyle {
    val register = Compiler.CodeStyle.YELLOW // 2, 6, 8, 1, 7, 3, 14, 11, 10
    val constant = Compiler.CodeStyle.BLUE
    val operator = Compiler.CodeStyle.BASE1
    val bracket = Compiler.CodeStyle.BASE2

    val label = Compiler.CodeStyle.MAGENTA
    val directive = Compiler.CodeStyle.ORANGE
    val instruction = Compiler.CodeStyle.VIOLET

    val pre_import = Compiler.CodeStyle.GREEN
    val comment = Compiler.CodeStyle.BASE5
    val pre_equ = Compiler.CodeStyle.BASE2
    val pre_macro = Compiler.CodeStyle.BASE1

    val set_pc = Compiler.CodeStyle.GREEN
}

fun Compiler.Token.Constant.hlStdConstant(){
    this.hl(StandardCodeStyle.constant, StandardCodeStyle.operator, StandardCodeStyle.bracket)
}