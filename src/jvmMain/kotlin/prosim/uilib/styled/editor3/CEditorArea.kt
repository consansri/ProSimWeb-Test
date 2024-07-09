package prosim.uilib.styled.editor3


import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotater
import cengine.editor.folding.CodeFolder
import cengine.editor.highlighting.Highlighter
import cengine.editor.selection.Caret
import cengine.editor.selection.Selection
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.editor.widgets.WidgetManager
import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeLog
import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JComponent

class CEditorArea : JComponent(), CodeEditor {
    override val textModel: TextModel = RopeModel()
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }
    override val codeFolder: CodeFolder = CodeFolder()
    override val widgetManager: WidgetManager = WidgetManager()
    override var highlighter: Highlighter? = null
    override var annotater: Annotater? = null
    override var completer: cengine.editor.completion.Completer? = null

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
        val visibleLines = codeFolder.getVisibleLines(textModel.lines)
        var y = insets.top
        val selection = selector.selection.asRange()
        nativeLog("Range: $selection")

        visibleLines.forEach { lineNumber ->


            // Render interline widgets
            widgetManager.interlineWidgets[lineNumber]?.forEach {
                g2d.font = fontBase
                g2d.color = foreground
                g2d.drawString(it.content, insets.left, y + fmBase.ascent)
                y += fmBase.height
            }

            // Render line text with syntax highlighting
            val startingIndex = textModel.getIndexFromLineAndColumn(lineNumber - 1, 0)
            val endIndex = textModel.getIndexFromLineAndColumn(lineNumber, 0)
            //nativeLog("Line $lineNumber: ${textModel.substring(startingIndex, endIndex)}")
            var x = insets.left
            for ((colID, charIndex) in (startingIndex until endIndex).withIndex()) {
                val char = textModel.charAt(charIndex)
                val highlighting = highlighter?.getHighlighting(charIndex)
                val charWidth = fmCode.charWidth(char)

                // Draw Selection
                selection?.let {
                    if (charIndex in it) {
                        g2d.color = selColor
                        g2d.fillRect(x, y, charWidth, fmCode.height)
                    }
                }

                // Draw Char
                g2d.color = highlighting?.color.toColor()
                g2d.font = fontCode
                g2d.drawString(char.toString(), x, y + fmCode.ascent)

                // Draw Underline
                highlighting?.underline?.let { col ->
                    g2d.color = col.toColor()
                    g2d.drawLine(x, y + fmCode.height - fmCode.descent, x + charWidth, y + fmCode.height - fmCode.descent)
                }

                // Draw Caret
                if (selector.caret.index == charIndex) {
                    g2d.color = foreground
                    g2d.fillRect(x, y, caretWidth, fmCode.height)
                }

                x += charWidth

                // Render inlay widgets
                widgetManager.inlayWidgets[lineNumber to colID]?.forEach {
                    g2d.font = fontBase
                    g2d.color = foreground
                    g2d.drawString(it.content, x, y + fmBase.ascent)
                    x += fmBase.stringWidth(it.content)
                }
            }

            // Render inlay widgets
            widgetManager.postlineWidgets[lineNumber]?.forEach {
                g2d.font = fontBase
                g2d.color = foreground
                g2d.drawString(it.content, x, y + fmBase.ascent)
                x += fmBase.stringWidth(it.content)
            }

            // Draw EOL Caret
            if (endIndex == textModel.length && selector.caret.index == textModel.length) {
                g2d.color = foreground
                g2d.fillRect(x, y, caretWidth, fmCode.height)
            }

            y += fmCode.height
        }
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
                            textStateModel.delete(selector.caret.index - 1, selector.caret.index)
                            selector.moveCaretLeft(1, false)
                        }

                        KeyEvent.VK_DELETE -> {
                            if (selector.selection.valid()) {
                                textStateModel.delete(selector.selection)
                            } else {
                                textStateModel.delete(selector.caret.index, selector.caret.index + 1)
                            }
                        }

                        KeyEvent.VK_HOME -> {
                            TODO()
                            /*if (e.isShiftDown) handleShiftSelection(e) else {
                                if (selStart < selEnd) {
                                    swapSelection()
                                } else {
                                    resetSelection()
                                    caret.moveCaretHome()
                                }
                            }*/
                        }

                        KeyEvent.VK_END -> {
                            TODO()
                            /*if (e.isShiftDown) handleShiftSelection(e) else {
                                if (selStart > selEnd) {
                                    swapSelection()
                                } else {
                                    resetSelection()
                                    caret.moveCaretEnd()
                                }
                            }*/
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