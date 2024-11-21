package cengine.lang.obj.elf

import cengine.lang.asm.ast.TargetSpec
import cengine.psi.core.*
import kotlin.experimental.*

/**
 * ELF File
 *
 * 1. [Ehdr]
 * 2. [Phdr]s
 * 3. [Section]s
 * 4. [Shdr]s
 */
class ExecELFGenerator(
    ei_class: Elf_Byte,
    ei_data: Elf_Byte,
    ei_osabi: Elf_Byte,
    ei_abiversion: Elf_Byte,
    e_machine: Elf_Half,
    e_flags: Elf_Word,
    linkerScript: LinkerScript
) : ELFGenerator(Ehdr.ET_EXEC, ei_class, ei_data, ei_osabi, ei_abiversion, e_machine, e_flags, linkerScript) {

    constructor(spec: TargetSpec<*>, e_flags: Elf_Word = 0U) : this(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, spec.e_machine, e_flags, spec.linkerScript)

    private val segAlign get() = linkerScript.segmentAlign.toULong()

    private val phdrSegment = createAndAddSegment(p_type = Phdr.PT_PHDR, p_flags = Phdr.PF_R, p_align = segAlign)

    private val textSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_X, p_align = segAlign)

    private val dataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_W, p_align = segAlign)

    private val rodataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R, p_align = segAlign)

    override fun orderSectionsAndResolveAddresses() {
        // Assign Sections to Segments
        sections.forEach {
            it.assignToSegment()
        }

        // Order Sections
        segments.forEach {
            sections.removeAll(it.sections)
        }
        var currentMemoryAddress = 0UL
        segments.forEach { segment ->
            sections.addAll(segment.sections)
            // apply padding
            if (currentMemoryAddress % segment.p_align != 0UL) {
                val padding = segment.p_align - (currentMemoryAddress % segment.p_align)
                currentMemoryAddress += padding
            }

            // Assign Segment Address
            segment.p_vaddr = currentMemoryAddress
            segment.p_paddr = currentMemoryAddress
            currentMemoryAddress += segment.p_memsz
        }

        linkerScript.textStart?.toULong()?.let {
            textSegment.p_vaddr = it
        }

        linkerScript.dataStart?.toULong()?.let {
            dataSegment.p_vaddr = it
        }

        linkerScript.rodataStart?.toULong()?.let {
            rodataSegment.p_vaddr = it
        }

        // Set Entry Point
        entryPoint = textSegment.p_vaddr
    }

    // PRIVATE METHODS

    private fun ELFSection.assignToSegment() {
        when {
            isText() -> textSegment.addSection(this)
            isData() -> dataSegment.addSection(this)
            isRoData() -> rodataSegment.addSection(this)
        }
    }

}