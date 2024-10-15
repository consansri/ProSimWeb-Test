package cengine.lang.asm.elf

import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.util.ByteBuffer
import cengine.util.Endianness

class BinaryBuilder(private val spec: TargetSpec) {
    val endianness: Endianness
        get() = when (spec.ei_data) {
            E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
            E_IDENT.ELFDATA2MSB -> Endianness.BIG
            else -> {
                throw RelocatableELFBuilder.ELFBuilderException("Invalid Data Encoding ${spec.ei_data}.")
            }
        }


    private var buffer: ByteBuffer = ByteBuffer(endianness)

    fun build(vararg statements: ASNode.Statement): ByteArray {

        // TODO

        return buffer.toByteArray()
    }

}