package extendable.components.assembly

import extendable.Architecture
import extendable.components.types.ByteValue

abstract class Assembly {

    abstract fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree)

    abstract fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree, startAtLine: Int): ReservationMap

    data class ReservationMap(val address: ByteValue.Type.Hex)

}