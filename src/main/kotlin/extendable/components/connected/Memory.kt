package extendable.components.connected

import extendable.components.types.ByteValue
import tools.TypeTools
import kotlin.math.pow

class Memory(private val addressBitWidth: Int, private val initialValue: String, private val wordByteCount: Int) {
    private val globalSize: Double // in Byte

    private var memList: MutableList<DMemInstance>

    init {
        require(addressBitWidth % 4 == 0 && addressBitWidth > 0) { "Memory: Address Bit width must be a positive multiple of 4." }
        this.globalSize = 2.0.pow(addressBitWidth.toDouble())
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    private fun setup() {
        saveDec(0.0, initialValue)
        saveDec(getAddressMax(), initialValue)
    }

    fun saveBin(address: Double, binString: String): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.byteValue.setBin(binString)
                return true
            }
        }
        memList += DMemInstance(address, ByteValue(initialValue, wordByteCount))
        saveDec(address, binString)
        return false
    }

    fun saveHex(address: Double, hexString: String): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.byteValue.setHex(hexString)
                return true
            }
        }
        memList += DMemInstance(address, ByteValue(initialValue, wordByteCount))
        saveDec(address, hexString)
        return false
    }

    fun saveDec(address: Double, decString: String): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.byteValue.setDec(decString)
                return true
            }
        }
        memList += DMemInstance(address, ByteValue(initialValue, wordByteCount))
        saveDec(address, decString)
        return false
    }

    fun load(address: Double): ByteValue? {
        for (instance in memList) {
            if (instance.address == address) {
                return instance.byteValue
            }
        }
        return null
    }

    fun clear() {
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    fun getMemList(): List<DMemInstance> {
        return memList
    }

    fun getAddressMax(): Double {
        return globalSize - 1
    }

    fun getInitialBinary(): ByteValue {
        return ByteValue(initialValue, wordByteCount)
    }

    fun getAddressHexString(address: Double): String {
        return TypeTools.getHexString(address.toLong(), addressBitWidth / 4)
    }

    data class DMemInstance(val address: Double, var byteValue: ByteValue)


}