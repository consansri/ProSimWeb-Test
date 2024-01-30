package emulator.archs.riscv64

import StyleAttr.Main.Editor.HL

object RV64Flags {

    val offset = HL.cyan.getFlag()
    val register = HL.yellow.getFlag()
    val constant = HL.blue.getFlag()

    val label = HL.magenta.getFlag()
    val directive = HL.orange.getFlag()
    val instruction = HL.violet.getFlag()

    val pre_import = HL.green.getFlag()
    val comment = HL.base05.getFlag()
    val pre_global = HL.base00.getFlag()
    val pre_equ = HL.base02.getFlag()
    val pre_option = HL.base04.getFlag()
    val pre_macro = HL.base01.getFlag()
    val pre_attribute = HL.base04.getFlag()

    val pre_unresolved = HL.base05.getFlag()

    val set_pc = HL.green.getFlag()
}