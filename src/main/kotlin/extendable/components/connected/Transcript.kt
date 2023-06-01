package extendable.components.connected

import extendable.ArchConst

class Transcript {

    private val headers: Array<String>
    private var content: List<Array<String>> = listOf()

    constructor() {
        this.headers = ArchConst.TRANSCRIPT_HEADERS

    }

    constructor(headers: Array<String>) {
        this.headers = headers

    }

    fun getHeaders(): Array<String>{
        return headers
    }

    fun setContent(content: List<Array<String>>){
        this.content = content
    }

    fun getContent(): List<Array<String>>{
        return content
    }



}