package emulator.archs.ikrmini

import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import StyleAttr.Main.Editor.HL

object IKRMiniFlags {


    val constant = HL.blue.getFlag()
    val symbol = HL.base00.getFlag()

    val instr = HL.violet.getFlag()
    val directive = HL.orange.getFlag()
    val label = HL.magenta.getFlag()
    val setpc = HL.yellow.getFlag()

    val comment = HL.base05.getFlag()
    val pre_import = HL.green.getFlag()
    val pre_global = HL.base00.getFlag()
    val pre_equ = HL.base02.getFlag()
    val pre_option = HL.base04.getFlag()
    val pre_macro = HL.base01.getFlag()
    val pre_attribute = HL.base04.getFlag()
    val set_pc = HL.green.getFlag()

    fun getInstrHL(vararg tokens: Compiler.Token): Syntax.ConnectedHL {
        return if(tokens.isNotEmpty()){
            Syntax.ConnectedHL(instr to listOf(tokens.first()) , constant to tokens.filterIsInstance<Compiler.Token.Constant>(), symbol to tokens.filterIsInstance<Compiler.Token.Symbol>(), label to tokens.filterIsInstance<Compiler.Token.Word>().drop(0))
        }else{
            Syntax.ConnectedHL()
        }
    }


}