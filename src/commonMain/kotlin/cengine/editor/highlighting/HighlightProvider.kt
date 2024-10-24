package cengine.editor.highlighting

interface HighlightProvider {

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String): List<HLInfo>
}