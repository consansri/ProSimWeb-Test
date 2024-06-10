package emulator.archs.ikrrisc2

import emulator.kit.types.Variable.Value.Bin

object IKRRisc2BinMapper {

    fun generateBinaryArray(instr: IKRRisc2Assembler.IKRRisc2Instr): Bin {
        val type = instr.type
        val paramType = type.paramType

        val bin: Bin = when(paramType){
            ParamType.I_TYPE -> TODO()
            ParamType.R_TYPE -> TODO()
            ParamType.R_TYPE_1S -> TODO()
            ParamType.L_TYPE_OFF -> TODO()
            ParamType.L_TYPE_INDEX -> TODO()
            ParamType.S_TYPE_OFF -> TODO()
            ParamType.S_TYPE_INDEX -> TODO()
            ParamType.BRANCH_DISP18 -> TODO()
            ParamType.J_DISP26 -> TODO()
            ParamType.J_RB -> TODO()
        }


    }


}