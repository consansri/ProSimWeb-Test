package cengine.lang.mif

import cengine.lang.mif.MifGenerator.Companion.rdx
import cengine.lang.mif.MifGenerator.Radix
import cengine.lang.mif.ast.MifNode
import cengine.lang.mif.ast.MifPsiFile
import cengine.lang.obj.elf.*
import cengine.util.integer.*
import cengine.util.integer.Value.Companion.toValue
import emulator.kit.memory.Memory
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class MifConverter(val depth: Double, val wordSize: Size) {

    constructor(wordSize: Size, addrSize: Size, id: String) : this(2.0.pow(addrSize.bitWidth), wordSize)

    val addrSize: Size = Size.nearestSize(log2(depth).roundToInt())
    var addrRDX: Radix = Radix.HEX
    var dataRDX: Radix = Radix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toValue(addrSize), Bin("1".repeat(addrSize.bitWidth), addrSize), listOf(Hex("0", wordSize))))
    }

    fun build(): String {
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

    fun addContent(startAddr: String, endAddr: String, data: String): MifConverter {
        return addContent(startAddr.rdx(addrRDX, addrSize), endAddr.rdx(addrRDX, addrSize), data.rdx(dataRDX, wordSize))
    }

    fun addContent(startAddr: Value, endAddr: Value, data: Value): MifConverter {
        // Find the range where the new content starts and modify accordingly
        val modifiedRanges = mutableListOf<Range>()

        ranges.forEach { range ->
            when {
                // Range is fully before the new content, keep it unchanged
                range.end < startAddr -> modifiedRanges.add(range)

                // Range is fully after the new content, keep it unchanged
                range.start > endAddr -> modifiedRanges.add(range)

                // The range overlaps with the new content
                else -> {
                    // Split the range into three parts: before, overlap, and after

                    // Part before the new content
                    if (range.start < startAddr) {
                        modifiedRanges.add(Range(range.start, startAddr - 1.toValue(addrSize), range.data))
                    }

                    // The new content replaces this part of the range
                    modifiedRanges.add(Range(startAddr, endAddr, listOf(data)))

                    // Part after the new content
                    if (range.end > endAddr) {
                        modifiedRanges.add(Range(endAddr + 1.toValue(addrSize), range.end, range.data))
                    }
                }
            }
        }

        ranges.clear()
        ranges.addAll(modifiedRanges)
        return this
    }

    fun addContent(startAddr: String, data: List<String>): MifConverter {
        return addContent(startAddr.rdx(addrRDX, addrSize), data.map { it.rdx(dataRDX, wordSize) })
    }

    fun addContent(startAddr: Value, data: List<Value>): MifConverter {
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

    fun setAddrRadix(radix: Radix): MifConverter {
        this.addrRDX = radix
        return this
    }

    fun setDataRadix(radix: Radix): MifConverter {
        this.dataRDX = radix
        return this
    }

    inner class Range(val start: Value, val end: Value, val data: List<Value>) {
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
            } else {
                // A range of addresses with alternating values
                val dataStr = data.joinToString(" ") { it.dataRDX() }
                "  [${start.addrRDX()}..${end.addrRDX()}] : $dataStr;\n"
            }
            return string
        }

        fun init(memory: Memory) {
            if (data.all { it.toULong() == 0UL }) return

            if (start == end) {
                memory.storeArray(start.toHex(), *data.toTypedArray())
            } else if (data.size == 1) {
                var currAddr = start
                val inc = 1U.toValue()
                while (true) {
                    memory.store(currAddr.toHex(), data.first())
                    if (currAddr == end) break
                    currAddr += inc
                }
            } else {
                memory.storeArray(start.toHex(), *data.toTypedArray())
            }
        }

        override fun toString(): String = "Range: ${start.toHex()}, ${end.toHex()}, $data -> ${build()}"

    }

    // Word Radix Format


    private fun Value.addrRDX(): String = rdx(addrRDX)
    private fun Value.dataRDX(): String = rdx(dataRDX)

    override fun toString(): String {
        return build()
    }

    companion object {

        fun parseMif(file: MifPsiFile): MifConverter {
            var currWordSize: Size? = null
            var currDepth: Double? = null
            var dataRDX = Radix.HEX
            var addrRDX = Radix.HEX

            file.program.headers.forEach {
                when (it.identifier.value) {
                    "WIDTH" -> {
                        currWordSize = Size.nearestSize(it.value.value.toInt())
                    }

                    "DEPTH" -> {
                        currDepth = it.value.value.toDouble()
                    }

                    "ADDRESS_RADIX" -> {
                        addrRDX = Radix.getRadix(it.value.value)
                    }

                    "DATA_RADIX" -> {
                        dataRDX = Radix.getRadix(it.value.value)
                    }
                }
            }

            val wordSize = currWordSize
            val depth = currDepth

            if (wordSize == null) throw Exception("Invalid or missing WIDTH!")
            if (depth == null) throw Exception("Invalid or missing DEPTH!")

            val mifConverter = MifConverter(depth, wordSize)

            mifConverter.setDataRadix(dataRDX)
            mifConverter.setAddrRadix(addrRDX)

            file.program.content?.assignments?.forEach {
                when (it) {
                    is MifNode.Assignment.Direct -> {
                        mifConverter.addContent(it.addr.value, listOf(it.data.value))
                    }

                    is MifNode.Assignment.ListOfValues -> {
                        mifConverter.addContent(it.addr.value, it.data.map { it.value })
                    }

                    is MifNode.Assignment.SingleValueRange -> {
                        mifConverter.addContent(it.valueRange.first.value, it.valueRange.last.value, it.data.value)
                    }
                }
            }

            return mifConverter
        }

        fun parseElf(file: ELFFile): MifConverter {
            return when (file) {
                is ELF32File -> parseElf32(file)
                is ELF64File -> parseElf64(file)
            }
        }

        private fun parseElf32(file: ELF32File): MifConverter {
            val builder = MifConverter(Size.Bit8, Size.Bit32, file.name)
            val bytes = file.content

            file.programHeaders.forEach {
                if (it !is ELF32_Phdr) return@forEach
                val startAddr = it.p_vaddr.toValue()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toUByte().toValue() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }

        private fun parseElf64(file: ELF64File): MifConverter {
            val builder = MifConverter(Size.Bit8, Size.Bit64, file.name)
            val bytes = file.content

            file.programHeaders.forEach {
                if (it !is ELF64_Phdr) return@forEach
                val startAddr = it.p_vaddr.toValue()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toUByte().toValue() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }
    }
}