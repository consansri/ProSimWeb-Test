package emulator.kit.common.memory

import debug.DebugTools
import emulator.kit.nativeWarn
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.Bit8
import emulator.kit.types.Variable.Value.Hex

/**
 * Represents the main memory of a system.
 *
 * @property addressSize The size of the memory addresses.
 * @property instanceSize The size of each memory instance.
 * @property endianess The endianess of the memory.
 * @property name The name of the memory.
 * @property initHex The initial binary value for the memory.
 * @property endianess The endianess of the memory.
 * @property memList The list of memory instances.
 * @property editableValues The list of editable memory values.
 * @property ioBounds The input/output bounds of the memory.
 * @property entrysInRow The number of entries in each row of memory.
 *
 * @constructor Creates a MainMemory instance with the specified parameters.
 *
 * @param addressSize The size of the memory addresses.
 * @param instanceSize The size of each memory instance.
 * @param endianess The endianess of the memory.
 * @param name The name of the memory.
 */
class MainMemory(override val addressSize: Variable.Size, override val instanceSize: Variable.Size, endianess: Endianess, override val name: String = "Memory", entrysInRow: Int = 16) : Memory() {
    override val initHex: String = "0"

    val addrIncByOne = Hex("1", addressSize)
    var endianess: Endianess = Endianess.BigEndian
    var memList: MutableList<MemInstance> = mutableListOf()
    private var editableValues: MutableList<MemInstance.EditableValue> = mutableListOf()
    var ioBounds: IOBounds? = null
        set(value) {
            field = value
            resetEditSection()
        }

    var entrysInRow: Int = entrysInRow
        set(value) {
            field = value
            getAllInstances().forEach { it.reMap(value) }
        }

    init {
        this.endianess = endianess
    }

    override fun globalEndianess(): Endianess = endianess

    override fun load(address: Hex, amount: Int, tracker: AccessTracker, endianess: Endianess): Hex {
        val hexValues = mutableListOf<String>()
        var currAddr: Variable.Value = address
        repeat(amount) {
            val addr: String = currAddr.toHex().getUResized(addressSize).getRawHexStr()
            val value = memList.firstOrNull {
                it.address.getRawHexStr() == addr
            }?.variable?.value ?: getInitialBinary().get()
            currAddr += Hex("1", addressSize)
            hexValues += value.toHex().toRawString()
        }

        when (endianess) {
            Endianess.LittleEndian -> hexValues.reverse()
            Endianess.BigEndian -> {}
        }

        return Hex(hexValues.joinToString("") { it })
    }

    override fun store(address: Hex, value: Variable.Value, mark: InstanceType, readonly: Boolean, tracker: AccessTracker, endianess: Endianess) {
        val hexValue = value.toHex()
        var hexAddress = address.getUResized(addressSize)

        val words = if (endianess == Endianess.LittleEndian) hexValue.splitToArray(instanceSize).reversed() else hexValue.splitToArray(instanceSize).toList()

        if (DebugTools.KIT_showMemoryInfo) {
            println("saving... ${endianess.name} ${hexValue.getRawHexStr()}, $words to ${hexAddress.getRawHexStr()}")
        }

        for (word in words) {
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
                val variable = Variable(initHex, instanceSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly, entrysInRow)
                memList.add(newInstance)
            }
            hexAddress = (hexAddress + addrIncByOne).toHex()
        }
    }

    fun getAllInstances(): List<MemInstance> = memList

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
                editableValues.add(MemInstance.EditableValue(addr.toHex().getUResized(addressSize), Hex("0", instanceSize), entrysInRow))
                addr += Hex("1", addressSize)
            }
        }
        for (value in editableValues) {
            memList.remove(value)
            memList.add(value)
        }
    }

    open class MemInstance(val address: Hex, var variable: Variable, var mark: InstanceType = InstanceType.ELSE, val readonly: Boolean = false, entrysInRow: Int) {
        private var entrysInRow = entrysInRow
            set(value) {
                field = value
                addrRelevantForOffset = address.getRawHexStr().substring(address.getRawHexStr().length - entrysInRow / 16 - 1).toIntOrNull(16) ?: throw Exception("couldn't extract relevant address part (from ${address.getRawHexStr()}) for offset calculation!")
            }

        private var addrRelevantForOffset: Int = address.getRawHexStr().substring(address.getRawHexStr().length - entrysInRow / 16 - 1).toIntOrNull(16) ?: throw Exception("couldn't extract relevant address part (from ${address.getRawHexStr()}) for offset calculation!")
            set(value) {
                field = value
                offset = addrRelevantForOffset % entrysInRow
                row = (address - Hex(offset.toString(16), Bit8())).toHex()
            }

        var offset: Int = addrRelevantForOffset % entrysInRow
        var row: Hex = (address - Hex(offset.toString(16), Bit8())).toHex()

        fun reMap(entrysInRow: Int) {
            this.entrysInRow = entrysInRow
        }

        class EditableValue(address: Hex, value: Hex, entrysInRow: Int) : MemInstance(address, Variable(value), InstanceType.EDITABLE, entrysInRow = entrysInRow)

        override fun toString(): String {
            return variable.get().toHex().getRawHexStr()
        }
    }

    data class IOBounds(val lowerAddr: Variable.Value, val amount: Long)
}