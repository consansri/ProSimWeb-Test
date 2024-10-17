package cengine.lang.asm.elf

import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
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
class ExecutableELFBuilder(
    ei_class: Elf_Byte,
    ei_data: Elf_Byte,
    ei_osabi: Elf_Byte,
    ei_abiversion: Elf_Byte,
    e_machine: Elf_Half,
    e_flags: Elf_Word,
    private val linkerScript: LinkerScript
) : ELFBuilder(Ehdr.ET_EXEC,ei_class, ei_data, ei_osabi, ei_abiversion, e_machine, e_flags) {

    constructor(spec: TargetSpec, e_flags: Elf_Word = 0U) : this(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, spec.e_machine, e_flags, spec.linkerScript)

    private val segAlign get() = linkerScript.segmentAlign.toULong()

    private val phdrSegment = createAndAddSegment(p_type = Phdr.PT_PHDR, p_flags = Phdr.PF_R, p_align = segAlign)

    private val textSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_X, p_align = segAlign)

    private val dataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_W, p_align = segAlign)

    private val rodataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R, p_align = segAlign)

    override fun build(vararg statements: ASNode.Statement): ByteArray {
        statements.forEach {
            it.execute()
        }

        // Assign Sections to Segments
        sections.forEach {
            it.assignToSegment()
        }

        // Order Sections
        segments.forEach {
            sections.removeAll(it.sections)
        }
        var currentMemoryAddress = 0UL
        segments.forEach {
            sections.addAll(it.sections)
            // apply padding
            if (currentMemoryAddress % it.p_align != 0UL) {
                val padding = it.p_align - (currentMemoryAddress % it.p_align)
                currentMemoryAddress += padding
            }

            // Assign Segment Address
            it.p_vaddr = currentMemoryAddress
            currentMemoryAddress += it.p_memsz
        }
        // Set Entry Point
        entryPoint = textSegment.p_vaddr

        // Resolve Late Evaluation
        sections.forEach {
            it.resolveReservations()
        }

        return writeELFFile()
    }

    override fun Section.resolveReservations() {
        reservations.forEach { def ->
            def.instr.nodes.filterIsInstance<ASNode.NumericExpr>().forEach { expr ->
                // Assign all Labels from segments
                segments.flatMap { it.sections }.forEach {
                    expr.assign(it)
                }
            }
            def.instr.type.lateEvaluation(this@ExecutableELFBuilder, this, def.instr, def.offset.toInt())
        }
        reservations.clear()
    }

    // PRIVATE METHODS

    private fun Section.assignToSegment() {
        when {
            isText() -> textSegment.addSection(this)
            isData() -> dataSegment.addSection(this)
            isRoData() -> rodataSegment.addSection(this)
        }
    }

    private fun ASNode.Statement.execute() {
        if (this.label != null) {
            val symIndex = symTab.getOrCreate(label.identifier, currentSection)
            val sym = symTab[symIndex]
            sym.setValue(currentSection.content.size.toULong())
            sym.st_shndx = sections.indexOf(currentSection).toUShort()
            val binding = Sym.ELF_ST_BIND(sym.st_info)
            sym.st_info = Sym.ELF_ST_INFO(binding, Sym.STT_NOTYPE)
            symTab.update(sym, symIndex)
            currentSection.addLabel(this.label)
        }

        when (this) {
            is ASNode.Statement.Dir -> {
                this.dir.type.build(this@ExecutableELFBuilder, this.dir)
            }

            is ASNode.Statement.Empty -> {}

            is ASNode.Statement.Instr -> {
                instruction.nodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    it.assign(symTab)
                }
                instruction.type.resolve(this@ExecutableELFBuilder, instruction)
            }

            is ASNode.Statement.Unresolved -> {}
        }
    }

}