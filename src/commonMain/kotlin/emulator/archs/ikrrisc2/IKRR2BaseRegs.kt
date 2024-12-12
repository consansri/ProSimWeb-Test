package emulator.archs.ikrrisc2

import androidx.compose.runtime.mutableStateListOf
import cengine.util.newint.IntNumber
import cengine.util.newint.UInt32
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class IKRR2BaseRegs : RegFile<UInt32> {
    override val name: String = "base"

    override val indentificators: List<FieldProvider> = listOf(
        IKRRisc2.BaseNameProvider
    )

    override val descriptors: List<FieldProvider> = listOf(
        IKRRisc2.BaseProvider
    )

    override val regValues = mutableStateListOf(*Array(32) {
        UInt32.ZERO
    })

    override fun set(index: Int, value: IntNumber<*>) {
        regValues[index] = value.toUInt32()
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt32.ZERO
        }
    }
}