package cengine.lang.asm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.util.integer.Hex
import emulator.kit.nativeLog

abstract class Disassembler {

    val decodedContent: MutableState<List<DecodedSegment>> = mutableStateOf(emptyList())

    fun disassemble(initializer: Initializer): List<DecodedSegment> {
        nativeLog("Disassemble Segments: Starting!")
        val contents = initializer.contents()
        val mapped = contents.map { (addr, content) ->
            val (data, labels) = content

            DecodedSegment(
                addr,
                labels,
                disassemble(addr, data)
            )
        }

        nativeLog("Disassemble Segments: Finished!")

        return mapped
    }

    abstract fun disassemble(startAddr: Hex, buffer: List<Hex>): List<Decoded>

    interface InstrProvider {

        fun decode(segmentAddr: Hex, offset: ULong): Decoded
    }

    data class DecodedSegment(
        val addr: Hex,
        val labels: List<Label>,
        val decodedContent: List<Decoded>,
    )

    /**
     * @param offset Offset in Segment
     */
    data class Label(
        val offset: ULong,
        val name: String,
    )

    data class Decoded(
        val offset: ULong,
        val data: Hex,
        val disassembled: String,
        val target: Hex? = null,
    )
}