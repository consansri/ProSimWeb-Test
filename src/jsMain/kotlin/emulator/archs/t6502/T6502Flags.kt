package emulator.archs.t6502

import StyleAttr.Main.Editor.HL

object T6502Flags {

    val instr = HL.violet.getFlag()


    fun getExtHL(mode: T6502Syntax.AModes): HL {
        return when (mode) {
            T6502Syntax.AModes.ACCUMULATOR -> TODO()
            T6502Syntax.AModes.IMPLIED -> TODO()
            T6502Syntax.AModes.IMMEDIATE -> TODO()
            T6502Syntax.AModes.ABSOLUTE -> TODO()
            T6502Syntax.AModes.RELATIVE -> TODO()
            T6502Syntax.AModes.INDIRECT -> TODO()
            T6502Syntax.AModes.ZEROPAGE -> TODO()
            T6502Syntax.AModes.ABSOLUTE_X -> TODO()
            T6502Syntax.AModes.ABSOLUTE_Y -> TODO()
            T6502Syntax.AModes.ZEROPAGE_X -> TODO()
            T6502Syntax.AModes.ZEROPAGE_Y -> TODO()
            T6502Syntax.AModes.ZEROPAGE_X_INDIRECT -> TODO()
            T6502Syntax.AModes.ZPINDIRECT_Y -> TODO()

            else -> HL.base00
        }
    }


}