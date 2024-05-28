package emulator.kit.common.memory

import debug.DebugTools
import emulator.kit.nativeWarn
import emulator.kit.types.Variable

class MainMemory(override val addressSize: Variable.Size, override val instanceSize: Variable.Size, endianess: Endianess): Memory() {
    override val initBin: String = "0"

    var memList: MutableList<MemInstance> = mutableListOf()
    private var editableValues: MutableList<MemInstance.EditableValue> = mutableListOf()
    var ioBounds: IOBounds? = null
        set(value) {
            field = value
            resetEditSection()
        }

    init {
        this.endianess = endianess
    }

    override fun load(address: Variable.Value.Hex): Variable.Value.Bin {
        val value = memList.firstOrNull { it.address.getRawHexStr() == address.toHex().getRawHexStr() }?.variable?.value?.toBin()
        return (value ?: getInitialBinary().get().toBin())
    }

    override fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType, readonly: Boolean): MemAccessResult {
        val hexValue = value.toHex()
        var hexAddress = address.toHex()

        val bytes = if(endianess == Endianess.LittleEndian) hexValue.splitToByteArray().reversed() else hexValue.splitToByteArray().toList()

        if (DebugTools.KIT_showMemoryInfo) {
            println("saving... ${endianess.name} ${hexValue.getRawHexStr()}, $bytes to ${hexAddress.getRawHexStr()}")
        }

        for (word in bytes) {
            val instance = memList.firstOrNull { it.address.getRawHexStr() == hexAddress.getRawHexStr() }
            if (instance != null) {
                if (!instance.readonly) {
                    instance.variable.setHex(word.toString())
                    if (mark != InstanceType.ELSE) {
                        instance.mark = mark
                    }
                } else {
                    nativeWarn("Memory: Denied writing data (address: ${hexAddress.getHexStr()}, value: ${hexValue.getHexStr()}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, instanceSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly, entrysInRow)
                memList.add(newInstance)
            }
            hexAddress = (hexAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }

        return MemAccessResult.HIT
    }

    override fun getAllInstances(): List<MemInstance> = memList

    override fun clear() {
        this.memList.clear()
        resetEditSection()
    }

    private fun resetEditSection() {
        memList.clear()
        editableValues.clear()
        ioBounds?.let {
            var addr = it.lowerAddr
            for (i in 0..<it.amount) {
                editableValues.add(MemInstance.EditableValue(addr.toHex().getUResized(addressSize), Variable.Value.Hex("0", instanceSize), entrysInRow))
                addr += Variable.Value.Hex("1", addressSize)
            }
        }
        for (value in editableValues) {
            memList.remove(value)
            memList.add(value)
        }
    }

    data class IOBounds(val lowerAddr: Variable.Value, val amount: Long)
}