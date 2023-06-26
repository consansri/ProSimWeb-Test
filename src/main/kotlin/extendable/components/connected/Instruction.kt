package extendable.components.connected

import extendable.Architecture
import extendable.components.types.ByteValue
import extendable.components.types.OpCode

class Instruction(
    val name: String,
    val exFormats: List<EXT>,
    val opCode: OpCode,
    val pseudoCode: String,
    val description: String,
    val paramSplit: String,
    val logic: (architecture: Architecture, mode: ExecutionMode) -> ReturnType
) {

    fun example(): String {
        val exampleParams = exFormats.joinToString(separator = paramSplit)
        val string = "$name $exampleParams"
        return string
    }

    sealed class ExtType {
        data class Reg(val reg: RegisterContainer.Register) : ExtType()
        data class Imm(val imm: ByteValue.Type) : ExtType()
        data class Label(val name: String) : ExtType()
    }

    sealed class ReturnType {
        data class BinaryRep(val binaryList: List<ByteValue.Type>) : ReturnType()
        data class ExecutionSuccess(val success: Boolean) : ReturnType()
    }

    enum class EXT {
        REG,
        IMM,
        LABEL,
        ADDRESS,
        SHIFT,
    }

    sealed class ExecutionMode {
        class BYTEGENERATION(val extensionWords: List<ExtType>?) : ExecutionMode()
        class EXECUTION(val data: List<ByteValue.Type.Binary>) : ExecutionMode()
    }

    fun execute(architecture: Architecture, mode: ExecutionMode): ReturnType {
        return logic(architecture, mode)
    }

}