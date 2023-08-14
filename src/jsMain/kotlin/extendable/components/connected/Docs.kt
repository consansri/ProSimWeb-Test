package extendable.components.connected



class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile(
            "User Manual",
            """
                <h1>User Manual</h1>
                <h2>General</h2>
                <ul>
                    <li><img alt="sun" src="../benicons/ver3/lightmode.svg"/><img alt="sun" src="../benicons/ver3/darkmode.svg"/>switches theme of whole site</li>
                    <li><img alt="eraser" src="../icons/eraser.svg"/>deletes every value in browser localstorage which was used by this site</li>
                </ul>
                <h2>Code Editor</h2>
                <ul>
                    <li><img alt="transcript" src="../benicons/ver3/disassembler.svg"/>switches to transcript view<br>click on horizontal name to switch between compiled and disassembled transcript</li>
                    <li><img alt="error" src="../benicons/ver3/status_error.svg"/><img alt="fine" src="../benicons/ver3/status_fine.svg"/><img alt="loading" src="../benicons/ver3/status_loading.svg"/>shows the compilation state of the current file<br>recompiles on click</li>
                    <li><img alt="undo" src="../benicons/ver3/backwards.svg"/>undo current changes in current file</li>
                    <li><img alt="redo" src="../benicons/ver3/forwards.svg"/>redo current changes in current file</li>
                    <li><img alt="redo" src="../benicons/ver3/info.svg"/>shows code editor info on hover</li>
                    <li><img alt="clear" src="../benicons/ver3/delete_black.svg"/>cleares current file content</li>
                    <li><img alt="tag" src="../benicons/ver3/tag.svg"/>shows grammartree information of selected element in code for debugging</li>
                </ul>
                <h2>Execution Control</h2>
                <h2>Registers</h2>
                <h2>Memory</h2>
                <h2>Settings</h2>
                
            """.trimIndent()
            )
    )

    init {
        files.addAll(htmlFiles)
    }
    constructor() : this(*emptyArray<Docs.HtmlFile>())

    data class HtmlFile(val title: String, val htmlContent: String)
}