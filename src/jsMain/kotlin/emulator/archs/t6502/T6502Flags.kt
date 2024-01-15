package emulator.archs.t6502

import StyleAttr.Main.Editor.HL
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax

object T6502Flags {

    val instr = HL.violet.getFlag()

    val comment = HL.base05.getFlag()

    val immediate = HL.magenta.getFlag()
    
    fun getConstantHL(constant: Compiler.Token.Constant): HL {
        return when(constant){
            is Compiler.Token.Constant.Ascii -> HL.blue
            is Compiler.Token.Constant.Binary -> HL.blue
            is Compiler.Token.Constant.Dec -> HL.blue
            is Compiler.Token.Constant.Hex -> HL.blue
            is Compiler.Token.Constant.String -> HL.blue
            is Compiler.Token.Constant.UDec -> HL.blue
        }
    }


}