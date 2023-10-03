package emulator.kit.common

import emulator.kit.types.Variable

/**
 * The [Transcript] contains two separate views of the program. The [compiled] and [disassembled] content.
 * To add content with custom formatting the [Row] class can be specified by each architecture.
 *
 * @constructor the headers ([compiledHeaders] and [disassembledHeaders]) of both views need to be defined in the constructor.
 */
class Transcript(private val compiledHeaders: List<String> = listOf(), private val disassembledHeaders: List<String> = listOf()) {

    val disassembled = mutableListOf<Row>()
    val compiled = mutableListOf<Row>()

    fun clear() {
        disassembled.clear()
        compiled.clear()
    }

    fun clear(type: Type) {
        when (type) {
            Type.COMPILED -> compiled.clear()
            Type.DISASSEMBLED -> disassembled.clear()
        }
    }

    fun deactivated(): Boolean {
        return compiledHeaders.isEmpty() && disassembledHeaders.isEmpty()
    }

    fun addRow(type: Type, row: Row) {
        disassembled.add(row)
    }

    fun addContent(type: Type, content: List<Row>) {
        when (type) {
            Type.COMPILED -> compiled.addAll(content)
            Type.DISASSEMBLED -> disassembled.addAll(content)
        }
    }

    fun getHeaders(type: Type): List<String> {
        return when (type) {
            Type.COMPILED -> compiledHeaders
            Type.DISASSEMBLED -> disassembledHeaders
        }
    }

    fun getContent(type: Type): List<Row> {
        return when (type) {
            Type.COMPILED -> compiled
            Type.DISASSEMBLED -> disassembled
        }
    }

    abstract class Row(vararg addresses: Variable.Value) {
        private val addresses = addresses.toMutableList()
        private var height = 1
        fun addAddresses(vararg addresses: Variable.Value) {
            this.addresses.addAll(addresses)
        }

        fun changeHeight(height: Int) {
            if (height > 1) {
                this.height = height
            }
        }

        fun getAddresses(): List<Variable.Value> = addresses

        fun getHeight(): Int = height

        abstract fun getContent(): List<Entry>
        data class Entry(val orientation: Orientation, val content: String)
        enum class Orientation {
            LEFT,
            CENTER,
            RIGHT
        }
    }

    enum class Type {
        COMPILED,
        DISASSEMBLED
    }


}