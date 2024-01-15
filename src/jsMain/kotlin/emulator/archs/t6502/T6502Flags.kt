package emulator.archs.t6502

import StyleAttr.Main.Editor.HL
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax

object T6502Flags {

    val instr = HL.violet.getFlag()
    val comment = HL.base05.getFlag()
    val constant = HL.magenta.getFlag()
    val symbol = HL.base00.getFlag()
    val reg = HL.base02.getFlag()

    fun getInstrHL(instrName: List<Compiler.Token>, symbols: List<Compiler.Token>, constants: List<Compiler.Token.Constant>, regLetters: List<Compiler.Token>): Syntax.ConnectedHL {
        return Syntax.ConnectedHL(instr to instrName, constant to constants, symbol to symbols, reg to regLetters)
    }


}