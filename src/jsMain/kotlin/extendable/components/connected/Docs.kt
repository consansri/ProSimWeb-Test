package extendable.components.connected

import react.FC
import react.Props


class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile.SourceFile(
            "User Manual",
            "../documents/user-manual.html"
        )
    )

    init {
        files.addAll(htmlFiles)
    }

    constructor() : this(*emptyArray<Docs.HtmlFile>())

    sealed class HtmlFile(val title: String) {

        class SourceFile(title: String, val src: String) : HtmlFile(title)
        class DefinedFile(title: String, val fc: FC<Props>) : HtmlFile(title)

    }
}