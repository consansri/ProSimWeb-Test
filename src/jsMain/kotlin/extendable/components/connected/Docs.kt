package extendable.components.connected



class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile(
            "User Manual",
            """
                <h1>User Manual</h1>
                
            """.trimIndent()
            )
    )

    init {
        files.addAll(htmlFiles)
    }
    constructor() : this(*emptyArray<Docs.HtmlFile>())

    data class HtmlFile(val title: String, val htmlContent: String)
}