package extendable.components.connected

import extendable.ArchConst
import extendable.components.types.MutVal

class Transcript {

    private val headers: Array<String>
    private var content: List<TranscriptEntry> = listOf()

    constructor() {
        val transcriptHeaders = ArchConst.TranscriptHeaders.values()
        val headersList = mutableListOf<String>()
        for (header in transcriptHeaders) {
            headersList.add(header.name)
        }
        this.headers = headersList.toTypedArray()
    }

    constructor(headers: Array<String>) {
        this.headers = headers
    }

    fun setContent(transcriptEntrys: List<TranscriptEntry>) {
        this.content = transcriptEntrys
    }

    fun addContent(transcriptEntry: TranscriptEntry) {
        this.content += transcriptEntry
    }

    fun getContent(): List<TranscriptEntry> {
        return content
    }

    fun getHeaders(): Array<String> {
        return headers
    }

    class TranscriptEntry(val memoryAddress: MutVal.Value.Hex) {

        val content = mutableMapOf<ArchConst.TranscriptHeaders, String>(ArchConst.TranscriptHeaders.addr to memoryAddress.getRawHexStr())

        fun addContent(header: ArchConst.TranscriptHeaders, value: String) {
            content[header] = value
        }

    }

}