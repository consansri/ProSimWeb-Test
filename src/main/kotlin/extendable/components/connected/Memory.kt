package extendable.components.connected

import StyleConst
import extendable.components.types.MutVal
import tools.DebugTools

class Memory(private val addressSize: MutVal.Size, private val initBin: String, private val wordSize: MutVal.Size, private val endianess: Endianess) {
    private var memMap: MutableMap<String, DMemInstance> = mutableMapOf()
    private var editableValues: MutableList<DMemInstance.EditableValue> = mutableListOf()

    fun save(address: MutVal.Value, mutVal: MutVal, mark: String = StyleConst.CLASS_TABLE_MARK_ELSE) {
        val wordList = mutVal.get().toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.LittleEndian) {
            wordList.reversed()
        }

        val hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("saving...  ${mutVal.get().toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]

            if (instance != null) {
                instance.mutVal.setHex(word.toString())
                instance.mark = mark
            } else {
                val newInstance = DMemInstance(hexAddress, MutVal(initBin, wordSize), mark)
                newInstance.mutVal.setHex(word.toString())
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
        }
    }

    fun save(address: MutVal.Value, value: MutVal.Value, mark: String = StyleConst.CLASS_TABLE_MARK_ELSE) {
        val wordList = value.toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.LittleEndian) {
            wordList.reversed()
        }

        var hexAddress = address.toBin().getUResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("saving...  ${value.toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]
            if (instance != null) {
                instance.mutVal.setHex(word.toString())
                instance.mark = mark
            } else {
                val newInstance = DMemInstance(hexAddress, MutVal(initBin, wordSize), mark)
                newInstance.mutVal.setHex(word.toString())
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + MutVal.Value.Hex("1")).toHex()
        }
    }

    fun load(address: MutVal.Value): MutVal {
        val value = memMap.get(address.toHex().getRawHexStr())?.mutVal
        if (value != null) {
            return value
        } else {
            return getInitialBinary()
        }
    }

    fun load(address: MutVal.Value, amount: Int): MutVal {
        val instances = mutableListOf<String>()

        var instanceAddress = address.toBin()
        for (i in 0 until amount) {
            val instance = load(instanceAddress)
            instances.add(instance.get().toBin().getRawBinaryStr())
            instanceAddress = (instanceAddress + MutVal.Value.Binary("1", addressSize)).toBin()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return MutVal(instances.joinToString("") { it }, MutVal.Tools.getNearestSize(amount * wordSize.bitWidth))
    }

    fun clear() {
        this.memMap.clear()
        refreshEditableValues()
    }

    fun addEditableValue(name: String, address: MutVal.Value.Hex, value: MutVal.Value.Binary) {
        editableValues.add(DMemInstance.EditableValue(name, address, value))
    }

    fun refreshEditableValues() {
        for (value in editableValues) {
            val key = value.address.getResized(addressSize).getRawHexStr()
            if (memMap.containsKey(key)) {
                memMap.remove(key)
            }
            memMap[key] = value
        }
    }

    fun removeEditableValue(editableValue: DMemInstance.EditableValue) {
        editableValues.remove(editableValue)
    }

    fun getMemMap(): Map<String, DMemInstance> {
        return memMap
    }

    fun getAddressMax(): MutVal.Value {
        return MutVal.Value.Hex("0", addressSize).getBiggest()
    }

    fun getDefaultInstances(): List<DMemInstance.EditableValue> {
        return editableValues
    }

    fun getInitialBinary(): MutVal {
        return MutVal(initBin, wordSize)
    }

    fun getAddressSize(): MutVal.Size {
        return addressSize
    }

    fun getWordSize(): MutVal.Size {
        return wordSize
    }

    open class DMemInstance(val address: MutVal.Value.Hex, var mutVal: MutVal, var mark: String = "") {
        class EditableValue(val name: String, address: MutVal.Value.Hex, value: MutVal.Value.Binary) : DMemInstance(address, MutVal(value), "dcf-mark-defaultvalue")

    }

    enum class Endianess {
        LittleEndian,
        BigEndian
    }

}