package prosim.uilib.styled.editor3


import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
import cengine.editor.selection.Caret
import cengine.editor.selection.Selection
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.editor.widgets.Widget
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.vfs.VirtualFile
import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeLog
import prosim.uilib.UIStates
import prosim.uilib.styled.COverlay
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.JComponent

class CEditorArea(override val file: VirtualFile, project: Project) : JComponent(), CodeEditor {

    companion object {
        const val columnPadding = 20
        const val linePadding = 5
        const val strokeWidth: Int = 2
        const val internalPadding = 4
    }

    override val psiManager: PsiManager<*>? = project.getManager(file)
    override val textModel: TextModel = RopeModel()
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }

    override val textStateModel = TextStateModel(textModel, selector)

    private val lang get() = psiManager?.lang

    // Children
    val overlay = COverlay()

    // UI

    var fontCode: Font = FontType.CODE.getFont()
    var fontBase: Font = FontType.CODE_INFO.getFont()

    private var fmCode: FontMetrics = getFontMetrics(fontCode)
    private var fmBase: FontMetrics = getFontMetrics(fontBase)

    private var selColor: Color = Color(0x77000000 xor UIStates.theme.get().codeLaF.selectionColor.rgb, true)
    private var secFGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE4)
    private var secBGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE7)

    val scrollPane = CScrollPane(true, this).apply {
        CScrollPane.removeArrowKeyScrolling(this)
    }

    // TEMPORARY RENDERED ELEMENTS
    private var rowHeaderWidth: Int = 0

    private var renderedLines: List<Pair<Bounds, Int>> = listOf()
    private var renderedWidgets: List<Pair<Bounds, Widget>> = listOf()
    private var renderedAnnotations: List<Pair<Bounds, Annotation>> = listOf()


    init {
        border = BorderMode.INSET.getBorder()
        cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        textModel.insert(0, "Hello World!\nNice World!")
        foreground = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        background = UIStates.theme.get().globalLaF.bgPrimary
        isFocusable = true

        val mouseHandler = MouseHandler()
        addKeyListener(KeyHandler())
        addMouseMotionListener(mouseHandler)
        addMouseListener(mouseHandler)

        project.register(this) // to listen to file changes from other sources

        file.onDiskChange = {
            loadFromFile()
            nativeLog("Reloaded Editor Content")
            revalidate()
            repaint()
        }

        loadFromFile()

        requestFocus()
        revalidate()
        repaint()
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
        g2d.renderEditorContent()
    }

    private fun Graphics2D.renderEditorContent() {
        val tempRenderedLines = mutableListOf<Pair<Bounds, Int>>()
        val tempRenderedWidgets = mutableListOf<Pair<Bounds, Widget>>()
        val tempRenderedAnnotations = mutableListOf<Pair<Bounds,Annotation>>()

        var yOffset = insets.top
        val xOffset = insets.left
        val lineNumberWidth = fmCode.stringWidth(textModel.lines.toString())
        rowHeaderWidth = xOffset + lineNumberWidth + 2 * internalPadding

        color = secBGColor
        drawLine(xOffset + lineNumberWidth + internalPadding, yOffset, xOffset + lineNumberWidth + internalPadding, yOffset + scrollPane.visibleRect.height)

        val selection = selector.selection.asRange()
        val visibleLines = psiManager?.lang?.codeFoldingProvider?.getVisibleLines(textModel.lines)
        if (visibleLines == null) {
            (0..<textModel.lines).forEach { lineNumber ->

                // Render interline widgets
                lang?.widgetProvider?.cachedInterLineWidgets?.filter { it.position.line == lineNumber }?.forEach {
                    val widgetDimension = drawWidget(it, rowHeaderWidth, yOffset, tempRenderedWidgets)
                    yOffset += widgetDimension.height
                }

                val height = renderLine(lineNumber, selection, yOffset, rowHeaderWidth, tempRenderedWidgets, tempRenderedAnnotations)

                drawLineNumber(lineNumber, Rectangle(xOffset, yOffset, lineNumberWidth, height))

                tempRenderedLines += Bounds(xOffset, yOffset, Int.MAX_VALUE, height) to lineNumber

                yOffset += height
            }
        } else {
            visibleLines.forEach { lineNumber ->

                // Render interline widgets
                lang?.widgetProvider?.cachedInterLineWidgets?.filter { it.position.line == lineNumber }?.forEach {
                    val widgetDimension = drawWidget(it, rowHeaderWidth, yOffset, tempRenderedWidgets)
                    yOffset += widgetDimension.height
                }

                val height = renderLine(lineNumber, selection, yOffset, rowHeaderWidth, tempRenderedWidgets, tempRenderedAnnotations)

                drawLineNumber(lineNumber, Rectangle(xOffset, yOffset, lineNumberWidth, height))

                tempRenderedLines += Bounds(xOffset, yOffset, Int.MAX_VALUE, height) to lineNumber

                yOffset += height
            }
        }

        renderedLines = tempRenderedLines
        renderedWidgets = tempRenderedWidgets
        renderedAnnotations = tempRenderedAnnotations
    }

    /**
     * Draws a Widget and returns the needed Dimension.
     */
    private fun Graphics2D.drawWidget(widget: Widget, xOffset: Int, yOffset: Int, tempRenderedWidgets: MutableList<Pair<Bounds, Widget>>): Dimension {
        val widgetContentWidth = fmBase.getStringBounds(widget.content, this)
        val contentRect = Rectangle(xOffset + internalPadding, yOffset + fmCode.height / 2 - widgetContentWidth.height.toInt() / 2, widgetContentWidth.width.toInt(), widgetContentWidth.height.toInt())

        if (widget.type != Widget.Type.INTERLINE) {
            val bgRect = Rectangle(contentRect.x - internalPadding / 2, contentRect.y - internalPadding / 2, contentRect.width + internalPadding, contentRect.height + internalPadding)
            color = secBGColor
            fillRoundRect(bgRect.x, bgRect.y, bgRect.width, bgRect.height, internalPadding, internalPadding)
        }

        color = secFGColor
        font = fontBase
        drawString(widget.content, contentRect.x, contentRect.y + fmBase.ascent)

        val widgetDimension = Dimension(widgetContentWidth.width.toInt() + internalPadding * 2, fmCode.height)
        tempRenderedWidgets.add(Bounds(xOffset, yOffset, widgetDimension.width, widgetDimension.height) to widget)

        return widgetDimension
    }

    private fun Graphics2D.drawLineNumber(number: Int, rect: Rectangle) {
        color = secFGColor
        font = fontBase
        drawString(number.toString(), rect.x + rect.width - fmBase.stringWidth(number.toString()), rect.y + fmCode.height / 2 - fmBase.height / 2 + fmBase.ascent)
    }

    /**
     * @param lineNumber starting with 1 to [textModel.lines]
     *
     * @return height of line (with drawn widgets)
     */
    private fun Graphics2D.renderLine(lineNumber: Int, selection: IntRange?, yOffset: Int, xOffset: Int, tempRenderedWidgets: MutableList<Pair<Bounds, Widget>>, tempRenderedAnnotation: MutableList<Pair<Bounds,Annotation>>): Int {
        var internalYOffset = yOffset
        var internalXOffset = xOffset


        // Render line text with syntax highlighting
        val startingIndex = textModel.getIndexFromLineAndColumn(lineNumber, 0)
        val endIndex = textModel.getIndexFromLineAndColumn(lineNumber + 1, 0)
        //nativeLog("Line $lineNumber: ${textModel.substring(startingIndex, endIndex)}")

        val lineContent = textModel.substring(startingIndex, endIndex)
        val highlights = lang?.highlightProvider?.fastHighlight(lineContent) ?: emptyList()

        for ((colID, charIndex) in (startingIndex until endIndex).withIndex()) {
            val char = lineContent[colID]
            val charWidth = fmCode.charWidth(char)

            // Draw Selection
            selection?.let {
                if (charIndex in it) {
                    color = selColor
                    fillRect(internalXOffset, internalYOffset, charWidth, fmCode.height)
                }
            }

            // Draw Char
            color = (highlights.firstOrNull { it.range.contains(colID) }
                ?.color(lang)
                ?: lang
                    ?.highlightProvider
                    ?.cachedHighlights
                    ?.firstOrNull { it.range.contains(charIndex) }
                    ?.color(lang)
                    ).toColor()

            font = fontCode
            drawString(char.toString(), internalXOffset, internalYOffset + fmCode.ascent)

            // Draw Underline
            lang?.annotationProvider?.cachedAnnotations?.firstOrNull {
                it.range.contains(charIndex)
            }?.let {
                color = it.severity.toColor(lang).toColor()
                fillRect(internalXOffset, internalYOffset + fmCode.height - fmCode.descent / 2, charWidth, strokeWidth)
                tempRenderedAnnotation.add(Bounds(internalXOffset, internalYOffset, charWidth, fmCode.height) to it)
            }

            // Draw Caret
            if (selector.caret.index == charIndex) {
                color = foreground
                fillRect(internalXOffset, internalYOffset, strokeWidth, fmCode.height)
            }

            internalXOffset += charWidth

            // Render inlay widgets
            lang?.widgetProvider?.cachedInlayWidgets?.filter { it.position.index == charIndex }?.forEach {
                val widgetDim = drawWidget(it, internalXOffset, internalYOffset, tempRenderedWidgets)
                internalXOffset += widgetDim.width
            }
        }

        // Render inlay widgets
        lang?.widgetProvider?.cachedPostLineWidget?.filter { it.position.line == lineNumber }?.forEach {
            val widgetDim = drawWidget(it, internalXOffset, internalYOffset, tempRenderedWidgets)
            internalXOffset += widgetDim.width
        }

        // Draw EOF Caret
        if (endIndex == textModel.length && selector.caret.index == textModel.length && selector.caret.line == lineNumber) {
            color = foreground
            fillRect(internalXOffset, internalYOffset, strokeWidth, fmCode.height)
        }
        return internalYOffset + fmCode.height - yOffset
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
        return Dimension(insets.left + insets.right + fmCode.charWidth(' ') * (textModel.maxColumns + columnPadding), insets.top + insets.bottom + fmCode.height * (textModel.lines + linePadding))
    }

    override fun getPreferredSize(): Dimension {
        return minimumSize
    }

    inner class MouseHandler : MouseListener, MouseMotionListener {
        override fun mouseClicked(e: MouseEvent) {
            val widget = renderedWidgets.firstOrNull { it.first.isInBounds(e.x, e.y) } ?: return
            widget.second.onClick()
        }

        override fun mousePressed(e: MouseEvent?) {

        }

        override fun mouseReleased(e: MouseEvent?) {

        }

        override fun mouseEntered(e: MouseEvent?) {

        }

        override fun mouseExited(e: MouseEvent?) {

        }

        override fun mouseDragged(e: MouseEvent?) {

        }

        override fun mouseMoved(e: MouseEvent) {
            val annotation = renderedAnnotations.firstOrNull { it.first.isInBounds(e.x, e.y) }
            if (annotation != null) {
                nativeLog("Annotation Hovered: ${annotation.second}")
                val html = """
                    <html>
                    <body>
                    <b>${annotation.second.severity}</b><br>
                    ${annotation.second.message}
                    </body>
                    </html>
                """.trimIndent()
                overlay.setContent(html, true)
                overlay.showAtLocation(e.x, e.y, 300, this@CEditorArea)
                return
            } else {
                overlay.makeInvisible()
            }
        }
    }

    inner class KeyHandler : KeyAdapter() {
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
                        if (selector.caret.index > 0) {
                            textStateModel.delete(selector.caret.index - 1, selector.caret.index)
                            selector.moveCaretLeft(1, false)
                        }
                    }
                }

                KeyEvent.VK_DELETE -> {
                    if (selector.selection.valid()) {
                        val caretIsHigherBound = selector.caretIsAtHigherBoundOfSel()
                        val deleted = textStateModel.delete(selector.selection)
                        if (caretIsHigherBound) selector.caret -= deleted
                    } else {
                        if (selector.caret.index < textModel.length) {
                            textStateModel.delete(selector.caret.index, selector.caret.index + 1)
                        }
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

                // Save File
                KeyEvent.VK_S -> {
                    if (e.isControlDown) {
                        saveToFile()
                    }
                }
            }
            revalidate()
            repaint()
        }

        override fun keyReleased(e: KeyEvent?) {}
    }

    private data class Bounds(val x: Int, val y: Int, val width: Int, val height: Int) {
        fun isInBounds(x: Int, y: Int): Boolean = x > this.x && x < this.x + width && y > this.y && y < this.y + height
    }
}