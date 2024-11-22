package cengine.lang.mif.ast

import cengine.editor.annotation.Annotation
import cengine.lang.asm.Disassembler
import cengine.lang.asm.Initializer
import cengine.lang.mif.MifGenerator.Companion.rdx
import cengine.lang.mif.MifGenerator.Radix
import cengine.lang.mif.MifLang
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory
import emulator.kit.nativeError
import emulator.kit.nativeLog
import kotlin.math.log2
import kotlin.math.roundToInt

class MifPsiFile(
    override val file: VirtualFile, var program: MifNode.Program,
) : PsiFile, Initializer {

    override val lang: MifLang get() = MifLang
    override val children: List<PsiElement>
        get() = program.children

    override val annotations: List<Annotation>
        get() = program.annotations

    override var parent: PsiElement? = null
    override val additionalInfo: String = "MifFile"
    override var range: IntRange = (children.minOf { it.range.first })..(children.maxOf { it.range.last })

    override val id: String = file.name

    override fun update() {
        // Reparse the file and update children
        val newFile = lang.psiParser.parse(file)
        program = newFile.program
        range = newFile.range
    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    override fun initialize(memory: Memory) {
        analyzeHeader { addrSize, wordSize, addrRDX, dataRDX, assignments ->
            assignments.filter {
                it !is MifNode.Assignment.SingleValueRange || it.data.value.rdx(dataRDX, wordSize).toHex() != 0U.toValue()
            }.forEach { assignment ->
                when (assignment) {
                    is MifNode.Assignment.Direct -> {
                        val startAddr = assignment.addr.value
                        memory.store(startAddr.rdx(addrRDX, addrSize).toHex(), assignment.data.value.rdx(dataRDX, wordSize))
                    }

                    is MifNode.Assignment.ListOfValues -> {
                        val startAddr = assignment.addr.value.rdx(addrRDX, addrSize).toHex()
                        val values = assignment.data.map { it.value.rdx(dataRDX, wordSize) }
                        memory.storeArray(startAddr, *values.toTypedArray())
                    }

                    is MifNode.Assignment.SingleValueRange -> {
                        val value = assignment.data.value.rdx(dataRDX, wordSize)
                        val startAddr = assignment.valueRange.first.value.rdx(addrRDX, addrSize).toHex()
                        val endAddr = assignment.valueRange.last.value.rdx(addrRDX, addrSize).toHex()
                        val length = (endAddr - startAddr).toULong().toInt() + 1
                        if (length < 0) {
                            nativeError("MifPsiFile ${file.name}: Length of ${assignment::class.simpleName} exceeds ${Int.MAX_VALUE} -> $length = $endAddr - $startAddr")
                            return@analyzeHeader
                        }
                        memory.storeArray(startAddr, *Array(length) { value })
                    }
                }
            }
        }
    }

    override fun contents(): Map<Hex, Pair<List<Hex>, List<Disassembler.Label>>> {
        nativeLog("contents()")
        val contents = mutableMapOf<Hex, Pair<List<Hex>, List<Disassembler.Label>>>()
        analyzeHeader { addrSize, wordSize, addrRDX, dataRDX, assignments ->
            contents.putAll(assignments.filter {
                it !is MifNode.Assignment.SingleValueRange || it.data.value.rdx(dataRDX, wordSize).toHex() != 0U.toValue()
            }.associate { assignment ->
                when (assignment) {
                    is MifNode.Assignment.Direct -> {
                        val startAddr = assignment.addr.value.rdx(addrRDX, addrSize).toHex()
                        val value = assignment.data.value.rdx(dataRDX, wordSize).toHex()
                        startAddr to (listOf(value) to emptyList())
                    }

                    is MifNode.Assignment.ListOfValues -> {
                        val startAddr = assignment.addr.value.rdx(addrRDX, addrSize).toHex()
                        val value = assignment.data.map { it.value.rdx(dataRDX, wordSize).toHex() }
                        startAddr to (value to emptyList())
                    }

                    is MifNode.Assignment.SingleValueRange -> {
                        val value = assignment.data.value.rdx(dataRDX, wordSize).toHex()
                        val startAddr = assignment.valueRange.first.value.rdx(addrRDX, addrSize).toHex()
                        val endAddr = assignment.valueRange.last.value.rdx(addrRDX, addrSize).toHex()
                        val length = (endAddr - startAddr).toULong().toInt() + 1
                        if (length < 0) {
                            nativeError("MifPsiFile ${file.name}: Length of ${assignment::class.simpleName} exceeds ${Int.MAX_VALUE} -> $length = $endAddr - $startAddr")
                            return@analyzeHeader
                        }
                        startAddr to (List(length) { value } to emptyList())
                    }
                }
            })
        }
        return contents
    }

    fun analyzeHeader(result: (addrSize: Size, wordSize: Size, addrRDX: Radix, dataRDX: Radix, assignments: List<MifNode.Assignment>) -> Unit) {
        var currWordSize: Size? = null
        var currDepth: Double? = null
        var dataRDX = Radix.HEX
        var addrRDX = Radix.HEX

        program.headers.forEach {
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

        val addrSize = Size.nearestSize(log2(depth).roundToInt())

        result(addrSize, wordSize, addrRDX, dataRDX, program.content?.assignments?.toList() ?: emptyList())
    }

}