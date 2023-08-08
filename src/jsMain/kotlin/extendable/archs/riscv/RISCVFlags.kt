package extendable.archs.riscv

import StyleConst
import StyleConst.Main.Editor.HL

object RISCVFlags {

    val offset = HL.magenta.getFlag()
    val register = HL.yellow.getFlag()
    val constant = HL.blue.getFlag()

    val label = HL.cyan.getFlag()
    val directive = HL.orange.getFlag()
    val instruction = HL.violet.getFlag()

    val pre_import = HL.green.getFlag()
    val comment = HL.base05.getFlag()
    val pre_equ = HL.base03.getFlag()
    val pre_option = HL.base04.getFlag()
    val pre_macro = HL.base03.getFlag()
    val pre_attribute = HL.base04.getFlag()

}