package extendable.components.connected



class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile(
            "User Manual",
            "../documents/user-manual.html"
            )
    )

    init {
        files.addAll(htmlFiles)
    }
    constructor() : this(*emptyArray<Docs.HtmlFile>())

    data class HtmlFile(val title: String, val src: String)
}