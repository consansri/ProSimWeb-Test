package cengine.util.integer

fun Array<Bin>.mergeToChunks(currSize: Size, chunkSize: Size): Array<Bin> {
    val source = this.toMutableList()
    val amount = chunkSize.bitWidth / currSize.bitWidth
    val padding = this.size % amount

    repeat(padding) {
        source.add(0, Bin("0", currSize))
    }

    return source.chunked(amount).map { values ->
        Bin(values.joinToString("") { it.rawInput }, chunkSize)
    }.toTypedArray()
}

fun Array<Hex>.mergeToChunks(currSize: Size, chunkSize: Size): Array<Hex> {
    val source = this.toMutableList()
    val amount = chunkSize.hexChars / currSize.hexChars
    val padding = this.size % amount

    repeat(padding) {
        source.add(0, Hex("0", currSize))
    }

    return source.chunked(amount).map { values ->
        Hex(values.joinToString("") { it.rawInput }, chunkSize)
    }.toTypedArray()
}