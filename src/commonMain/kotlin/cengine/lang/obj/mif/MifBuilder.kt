package cengine.lang.obj.mif

import cengine.lang.obj.elf.ELF32File
import cengine.lang.obj.elf.ELF64File
import cengine.lang.obj.elf.ELFFile
import cengine.util.integer.*
import emulator.kit.nativeLog
import kotlin.math.pow

class MifBuilder(val wordSize: Size, val addrSize: Size) {

    val depth: Double = 2.0.pow(addrSize.bitWidth)
    var addrRDX: Radix = Radix.HEX
    var dataRDX: Radix = Radix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    private val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toValue(addrSize), Bin("1".repeat(addrSize.bitWidth), addrSize), listOf(Hex("0", wordSize))))
    }

    companion object {

        fun parseElf(file: ELFFile<*, *, *, *, *, *, *>): MifBuilder {
            return when (file) {
                is ELF32File -> parseElf(file)
                is ELF64File -> parseElf(file)
            }
        }

        fun parseElf(file: ELF32File): MifBuilder {
            val builder = MifBuilder(Size.Bit8, Size.Bit32)
            val bytes = file.content

            file.programHeaders.forEach {
                val startAddr = it.p_vaddr.toValue()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toUByte().toValue() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }

        fun parseElf(file: ELF64File): MifBuilder {
            val builder = MifBuilder(Size.Bit8, Size.Bit64)
            val bytes = file.content

            file.programHeaders.forEach {
                val startAddr = it.p_vaddr.toValue()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toUByte().toValue() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }
    }

    fun build(): String {

        nativeLog(
            "Ranges: ${
                ranges.joinToString("") {
                    "\n\t" + it.toString()
                }
            }"
        )
        val builder = StringBuilder()
        builder.append("DEPTH = ${depth.toString().takeWhile { it != '.' }}; -- The size of memory in words\n")
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
        if (data.isEmpty()) return this
        val newEnd = startAddr + (data.size - 1).toValue(addrSize)
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
                        modifiedRanges.add(Range(range.start, startAddr - 1.toValue(addrSize), range.data))
                    }

                    // The new content replaces this part of the range
                    modifiedRanges.add(Range(startAddr, newEnd, data))

                    // Part after the new content
                    if (range.end > newEnd) {
                        modifiedRanges.add(Range(newEnd + 1.toValue(addrSize), range.end, range.data))
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
            val string = if (start == end) {
                // Single address
                "  ${start.addrRDX()} : ${data[0].dataRDX()};\n"
            } else if (data.size == 1) {
                // A range of addresses with a single repeating value
                "  [${start.addrRDX()}..${end.addrRDX()}] : ${data[0].dataRDX()};\n"
            } else if (enroll) {
                data.mapIndexed { index, value ->
                    "  ${(start + index.toValue(addrSize)).addrRDX()} : ${value.dataRDX()};\n"
                }.joinToString("")
            } else {
                // A range of addresses with alternating values
                val dataStr = data.joinToString(" ") { it.dataRDX() }
                "  [${start.addrRDX()}..${end.addrRDX()}] : $dataStr;\n"
            }
            return string
        }

        override fun toString(): String = "Range: ${start.toHex()}, ${end.toHex()}, $data -> ${build()}"

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