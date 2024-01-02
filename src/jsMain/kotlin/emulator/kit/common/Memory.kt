package emulator.kit.common

import StyleAttr
import emulator.kit.types.Variable
import debug.DebugTools

/**
 * For emulating the [Memory] of architectures, this class is used. [store] and [load] functions are already implemented which depend on the [endianess].
 * Beside the normal not directly editable values there are [MemInstance.EditableValue]s to allow memory mapped i/o emulation.
 *
 * @constructor expecting a [addressSize] and [wordSize] such as initial binary values ([initBin]) and the [endianess] of the memory.
 *
 */
class Memory(
    private val addressSize: Variable.Size,
    private val initBin: String,
    private val wordSize: Variable.Size,
    private var endianess: Endianess,
    private var ioBounds: IOBounds? = null,
    private var entrysInRow: Int = 16
) {
    var memList: MutableList<MemInstance> = mutableListOf()
    private var editableValues: MutableList<MemInstance.EditableValue> = mutableListOf()

    init {
        resetEditSection()
    }

    fun setEndianess(endianess: Endianess) {
        this.endianess = endianess
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("Memory: switched Endianess to ${endianess.name}")
        }
    }

    fun getEntrysInRow(): Int = entrysInRow
    fun setEntrysInRow(amount: Int) {
        entrysInRow = amount
        memList.forEach { it.reMap(amount) }
    }

    fun getEndianess(): Endianess = endianess
    fun store(address: Variable.Value, variable: Variable, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = variable.get().toHex().getRawHexStr().reversed().chunked(wordSize.bitWidth / 4) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        val hexAddress = address.toHex()
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("saving...  ${variable.get().toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memList.firstOrNull { it.address == hexAddress }

            if (instance != null) {
                if (!instance.readonly) {
                    instance.variable.setHex(word.toString())
                    if (mark != StyleAttr.Main.Table.Mark.ELSE) {
                        instance.mark = mark
                    }
                } else {
                    console.warn("Memory: Denied writing data (address: ${address.toHex().getHexStr()}, value: ${variable.get().toHex().getHexStr()}) in readonly Memory!")
                }
            } else {
                val zeroValue = Variable(initBin, wordSize)
                zeroValue.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, zeroValue, mark, readonly, entrysInRow)
                memList.add(newInstance)
            }
        }
    }

    fun storeArray(address: Variable.Value, vararg values: Variable.Value, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = values.map { value -> value.toHex().getRawHexStr().reversed().chunked(wordSize.bitWidth / 4) { it.reversed() } }.reversed().flatten()

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        var hexAddress = address.toHex()
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("Memory: saving... ${endianess.name} {${values.joinToString(" ") { it.toHex().getHexStr() }}}, $wordList to ${hexAddress.getRawHexStr()}")
        }

        for (word in wordList) {
            val instance = memList.firstOrNull { it.address.getRawHexStr() == hexAddress.getRawHexStr() }
            if (instance != null) {
                if (!instance.readonly) {
                    instance.variable.setHex(word.toString())
                    if (mark != StyleAttr.Main.Table.Mark.ELSE) {
                        instance.mark = mark
                    }
                } else {
                    console.warn("Memory: Denied writing data (address: ${address.toHex().getHexStr()}, values: {${values.joinToString(" ") { it.toHex().getHexStr() }}}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly, entrysInRow)
                memList.add(newInstance)
            }
            hexAddress = (hexAddress + Variable.Value.Hex("1", Variable.Size.Bit8())).toHex()
        }
    }

    fun store(address: Variable.Value, value: Variable.Value, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        val hexValue = value.toHex()
        var hexAddress = address.toHex()

        // Little Endian
        var wordList = hexValue.getRawHexStr().reversed().chunked(wordSize.bitWidth / 4) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }


        if (DebugTools.KIT_showMemoryInfo) {
            console.log("saving... ${endianess.name} ${hexValue.getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }

        for (word in wordList) {
            val instance = memList.firstOrNull { it.address.getRawHexStr() == hexAddress.getRawHexStr() }
            if (instance != null) {
                if (!instance.readonly) {
                    instance.variable.setHex(word.toString())
                    if (mark != StyleAttr.Main.Table.Mark.ELSE) {
                        instance.mark = mark
                    }
                } else {
                    console.warn("Memory: Denied writing data (address: ${hexAddress.getHexStr()}, value: ${hexValue.getHexStr()}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly, entrysInRow)
                memList.add(newInstance)
            }
            hexAddress = (hexAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }
    }

    fun load(address: Variable.Value.Hex): Variable.Value.Bin {
        val value = memList.firstOrNull { it.address.getRawHexStr() == address.toHex().getRawHexStr() }?.variable?.value?.toBin()
        return value ?: getInitialBinary().get().toBin()
    }

    fun load(address: Variable.Value.Hex, amount: Int): Variable.Value.Bin {
        val instances = mutableListOf<String>()

        var instanceAddress = address.getUResized(addressSize)
        for (i in 0..<amount) {
            val value = load(instanceAddress)
            instances.add(value.toBin().getRawBinaryStr())
            instanceAddress = (instanceAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return Variable.Value.Bin(instances.joinToString("") { it })
    }

    fun clear() {
        this.memList.clear()
        resetEditSection()
    }

    private fun resetEditSection() {
        memList.clear()
        editableValues.clear()
        ioBounds?.let {
            var addr = it.lowerAddr
            for (i in 0..<it.amount) {
                editableValues.add(MemInstance.EditableValue(addr.toHex().getUResized(addressSize), Variable.Value.Hex("0", wordSize), entrysInRow))
                addr += Variable.Value.Hex("1", addressSize)
            }
        }
        for (value in editableValues) {
            memList.remove(value)
            memList.add(value)
        }
    }

    fun getIOBounds(): IOBounds? {
        return ioBounds
    }

    fun useIOBounds(startAddress: Variable.Value.Hex, amount: Long) {
        ioBounds = IOBounds(startAddress, amount)
        resetEditSection()
    }

    fun removeIOBounds() {
        ioBounds = null
        resetEditSection()
    }

    fun getMemList(): List<MemInstance> {
        return memList
    }

    fun getAddressMax(): Variable.Value {
        return Variable.Value.Hex("0", addressSize).getBiggest()
    }

    fun getEditableInstances(): List<MemInstance.EditableValue> {
        return editableValues
    }

    fun getInitialBinary(): Variable {
        return Variable(initBin, wordSize)
    }

    fun getAddressSize(): Variable.Size {
        return addressSize
    }

    fun getWordSize(): Variable.Size {
        return wordSize
    }

    open class MemInstance(val address: Variable.Value.Hex, var variable: Variable, var mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, val readonly: Boolean = false, entrysInRow: Int) {

        private val addrRelevantForOffset: Int
        var offset: Int
        var row: Variable.Value.Hex

        init {
            val tempAddrRelevantForOffset = address.getRawHexStr().substring(address.getRawHexStr().length - 4).toIntOrNull(16)
            addrRelevantForOffset = if (tempAddrRelevantForOffset != null) {
                tempAddrRelevantForOffset
            } else {
                console.error("couldn't extract relevant address part (from ${address.getRawHexStr()}) for offset calculation!")
                0
            }

            offset = addrRelevantForOffset % entrysInRow
            row = (address - Variable.Value.Hex(offset.toString(16), Variable.Size.Bit8())).toHex()
        }

        fun reMap(entrysInRow: Int) {
            offset = addrRelevantForOffset % entrysInRow
            row = (address - Variable.Value.Hex(offset.toString(16), Variable.Size.Bit8())).toHex()
        }

        class EditableValue(address: Variable.Value.Hex, value: Variable.Value.Hex, entrysInRow: Int) : MemInstance(address, Variable(value), StyleAttr.Main.Table.Mark.EDITABLE, entrysInRow = entrysInRow)
    }

    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"),
        BigEndian("Big Endian")
    }

    data class IOBounds(val lowerAddr: Variable.Value, val amount: Long)

}