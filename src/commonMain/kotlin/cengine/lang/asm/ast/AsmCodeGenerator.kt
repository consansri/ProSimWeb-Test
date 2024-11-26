package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.util.buffer.Buffer
import cengine.util.integer.Hex
import cengine.util.integer.Value.Companion.toValue

abstract class AsmCodeGenerator<T : AsmCodeGenerator.Section>(protected val linkerScript: LinkerScript) {

    abstract val fileSuffix: String

    val symbols: MutableSet<Symbol<T>> = mutableSetOf()

    abstract val sections: MutableList<T>

    abstract var currentSection: T

    protected abstract fun orderSectionsAndResolveAddresses()

    protected abstract fun writeFile(): ByteArray

    fun generate(ast: ASNode.Program): ByteArray {
        val statements = ast.getAllStatements()
        statements.forEach {
            it.execute()
        }

        orderSectionsAndResolveAddresses()

        // Resolve Late Evaluation
        sections.forEach {
            it.resolveReservations()
        }

        return writeFile()
    }

    private fun addLabel(name: String): Boolean {
        return symbols.add(
            Symbol.Label(
                name,
                currentSection,
                currentSection.content.size.toUInt()
            )
        )
    }

    private fun Section.resolveReservations() {
        reservations.forEach { def ->
            def.instr.nodes.filterIsInstance<ASNode.NumericExpr>().forEach { expr ->
                // Assign all Labels
                expr.assign(symbols, this, def.offset)
            }
            def.instr.type.lateEvaluation(this@AsmCodeGenerator, this, def.instr, def.offset.toInt())
        }
        reservations.clear()
    }

    fun ASNode.Statement.execute() {
        if (this.label != null) {
            val added = addLabel(label.identifier)
            if (!added) {
                this.addError("Label ${label.identifier} was already defined!")
            }
        }

        when (this) {
            is ASNode.Statement.Dir -> {
                try {
                    this.dir.type.build(this@AsmCodeGenerator, this.dir)
                } catch (e: NotImplementedError) {
                    dir.addError(e.message.toString())
                }
            }

            is ASNode.Statement.Empty -> {}

            is ASNode.Statement.Instr -> {
                instruction.nodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    it.assign(symbols, currentSection, currentSection.content.size.toUInt())
                }
                try {
                    instruction.type.resolve(this@AsmCodeGenerator, instruction)
                } catch (e: Exception) {
                    instruction.addError(e.message.toString())
                }
            }

            is ASNode.Statement.Unresolved -> {}
        }
    }

    fun getOrCreateAbsSymbolInCurrentSection(name: String, value: ULong): Boolean {
        return symbols.add(Symbol.Abs(name, currentSection, value))
    }

    fun getOrCreateSectionAndSetCurrent(name: String, type: UInt = Shdr.SHT_NULL, flags: ULong = 0U, link: T? = null, info: String? = null): T {
        currentSection = getOrCreateSection(name, type, flags, link, info)
        return currentSection
    }

    fun getOrCreateSection(name: String, type: UInt = Shdr.SHT_NULL, flags: ULong = 0U, link: T? = null, info: String? = null): T {
        val section = sections.firstOrNull { it.name == name }
        if (section != null) return section
        val created = createNewSection(name, type, flags, link, info)
        sections.add(created)
        return created
    }

    abstract fun createNewSection(name: String, type: UInt = Shdr.SHT_NULL, flags: ULong = 0U, link: T? = null, info: String? = null): T

    interface Section {
        val name: String
        var type: UInt
        var flags: ULong
        var link: Section?
        var info: String?
        var address: Hex

        val content: Buffer<*>
        val reservations: MutableList<InstrReservation>

        fun queueLateInit(instr: ASNode.Instruction, size: Int) {
            reservations.add(InstrReservation(instr, content.size.toUInt()))
            content.pad(size)
        }

        fun print(): String = "$name: size ${content.size}"

        fun isProg(): Boolean = type == Shdr.SHT_PROGBITS
        fun isText(): Boolean = isProg() && (Shdr.SHF_EXECINSTR + Shdr.SHF_ALLOC).toULong() == flags
        fun isData(): Boolean = isProg() && (Shdr.SHF_WRITE + Shdr.SHF_ALLOC).toULong() == flags
        fun isRoData(): Boolean = isProg() && Shdr.SHF_ALLOC.toULong() == flags
    }

    sealed class Symbol<T : Section>(val name: String, val section: T, var binding: Binding = Binding.LOCAL) {

        override fun equals(other: Any?): Boolean {
            if (other !is Symbol<*>) return false
            if (name != other.name) return false
            if (section != other.section) return false
            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + section.hashCode()
            return result
        }

        class Abs<T : Section>(name: String, section: T, val value: ULong) : Symbol<T>(name, section)

        class Label<T : Section>(name: String, link: T, val offset: UInt) : Symbol<T>(name, link) {
            val local = name.all { it.isDigit() }
            fun address(): Hex = section.address + offset.toValue()
        }

        enum class Binding {
            LOCAL,
            GLOBAL,
            WEAK
        }
    }

    data class InstrReservation(val instr: ASNode.Instruction, val offset: UInt)


}