package emulator.kit.common.memory

import emulator.kit.types.Variable
import emulator.kit.types.Variable.Tools.toValue

abstract class Memory {

    abstract val addressSize: Variable.Size
    abstract val instanceSize: Variable.Size
    abstract val initBin: String
    var endianess: Endianess = Endianess.BigEndian
    var entrysInRow: Int = 16
        set(value) {
            field = value
            getAllInstances().forEach { it.reMap(value) }
        }

    abstract fun load(address: Variable.Value.Hex): Variable.Value.Bin
    abstract fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): MemAccessResult
    abstract fun getAllInstances(): List<MemInstance>
    abstract fun clear()

    fun store(address: Variable.Value, variable: Variable, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): MemAccessResult {
        return store(address, variable.get(), mark, readonly)
    }

    fun storeArray(address: Variable.Value, vararg values: Variable.Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false) {
        var curraddr = address
        values.forEach {
            store(curraddr, it, mark, readonly)
            curraddr += it.size.getByteCount().toValue(addressSize)
        }
    }

    fun load(address: Variable.Value.Hex, amount: Int): Variable.Value.Bin {
        val instances = mutableListOf<String>()

        var instanceAddress = address.getUResized(addressSize)
        for (i in 0..<amount) {
            val value = load(instanceAddress)

            instances.add(value.toBin().getRawBinStr())
            instanceAddress = (instanceAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return Variable.Value.Bin(instances.joinToString("") { it })
    }

    fun loadArray(address: Variable.Value.Hex, amount: Int): Array<Variable.Value.Bin> {
        val instances = mutableListOf<Variable.Value.Bin>()

        var instanceAddress = address.getUResized(addressSize)
        for (i in 0..<amount) {
            val value = load(instanceAddress)
            instances.add(value.toBin())
            instanceAddress = (instanceAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }

        return instances.toTypedArray()
    }

    fun getInitialBinary(): Variable {
        return Variable(initBin, instanceSize)
    }

    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"), BigEndian("Big Endian")
    }

    enum class MemAccessResult {
        HIT, MISS
    }

    enum class InstanceType(val light: Int, val dark: Int? = null) {
        PROGRAM(0xA040A0), DATA(0x40A0A0), EDITABLE(0x222222, 0xA0A0A0), NOTUSED(0x777777), ELSE(0xA0A040);
    }

    open class MemInstance(val address: Variable.Value.Hex, var variable: Variable, var mark: InstanceType = InstanceType.ELSE, val readonly: Boolean = false, entrysInRow: Int) {
        private var entrysInRow = entrysInRow
            set(value) {
                field = value
                addrRelevantForOffset = address.getRawHexStr().substring(address.getRawHexStr().length - entrysInRow / 16 - 1).toIntOrNull(16) ?: throw Exception("couldn't extract relevant address part (from ${address.getRawHexStr()}) for offset calculation!")
            }

        private var addrRelevantForOffset: Int = address.getRawHexStr().substring(address.getRawHexStr().length - entrysInRow / 16 - 1).toIntOrNull(16) ?: throw Exception("couldn't extract relevant address part (from ${address.getRawHexStr()}) for offset calculation!")
            set(value) {
                field = value
                offset = addrRelevantForOffset % entrysInRow
                row = (address - Variable.Value.Hex(offset.toString(16), Variable.Size.Bit8())).toHex()
            }

        var offset: Int = addrRelevantForOffset % entrysInRow
        var row: Variable.Value.Hex = (address - Variable.Value.Hex(offset.toString(16), Variable.Size.Bit8())).toHex()

        fun reMap(entrysInRow: Int) {
            this.entrysInRow = entrysInRow
        }

        class EditableValue(address: Variable.Value.Hex, value: Variable.Value.Hex, entrysInRow: Int) : MemInstance(address, Variable(value), InstanceType.EDITABLE, entrysInRow = entrysInRow)

        override fun toString(): String {
            return variable.get().toHex().getRawHexStr()
        }
    }

}