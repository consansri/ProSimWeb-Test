package emulator.kit.common

import StyleAttr
import emulator.kit.types.Variable
import tools.DebugTools

class Memory(private val addressSize: Variable.Size, private val initBin: String, private val wordSize: Variable.Size, var endianess: Endianess) {
    private var memMap: MutableMap<String, MemInstance> = mutableMapOf()
    private var editableValues: MutableList<MemInstance.EditableValue> = mutableListOf()

    fun setEndianess(endianess: Endianess) {
        this.endianess = endianess
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("switched Endianess to ${endianess.name}")
        }
    }
    fun getEndianess(): Endianess = endianess
    fun save(address: Variable.Value, variable: Variable, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = variable.get().toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        val hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
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
                    console.warn("Denied writing data (address: ${address.toHex().getHexStr()}, value: ${variable.get().toHex().getHexStr()}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
        }
    }
    fun saveArray(address: Variable.Value, vararg values: Variable.Value, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = values.map {value -> value.toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() } }.reversed().flatten()

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        var hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("saving... ${endianess.name} {${values.joinToString(" ") { it.toHex().getHexStr() }}}, $wordList to ${hexAddress.getRawHexStr()}")
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
                    console.warn("Denied writing data (address: ${address.toHex().getHexStr()}, values: {${values.joinToString(" ") { it.toHex().getHexStr() }}}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + Variable.Value.Hex("1")).toHex()
        }
    }
    fun save(address: Variable.Value, value: Variable.Value, mark: StyleAttr.Main.Table.Mark = StyleAttr.Main.Table.Mark.ELSE, readonly: Boolean = false) {
        // Little Endian
        var wordList = value.toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.BigEndian) {
            // Big Endian
            wordList = wordList.reversed()
        }

        var hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
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
                    console.warn("Denied writing data (address: ${address.toHex().getHexStr()}, value: ${value.toHex().getHexStr()}) in readonly Memory!")
                }
            } else {
                val variable = Variable(initBin, wordSize)
                variable.setHex(word.toString())
                val newInstance = MemInstance(hexAddress, variable, mark, readonly)
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + Variable.Value.Hex("1")).toHex()
        }
    }
    fun load(address: Variable.Value): Variable {
        val value = memMap.get(address.toHex().getRawHexStr())?.variable
        if (value != null) {
            return value
        } else {
            return getInitialBinary()
        }
    }
    fun load(address: Variable.Value, amount: Int): Variable {
        val instances = mutableListOf<String>()

        var instanceAddress = address.toBin()
        for (i in 0 until amount) {
            val instance = load(instanceAddress)
            instances.add(instance.get().toBin().getRawBinaryStr())
            instanceAddress = (instanceAddress + Variable.Value.Bin("1", addressSize)).toBin()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return Variable(instances.joinToString("") { it }, Variable.Tools.getNearestSize(amount * wordSize.bitWidth))
    }
    fun clear() {
        this.memMap.clear()
        refreshEditableValues()
    }
    fun addEditableValue(name: String, address: Variable.Value.Hex, value: Variable.Value.Hex) {
        editableValues.removeAll(editableValues.filter { it.address == address })
        editableValues.add(MemInstance.EditableValue(name, address, value))
        editableValues.sortBy { it.address.getRawHexStr() }
    }
    fun refreshEditableValues() {
        for (value in editableValues) {
            val key = value.address.getUResized(addressSize).getRawHexStr()
            if (memMap.containsKey(key)) {
                memMap.remove(key)
            }
            memMap[key] = value
        }
    }
    fun removeEditableValue(editableValue: MemInstance.EditableValue) {
        editableValues.remove(editableValue)
    }
    fun clearEditableValues() {
        editableValues.clear()
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
        class EditableValue(val name: String, address: Variable.Value.Hex, value: Variable.Value.Hex) : MemInstance(address, Variable(value), StyleAttr.Main.Table.Mark.EDITABLE)

    }
    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"),
        BigEndian("Big Endian")
    }

}