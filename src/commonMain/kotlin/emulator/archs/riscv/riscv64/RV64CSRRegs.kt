package emulator.archs.riscv.riscv64

import androidx.compose.runtime.mutableStateListOf
import cengine.util.newint.IntNumber
import cengine.util.newint.UInt64
import emulator.archs.riscv.RV
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class RV64CSRRegs : RegFile<UInt64> {
    override val name: String = "csr"

    override val indentificators: List<FieldProvider> = listOf(
        RV.CSRNameProvider(false)
    )

    override val descriptors: List<FieldProvider> = listOf(
        RV.CSRPrivilegeProvider
    )

    override val regValues = mutableStateListOf(*Array(4096) { UInt64.ZERO })

    override fun set(index: Int, value: IntNumber<*>) {
        regValues[index] = value.toUInt64()
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt64.ZERO
        }
    }
}