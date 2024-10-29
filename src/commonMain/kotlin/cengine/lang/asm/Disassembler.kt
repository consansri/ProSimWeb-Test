package cengine.lang.asm

import androidx.compose.runtime.MutableState
import cengine.lang.obj.elf.*
import cengine.util.ByteBuffer
import cengine.util.integer.Hex
import cengine.util.integer.toValue

interface Disassembler {

    val decoded: MutableState<List<DecodedSegment>>

    fun disassemble(byteBuffer: ByteBuffer, startAddr: Hex): List<Decoded>

    fun disassemble(elfFile: ELFFile<*, *, *, *, *, *, *>) {
        decoded.value = when (elfFile) {
            is ELF32File -> disassemble(elfFile)
            is ELF64File -> disassemble(elfFile)
        }
    }

    fun disassemble(elfFile: ELF32File): List<DecodedSegment> {
        return elfFile.segmentToSectionGroup.filterIsInstance<ELF32File.ELF32Segment>().map { group ->

            val labels = group.sections.flatMap {shdr: ELF32_Shdr ->
                val sectionIndex = elfFile.sectionHeaders.indexOf(shdr)
                val symbols = elfFile.symbolTable?.filter {
                    it.st_shndx == sectionIndex.toUShort()
                } ?: return@flatMap emptyList<Label>()

                symbols.filter {
                    Sym.ELF_ST_TYPE(it.st_info) == Sym.STT_NOTYPE
                }.mapNotNull { sym ->
                    val offset = shdr.sh_offset - group.phdr.p_offset + sym.st_value
                    val name = elfFile.getStrTabString(sym.st_name.toInt()) ?: "[invalid]"
                    if (name.isEmpty()) return@mapNotNull null
                    Label(offset.toULong(), name)
                }
            }

            val start = group.phdr.p_offset
            val end = start + group.phdr.p_filesz
            val byteArray = elfFile.content.copyOfRange(start.toInt(), end.toInt())
            val startAddr = group.phdr.p_vaddr.toValue()
            val byteBuffer = ByteBuffer(elfFile.e_ident.endianness, byteArray)
            DecodedSegment(startAddr, labels, disassemble(byteBuffer, startAddr))
        }
    }

    fun disassemble(elfFile: ELF64File): List<DecodedSegment> {
        return elfFile.segmentToSectionGroup.filterIsInstance<ELF64File.ELF64Segment>().map { group ->
            val labels = group.sections.flatMap { shdr: ELF64_Shdr ->
                val sectionIndex = elfFile.sectionHeaders.indexOf(shdr)
                val symbols = elfFile.symbolTable?.filter {
                    it.st_shndx == sectionIndex.toUShort()
                } ?: return@flatMap emptyList<Label>()

                symbols.filter {
                    Sym.ELF_ST_TYPE(it.st_info) == Sym.STT_NOTYPE
                }.mapNotNull { sym ->
                    val offset = shdr.sh_offset - group.phdr.p_offset + sym.st_value
                    val name = elfFile.getStrTabString(sym.st_name.toInt()) ?: "[invalid]"
                    if (name.isEmpty()) return@mapNotNull null
                    Label(offset, name)
                }
            }

            val start = group.phdr.p_offset
            val end = start + group.phdr.p_filesz
            val byteArray = elfFile.content.copyOfRange(start.toInt(), end.toInt())
            val startAddr = group.phdr.p_vaddr.toValue()
            val byteBuffer = ByteBuffer(elfFile.e_ident.endianness, byteArray)
            DecodedSegment(startAddr, labels, disassemble(byteBuffer, startAddr))
        }
    }


    data class DecodedSegment(
        val addr: Hex,
        val labels: List<Label>,
        val decodedContent: List<Decoded>
    )

    /**
     * @param offset Offset in Segment
     */
    data class Label(
        val offset: ULong,
        val name: String
    )

    data class Decoded(
        val offset: ULong,
        val data: Hex,
        val disassembled: String,
        val target: Hex? = null
    )
}