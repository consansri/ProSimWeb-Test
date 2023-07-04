package extendable.components.connected

import extendable.ArchConst

class Transcript {

    private val headers: Array<String>
    private var content: List<TranscriptEntry> = listOf()

    constructor() {
        val transcriptHeaders = ArchConst.TranscriptHeaders.values()
        val headersList = mutableListOf<String>()
        for(header in transcriptHeaders){
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

    class TranscriptEntry(val memoryHexAddress: String, val editorLines: IntRange){

        val content = mutableMapOf<String, String>()

        fun addContent(header: String, value: String){
            content[header] = value
        }

    }

}