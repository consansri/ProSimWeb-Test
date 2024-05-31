package emulator.kit.common.memory

import emulator.kit.common.MicroSetup
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Tools.toValue

sealed class Memory: Throwable() {

    abstract val addressSize: Variable.Size
    abstract val instanceSize: Variable.Size
    abstract val initBin: String
    var endianess: Endianess = Endianess.BigEndian

    init {
        addToCurrSetup()
    }

    private fun addToCurrSetup(){
        MicroSetup.append(this)
    }

    abstract fun load(address: Variable.Value): Variable.Value
    abstract fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false)
    abstract fun clear()

    fun store(address: Variable.Value, variable: Variable, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false) {
        return store(address, variable.get(), mark, readonly)
    }

    fun storeArray(address: Variable.Value, vararg values: Variable.Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false) {
        var curraddr: Variable.Value = address
        values.forEach {
            store(curraddr.toHex(), it, mark, readonly)
            curraddr += it.size.getByteCount().toValue(addressSize)
        }
    }

    fun load(address: Variable.Value, amount: Int): Variable.Value.Bin {
        val instances = mutableListOf<String>()

        var instanceAddress = address.toHex().getUResized(addressSize)
        for (i in 0..<amount) {
            val value = load(instanceAddress)

            instances.add(value.toBin().toRawString())
            instanceAddress = (instanceAddress + Variable.Value.Hex("01", Variable.Size.Bit8())).toHex()
        }

        if (endianess == Endianess.LittleEndian) {
            instances.reverse()
        }

        return Variable.Value.Bin(instances.joinToString("") { it })
    }

    fun loadArray(address: Variable.Value, amount: Int): Array<Variable.Value.Bin> {
        val instances = mutableListOf<Variable.Value.Bin>()

        var instanceAddress = address.toHex().getUResized(addressSize)
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

    class MemoryException(override val cause: Memory, override val message: String): Exception()

    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"), BigEndian("Big Endian")
    }

    enum class InstanceType(val light: Int, val dark: Int? = null) {
        PROGRAM(0xA040A0),
        DATA(0x40A0A0),
        EDITABLE(0x222222, 0xA0A0A0),
        NOTUSED(0x777777),
        ELSE(0xA0A040);
    }
}