package cengine.lang.mif.ast

import cengine.editor.annotation.Annotation
import cengine.lang.asm.Disassembler
import cengine.lang.asm.Initializer
import cengine.lang.mif.MifGenerator.Radix
import cengine.lang.mif.MifLang
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.util.integer.Size
import cengine.util.newint.BigInt
import cengine.util.newint.IntNumber
import cengine.util.newint.IntNumber.Companion.parseAnyUInt
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory
import emulator.kit.nativeError
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

    override fun initialize(memory: Memory<*, *>) {
        analyzeHeader { addrSize, wordSize, addrRDX, dataRDX, assignments ->
            assignments.filter { assignment ->
                assignment !is MifNode.Assignment.RepeatingValueRange || assignment.data.all { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount).toBigInt() != BigInt.ZERO }
            }.forEach { assignment ->
                when (assignment) {
                    is MifNode.Assignment.Direct -> {
                        val startAddr = assignment.addr.value
                        memory.storeEndianAware(BigInt.parse(startAddr, addrRDX.radix), assignment.data.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount))
                    }

                    is MifNode.Assignment.ListOfValues -> {
                        val startAddr = BigInt.parse(assignment.addr.value, addrRDX.radix)
                        val values = assignment.data.map { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount) }
                        memory.storeArray(startAddr, values)
                    }

                    is MifNode.Assignment.RepeatingValueRange -> {
                        val values = assignment.data.map { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount) }
                        val startAddr = BigInt.parse(assignment.valueRange.first.value, addrRDX.radix)
                        val endAddr = BigInt.parse(assignment.valueRange.last.value, addrRDX.radix)
                        val length = (endAddr - startAddr) + 1
                        if (length < 0 || length > Int.MAX_VALUE) {
                            nativeError("MifPsiFile ${file.name}: Length of ${assignment::class.simpleName} exceeds 0..${Int.MAX_VALUE} -> $length = $endAddr - $startAddr")
                            return@analyzeHeader
                        }
                        val initArray = try {
                            List(length.toInt()) {
                                values[it % values.size]
                            }
                        } catch (e: Exception) {
                            nativeError("Couldn't convert $length to exact Int (${length.value.intValue(false)})")
                            emptyList()
                        }
                        memory.storeArray(startAddr, initArray)
                    }
                }
            }
        }
    }

    override fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>> {
        val contents = mutableMapOf<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>>()
        analyzeHeader { addrSize, wordSize, addrRDX, dataRDX, assignments ->
            contents.putAll(assignments.filter { assignment ->
                assignment !is MifNode.Assignment.RepeatingValueRange || assignment.data.all { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount).toBigInt() != BigInt.ZERO }
            }.associate { assignment ->
                when (assignment) {
                    is MifNode.Assignment.Direct -> {
                        val startAddr = BigInt.parse(assignment.addr.value, addrRDX.radix)
                        val value = BigInt.parse(assignment.data.value, dataRDX.radix)
                        startAddr to (listOf(value) to emptyList())
                    }

                    is MifNode.Assignment.ListOfValues -> {
                        val startAddr = BigInt.parse(assignment.addr.value, addrRDX.radix)
                        val value = assignment.data.map { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount) }
                        startAddr to (value to emptyList())
                    }

                    is MifNode.Assignment.RepeatingValueRange -> {
                        val values = assignment.data.map { it.value.parseAnyUInt(dataRDX.radix, wordSize.byteCount) }
                        val startAddr = BigInt.parse(assignment.valueRange.first.value, addrRDX.radix)
                        val endAddr = BigInt.parse(assignment.valueRange.last.value, addrRDX.radix)
                        val length = (endAddr - startAddr) + 1
                        if (length < 0) {
                            nativeError("MifPsiFile ${file.name}: Length of ${assignment::class.simpleName} exceeds ${Int.MAX_VALUE} -> $length = $endAddr - $startAddr")
                            return@analyzeHeader
                        }
                        startAddr to (List(length.toInt()) {
                            values[it % values.size]
                        } to emptyList())
                    }
                }
            })
        }
        return contents
    }

    private fun analyzeHeader(result: (addrSize: Size, wordSize: Size, addrRDX: Radix, dataRDX: Radix, assignments: List<MifNode.Assignment>) -> Unit) {
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