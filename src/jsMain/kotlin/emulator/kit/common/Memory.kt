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
    var endianess: Endianess,
    private var ioBounds: IOBounds? = IOBounds(Variable.Value.Hex("00F00000", addressSize), 16)
) {
    private var memMap: MutableMap<String, MemInstance> = mutableMapOf()
    private var editableValues: MutableList<MemInstance.EditableValue> = mutableListOf()


    init {
        store(Variable.Value.Bin("0"), Variable(wordSize))
        store(getAddressMax(), Variable(wordSize))
        resetEditSection()
    }

    fun setEndianess(endianess: Endianess) {
        this.endianess = endianess
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("Memory: switched Endianess to ${endianess.name}")
        }
    }

    fun getEndianess(): Endianess = endianess
    fun store(address: Variable.Value, variable: Variable, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = variable.get().toHex().getRawHexStr().reversed().chunked(wordSize.bitWidth / 4) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        val hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("saving...  ${variable.get().toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]

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
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
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

        var hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("Memory: saving... ${endianess.name} {${values.joinToString(" ") { it.toHex().getHexStr() }}}, $wordList to ${hexAddress.getRawHexStr()}")
        }

        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]
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
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + Variable.Value.Hex("1", Variable.Size.Bit8())).toHex()
        }
    }

    fun store(address: Variable.Value, value: Variable.Value, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = value.toHex().getRawHexStr().reversed().chunked(wordSize.bitWidth / 4) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        var hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.KIT_showMemoryInfo) {
            console.log("saving... ${endianess.name} ${value.toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }

        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]
            if (instance != null) {
                if (!instance.readonly) {
                    instance.variable.setHex(word.toString())
                    if (mark != StyleAttr.Main.Table.Mark.ELSE) {
                        instance.mark = mark
                    }
                } else {
                    console.warn("Memory: Denied writing data (address: ${address.toHex().getHexStr()}, value: ${value.toHex().getHexStr()}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }
    }

    fun load(address: Variable.Value): Variable.Value {
        val value = memMap.get(address.toHex().getUResized(addressSize).getRawHexStr())?.variable?.get()
        if (value != null) {
            return value
        } else {
            return getInitialBinary().get()
        }
    }

    fun load(address: Variable.Value, amount: Int): Variable.Value {
        val instances = mutableListOf<String>()

        var instanceAddress = address.toBin().getUResized(addressSize)
        for (i in 0 until amount) {
            val value = load(instanceAddress)
            instances.add(value.toBin().getRawBinaryStr())
            instanceAddress = (instanceAddress + Variable.Value.Bin("1", addressSize)).toBin()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return Variable.Value.Bin(instances.joinToString("") { it }, Variable.Tools.getNearestSize(amount * wordSize.bitWidth))
    }

    fun clear() {
        this.memMap.clear()
        store(Variable.Value.Bin("0"), Variable(wordSize))
        store(getAddressMax(), Variable(wordSize))
        resetEditSection()
    }

    private fun resetEditSection() {
        memMap.clear()
        editableValues.clear()
        ioBounds?.let {
            var addr = it.lowerAddr
            for (i in 0..<it.amount) {
                editableValues.add(MemInstance.EditableValue(addr.toHex().getUResized(addressSize), Variable.Value.Hex("0", wordSize)))
                addr += Variable.Value.Hex("1", addressSize)
            }
        }
        for (value in editableValues) {
            val key = value.address.getUResized(addressSize).getRawHexStr()
            memMap.remove(key)
            memMap[key] = value
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

    fun getMemMap(): Map<String, MemInstance> {
        return memMap
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

    open class MemInstance(val address: Variable.Value.Hex, var variable: Variable, var mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, val readonly: Boolean = false) {
        class EditableValue(address: Variable.Value.Hex, value: Variable.Value.Hex) : MemInstance(address, Variable(value), StyleAttr.Main.Table.Mark.EDITABLE)
    }

    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"),
        BigEndian("Big Endian")
    }

    data class IOBounds(val lowerAddr: Variable.Value, val amount: Long)

}