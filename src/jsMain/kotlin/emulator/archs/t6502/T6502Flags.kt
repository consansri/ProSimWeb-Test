package emulator.archs.t6502

import StyleAttr.Main.Editor.HL
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax

object T6502Flags {


    val comment = HL.base05.getFlag()
    val constant = HL.blue.getFlag()
    val symbol = HL.base00.getFlag()
    val reg = HL.base02.getFlag()

    val instr = HL.violet.getFlag()
    val label = HL.magenta.getFlag()
    val setpc = HL.yellow.getFlag()

    fun getInstrHL(instrName: Set<Compiler.Token>, symbols: Set<Compiler.Token>, constants: Set<Compiler.Token.Constant>, regLetters: Set<Compiler.Token>, labelLinks: Set<Compiler.Token>): Syntax.ConnectedHL {
        return Syntax.ConnectedHL(instr to instrName, constant to constants, symbol to symbols, reg to regLetters, label to labelLinks)
    }


}