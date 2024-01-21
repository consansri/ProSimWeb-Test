package emulator.archs

import emulator.archs.t6502.T6502
import emulator.archs.t6502.T6502Syntax
import emulator.kit.Architecture
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : Architecture(T6502.config, T6502.asmConfig) {

    override fun exeSingleStep() {
        super.exeSingleStep()

        val currentPC = getRegContainer().pc.get().toHex()
        val opCode = getMemory().load(currentPC).toHex()
        val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) }

        if (instrType == null) {
            getConsole().error("Couldn't resolve Instruction at Address ${currentPC}!")
            return
        }
        val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

        instrType.execute(this, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())

    }

}