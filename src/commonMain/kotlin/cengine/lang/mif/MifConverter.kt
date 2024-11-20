package cengine.lang.mif

import cengine.lang.asm.Initializer
import cengine.lang.obj.elf.ELF32File
import cengine.lang.obj.elf.ELF64File
import cengine.lang.obj.elf.ELFFile
import cengine.util.integer.*
import emulator.kit.memory.Memory
import kotlin.math.pow

class MifConverter(val wordSize: Size, val addrSize: Size, override val id: String) : Initializer {

    val depth: Double = 2.0.pow(addrSize.bitWidth)
    var addrRDX: Radix = Radix.HEX
    var dataRDX: Radix = Radix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toValue(addrSize), Bin("1".repeat(addrSize.bitWidth), addrSize), listOf(Hex("0", wordSize))))
    }

    override fun initialize(memory: Memory) {
        ranges.forEach { range ->
            range.init(memory)
        }
    }

    companion object {
        fun parseElf(file: ELFFile<*, *, *, *, *, *, *>): MifConverter {
            return when(file){
                is ELF32File -> parseElf32(file)
                is ELF64File -> parseElf64(file)
            }
        }

        private fun parseElf32(file: ELF32File): MifConverter {
            val builder = MifConverter(Size.Bit8, Size.Bit32, file.name)
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

        private fun parseElf64(file: ELF64File): MifConverter {
            val builder = MifConverter(Size.Bit8, Size.Bit64, file.name)
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

    fun addContent(startAddr: Hex, data: List<Hex>): MifConverter {
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

        fun init(memory: Memory) {
            val zero = 0U.toValue()
            if (data.all { it == zero }) return

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
            } else if (enroll) {
                data.mapIndexed { index, value ->
                    val addr = (start + index.toValue(addrSize)).toHex()
                    memory.store(addr, value)
                }
            } else {
                memory.storeArray(start.toHex(), *data.toTypedArray())
            }
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

    override fun toString(): String {
        return build()
    }

    enum class Radix {
        HEX,
        OCT,
        BIN,
        DEC,
    }
}