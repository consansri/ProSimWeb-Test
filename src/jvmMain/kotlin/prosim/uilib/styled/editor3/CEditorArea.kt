package prosim.uilib.styled.editor3


import cengine.editor.CodeEditor
import cengine.editor.annotation.AnnotationProvider
import cengine.editor.selection.Caret
import cengine.editor.selection.Selection
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.vfs.VirtualFile
import emulator.kit.assembler.CodeStyle
import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JComponent

class CEditorArea(override val file: VirtualFile) : JComponent(), CodeEditor {
    override val textModel: TextModel = RopeModel()
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }

    var annotationProvider: AnnotationProvider? = null
    var completionProvider: cengine.editor.completion.CompletionProvider? = null

    val textStateModel = TextStateModel(textModel, selector)
    val handler = EventHandler()

    var fontCode: Font = FontType.CODE.getFont()
    var fontBase: Font = FontType.CODE_INFO.getFont()

    private var fmCode: FontMetrics = getFontMetrics(fontCode)
    private var fmBase: FontMetrics = getFontMetrics(fontBase)
    private var caretWidth: Int = 2
    private var selColor: Color = Color(0x77000000 xor UIStates.theme.get().codeLaF.selectionColor.rgb, true)

    init {
        border = BorderMode.INSET.getBorder()
        textModel.insert(0, "Hello World!\nNice World!")
        foreground = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        background = UIStates.theme.get().globalLaF.bgPrimary
        isFocusable = true
        requestFocus()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // draw background
        g2d.color = background
        val bounds = bounds
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)


        // draw content


    }

    private fun renderLines(g2d: Graphics2D) {
        var y = insets.top
        val selection = selector.selection.asRange()
        val visibleLines = language?.codeFoldingProvider?.getVisibleLines(textModel.lines)
        if (visibleLines == null) {
            (0..textModel.lines).forEach { lineNumber ->
                val height = renderLine(g2d, lineNumber, selection, y)
                y += height
            }
        } else {
            visibleLines.forEach { lineNumber ->
                val height = renderLine(g2d, lineNumber, selection, y)
                y += height
            }
        }
    }

    /**
     * @return height of line (with drawn widgets)
     */
    private fun renderLine(g2d: Graphics2D, lineNumber: Int, selection: IntRange?, y: Int): Int {
        var height = 0

        // Render interline widgets
        language?.widgetProvider?.cachedInterLineWidgets?.filter { it.position.line == lineNumber }?.forEach {
            g2d.font = fontBase
            g2d.color = foreground
            g2d.drawString(it.content, insets.left, y + height + fmBase.ascent)
            height += fmBase.height
        }

        // Render line text with syntax highlighting
        val startingIndex = textModel.getIndexFromLineAndColumn(lineNumber - 1, 0)
        val endIndex = textModel.getIndexFromLineAndColumn(lineNumber, 0)
        //nativeLog("Line $lineNumber: ${textModel.substring(startingIndex, endIndex)}")
        var x = insets.left
        val lineContent = textModel.substring(startingIndex, endIndex)
        val highlights = language?.highlightProvider?.fastHighlight(lineContent) ?: emptyList()

        for ((colID, charIndex) in (startingIndex until endIndex).withIndex()) {
            val char = lineContent[colID]
            val charWidth = fmCode.charWidth(char)

            // Draw Selection
            selection?.let {
                if (charIndex in it) {
                    g2d.color = selColor
                    g2d.fillRect(x, y+ height, charWidth, fmCode.height)
                }
            }

            // Draw Char
            g2d.color = (highlights.firstOrNull { it.range.contains(colID) }
                ?.color(language)
                ?: language
                    ?.highlightProvider
                    ?.cachedHighlights
                    ?.firstOrNull { it.range.contains(charIndex) }
                    ?.color(language)
                    ).toColor()

            g2d.font = fontCode
            g2d.drawString(char.toString(), x, y + height + fmCode.ascent)

            // Draw Underline
            language?.annotationProvider?.cachedAnnotations?.firstOrNull {
                it.range.contains(charIndex)
            }?.let {
                g2d.color = it.severity.toColor(language).toColor()
                g2d.drawLine(x, y + height + fmCode.height - fmCode.descent, x + charWidth, y + height + fmCode.height - fmCode.descent)
            }

            // Draw Caret
            if (selector.caret.index == charIndex) {
                g2d.color = foreground
                g2d.fillRect(x, y + height, caretWidth, fmCode.height)
            }

            x += charWidth

            // Render inlay widgets
            language?.widgetProvider?.cachedInlayWidgets?.filter {it.position.index == charIndex}?.forEach {
                g2d.font = fontBase
                g2d.color = foreground
                g2d.drawString(it.content, x, y + height + fmBase.ascent)
                x += fmBase.stringWidth(it.content)
            }
        }

        // Render inlay widgets
        language?.widgetProvider?.cachedPostLineWidget?.filter {it.position.line == lineNumber}?.forEach {
            g2d.font = fontBase
            g2d.color = foreground
            g2d.drawString(it.content, x, y + height + fmBase.ascent)
            x += fmBase.stringWidth(it.content)
        }

        // Draw EOL Caret
        if (endIndex == textModel.length && selector.caret.index == textModel.length && selector.caret.line == lineNumber - 1) {
            g2d.color = foreground
            g2d.fillRect(x, y + height, caretWidth, fmCode.height)
        }
        return height + fmCode.height
    }

    fun Int?.toColor(): Color {
        return if (this == null) foreground else Color(this)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }

    private fun getClipboardContent(): String? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val clipboardData = clipboard.getContents(null)
        return if (clipboardData != null && clipboardData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            clipboardData.getTransferData(DataFlavor.stringFlavor) as String
        } else {
            null
        }
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(800, 600)
    }

    inner class EventHandler {
        init {
            this@CEditorArea.addKeyListener(object : KeyAdapter() {
                override fun keyTyped(e: KeyEvent) {
                    // Character Insertion
                    when {
                        e.keyChar.isISOControl() -> {

                        }

                        e.keyChar.isDefined() -> {
                            val newChar = e.keyChar.toString()
                            textStateModel.delete(selector.selection)
                            textStateModel.insert(selector.caret, newChar)
                        }
                    }
                    repaint()
                }

                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_TAB -> {
                            if (e.isShiftDown) {
                                // remove Indent
                            } else {
                                // indent
                            }
                        }

                        KeyEvent.VK_A -> {
                            if (e.isControlDown) {
                                selector.selection.select(0, textModel.length)
                            }
                        }

                        KeyEvent.VK_C -> {
                            if (e.isControlDown) {
                                copyToClipboard(textModel.substring(selector.selection))
                            }
                        }

                        KeyEvent.VK_Z -> {
                            if (e.isControlDown) {
                                if (e.isShiftDown) {
                                    textStateModel.redo()
                                } else {
                                    textStateModel.undo()
                                }
                            }
                        }

                        KeyEvent.VK_V -> {
                            if (e.isControlDown) {
                                val content = getClipboardContent()
                                textStateModel.delete(selector.selection)

                                content?.let { text ->
                                    textStateModel.insert(selector.caret, text)
                                }
                            }
                        }

                        KeyEvent.VK_X -> {
                            if (e.isControlDown) {
                                val selected = textModel.substring(selector.selection)
                                if (selected.isNotEmpty()) {
                                    copyToClipboard(selected)
                                    textStateModel.delete(selector.selection)
                                }
                            }
                        }

                        KeyEvent.VK_ENTER -> {
                            textStateModel.delete(selector.selection)
                            textStateModel.insert(selector.caret, "\n")
                        }

                        KeyEvent.VK_LEFT -> {
                            selector.moveCaretLeft(1, e.isShiftDown)
                        }

                        KeyEvent.VK_RIGHT -> {
                            selector.moveCaretRight(1, e.isShiftDown)
                        }

                        KeyEvent.VK_UP -> {
                            selector.moveCaretUp(1, e.isShiftDown)
                        }

                        KeyEvent.VK_DOWN -> {
                            selector.moveCaretDown(1, e.isShiftDown)
                        }

                        KeyEvent.VK_BACK_SPACE -> {
                            if (selector.selection.valid()) {
                                val caretIsHigherBound = selector.caretIsAtHigherBoundOfSel()
                                val deleted = textStateModel.delete(selector.selection)
                                if (caretIsHigherBound) selector.caret -= deleted
                            } else {
                                textStateModel.delete(selector.caret.index - 1, selector.caret.index)
                                selector.moveCaretLeft(1, false)
                            }
                        }

                        KeyEvent.VK_DELETE -> {
                            if (selector.selection.valid()) {
                                val caretIsHigherBound = selector.caretIsAtHigherBoundOfSel()
                                val deleted = textStateModel.delete(selector.selection)
                                if (caretIsHigherBound) selector.caret -= deleted
                            } else {
                                textStateModel.delete(selector.caret.index, selector.caret.index + 1)
                            }
                        }

                        KeyEvent.VK_HOME -> {
                            selector.home(e.isShiftDown)
                        }

                        KeyEvent.VK_END -> {
                            selector.end(e.isShiftDown)
                        }

                        KeyEvent.VK_F -> {
                            // TODO if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.FIND)
                        }

                        KeyEvent.VK_R -> {
                            // TODO if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.REPLACE)
                        }

                        // Custom Use
                        KeyEvent.VK_S -> {
                            // TODO if (e.isControlDown) shortCuts?.ctrlS()
                        }
                    }
                    repaint()
                }

                override fun keyReleased(e: KeyEvent?) {}
            })
        }

    }

}