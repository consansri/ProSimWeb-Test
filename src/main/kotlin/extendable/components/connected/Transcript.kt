package extendable.components.connected

import extendable.ArchConst

class Transcript {

    private val headers: Array<String>
    private var content: List<TranscriptEntry> = listOf()

    constructor() {
        this.headers = ArchConst.TRANSCRIPT_HEADERS

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

    class TranscriptEntry(val content: Array<String>, val map: Map<LongRange, IntRange>)

}