package extendable.components.connected

import StyleConst
import extendable.components.types.ByteValue
import tools.DebugTools

class Memory(private val addressSize: ByteValue.Size, private val initBin: String, private val wordSize: ByteValue.Size, private val endianess: Endianess) {
    private var memMap: MutableMap<String, DMemInstance> = mutableMapOf()

    init {
        setup()
    }

    private fun setup() {
        save(ByteValue.Type.Hex("0", addressSize), getInitialBinary())
        save(getAddressMax(), getInitialBinary())
    }

    fun save(address: ByteValue.Type, byteValue: ByteValue, mark: String = StyleConst.CLASS_TABLE_MARK_ELSE) {
        val wordList = byteValue.get().toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.LittleEndian) {
            wordList.reversed()
        }

        val hexAddress = address.toBin().getResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("saving...  ${byteValue.get().toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]

            if (instance != null) {
                instance.byteValue.setHex(word.toString())
                instance.mark = mark
            } else {
                val newInstance = DMemInstance(hexAddress, ByteValue(initBin, wordSize), mark)
                newInstance.byteValue.setHex(word.toString())
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
        }
    }

    fun save(address: ByteValue.Type, value: ByteValue.Type, mark: String = StyleConst.CLASS_TABLE_MARK_ELSE) {
        val wordList = value.toHex().getRawHexStr().reversed().chunked(wordSize.byteCount * 2) { it.reversed() }

        if (endianess == Endianess.LittleEndian) {
            wordList.reversed()
        }

        var hexAddress = address.toBin().getResized(addressSize).toHex()
        if (DebugTools.ARCH_showMemoryInfo) {
            console.log("saving...  ${value.toHex().getRawHexStr()}, $wordList to ${hexAddress.getRawHexStr()}")
        }
        for (word in wordList) {
            val instance = memMap[hexAddress.getRawHexStr()]
            if (instance != null) {
                instance.byteValue.setHex(word.toString())
                instance.mark = mark
            } else {
                val newInstance = DMemInstance(hexAddress, ByteValue(initBin, wordSize), mark)
                newInstance.byteValue.setHex(word.toString())
                memMap[hexAddress.getRawHexStr()] = newInstance
            }
            hexAddress = (hexAddress + ByteValue.Type.Hex("1")).toHex()
        }
    }

    fun load(address: ByteValue.Type): ByteValue {
        val value = memMap.get(address.toHex().getRawHexStr())?.byteValue
        if (value != null) {
            return value
        } else {
            return getInitialBinary()
        }
    }

    fun load(address: ByteValue.Type, amount: Int): ByteValue {
        val instances = mutableListOf<String>()

        var instanceAddress = address.toBin()
        for (i in 0 until amount) {
            val instance = load(instanceAddress)
            instances.add(instance.get().toHex().getRawHexStr())
            instanceAddress = (instanceAddress + ByteValue.Type.Binary("1", addressSize)).toBin()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return ByteValue(instances.joinToString("") { it }, ByteValue.Tools.getNearestSize(amount * wordSize.bitWidth))
    }

    fun clear() {
        this.memMap.clear()
        setup()
    }

    fun getMemMap(): Map<String, DMemInstance> {
        return memMap
    }

    fun getAddressMax(): ByteValue.Type {
        return ByteValue.Type.Hex("0", addressSize).getBiggest()
    }

    fun getInitialBinary(): ByteValue {
        return ByteValue(initBin, wordSize)
    }

    fun getAddressSize(): ByteValue.Size {
        return addressSize
    }

    fun getWordSize(): ByteValue.Size {
        return wordSize
    }

    data class DMemInstance(val address: ByteValue.Type.Hex, var byteValue: ByteValue, var mark: String = "")

    enum class Endianess {
        LittleEndian,
        BigEndian
    }

}