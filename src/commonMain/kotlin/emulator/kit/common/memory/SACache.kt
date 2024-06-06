package emulator.kit.common.memory

import emulator.kit.common.IConsole

class SACache(
    backingMemory: Memory,
    console: IConsole,
    rowBits: Int,
    offsetBits: Int,
    blockCount: Int,
    replaceAlgo: Model.ReplaceAlgo,
    override val name: String = "Cache (SA)"
) : Cache(
    backingMemory,
    console,
    indexBits = rowBits,
    offsetBits = offsetBits,
    blockCount = blockCount,
    replaceAlgo = replaceAlgo
) {

}