package emulator.kit.common

import react.FC
import react.Props

/**
 * This class contains all documents which are partly supplied by specific architectures. There are two options to define a documentation file.
 * The first is by linking a source path to a specific html source file and the second is by directly defining a file as an react component, inwhich information can be generated directly from the implemented architecture.
 */
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

    constructor() : this(*emptyArray<HtmlFile>())

    sealed class HtmlFile(val title: String) {

        class SourceFile(title: String, val src: String) : HtmlFile(title)
        class DefinedFile(title: String, val fc: FC<Props>) : HtmlFile(title)

    }
}