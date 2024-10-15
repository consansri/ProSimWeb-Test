package cengine.lang.asm.mif

import cengine.util.integer.*
import kotlin.math.pow

class MifBuilder(val wordSize: Size, val addrSize: Size) {

    val depth: Double = 2.0.pow(addrSize.bitWidth)
    var addrRDX: Radix = Radix.HEX
    var dataRDX: Radix = Radix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    private val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toValue(wordSize), Bin("1".repeat(wordSize.bitWidth), wordSize), listOf(Hex("0", wordSize))))
    }

    fun build(): String {
        val builder = StringBuilder()
        builder.append("DEPTH = ${depth}; -- The size of memory in words\n")
        builder.append("WIDTH = ${wordSize.bitWidth}; -- The size of data in bits\n")
        builder.append("ADDRESS_RADIX = ${addrRDX.name}; -- The radix for address values\n")
        builder.append("DATA_RADIX = ${dataRDX.name}; -- The radix for data values\n")
        builder.append("CONTENT BEGIN\n")

        ranges.forEach { range ->
            builder.append(range.build())
        }

        builder.append("END;\n")

        return builder.toString()
    }

    fun addContent(startAddr: Hex, data: List<Hex>): MifBuilder {
        // Find the range where the new content starts and modify accordingly
        val newEnd = startAddr + (data.size - 1).toValue(wordSize)
        val modifiedRanges = mutableListOf<Range>()

        ranges.forEach { range ->
            when {
                // Range is fully before the new content, keep it unchanged
                range.end < startAddr -> modifiedRanges.add(range)

                // Range is fully after the new content, keep it unchanged
                range.start > newEnd -> modifiedRanges.add(range)

                // The range overlaps with the new content
                else -> {
                    // Split the range into three parts: before, overlap, and after

                    // Part before the new content
                    if (range.start < startAddr) {
                        modifiedRanges.add(Range(range.start, startAddr - 1.toValue(wordSize), range.data))
                    }

                    // The new content replaces this part of the range
                    modifiedRanges.add(Range(startAddr, newEnd, data))

                    // Part after the new content
                    if (range.end > newEnd) {
                        modifiedRanges.add(Range(newEnd + 1.toValue(wordSize), range.end, range.data))
                    }
                }
            }
        }

        ranges.clear()
        ranges.addAll(modifiedRanges)
        return this
    }

    fun setAddrRadix(radix: Radix): MifBuilder {
        this.addrRDX = radix
        return this
    }

    fun setDataRadix(radix: Radix): MifBuilder {
        this.dataRDX = radix
        return this
    }

    inner class Range(val start: Value, val end: Value, val data: List<Value>, val enroll: Boolean = true) {
        // Helper function to check if a range contains a specific address
        fun contains(addr: Hex): Boolean = addr >= start && addr <= end

        // Helper function to check if a range overlaps with another range
        fun overlaps(startAddr: Hex, endAddr: Hex): Boolean =
            !(startAddr > end || endAddr < start)

        // Splits the range into parts that come before and after a specific address
        fun split(addr: Hex): Pair<Range?, Range?> {
            return if (addr > start && addr < end) {
                Pair(
                    Range(start, addr - 1.toValue(Size.Bit32), data),
                    Range(addr + 1.toValue(Size.Bit32), end, data)
                )
            } else {
                Pair(null, null)
            }
        }

        fun build(): String {
            return if (start == end) {
                // Single address
                "  ${start.addrRDX()} : ${data[0].dataRDX()};\n"
            } else if (data.size == 1) {
                // A range of addresses with a single repeating value
                "  [${start.addrRDX()}..${end.addrRDX()}] : ${data[0].dataRDX()};\n"
            } else if (enroll) {
                data.mapIndexed { index, value ->
                    "  ${(value + index.toValue(wordSize)).addrRDX()} : ${value.dataRDX()};\n"
                }.joinToString("")
            } else {
                // A range of addresses with alternating values
                val dataStr = data.joinToString(" ") { it.dataRDX() }
                "  [${start.addrRDX()}..${end.addrRDX()}] : $dataStr;\n"
            }
        }
    }

    // Word Radix Format
    private fun Value.rdx(radix: Radix): String {
        return when (radix) {
            Radix.HEX -> toHex().toRawString()
            Radix.OCT -> toOct().toRawString()
            Radix.BIN -> toBin().toRawString()
            Radix.DEC -> toUDec().toRawString()
        }
    }

    private fun Value.addrRDX(): String = rdx(addrRDX)
    private fun Value.dataRDX(): String = rdx(dataRDX)


    enum class Radix {
        HEX,
        OCT,
        BIN,
        DEC,
    }
}