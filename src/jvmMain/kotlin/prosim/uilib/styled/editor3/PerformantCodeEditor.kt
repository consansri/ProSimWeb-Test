package prosim.uilib.styled.editor3

import cengine.editor.CodeEditor
import cengine.editor.selection.Caret
import cengine.editor.selection.Selection
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.editor.widgets.Widget
import cengine.lang.LanguageService
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.util.text.LineColumn
import cengine.vfs.VirtualFile
import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import prosim.uilib.UIStates
import prosim.uilib.alpha
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel
import javax.swing.SwingUtilities

class PerformantCodeEditor(
    override val file: VirtualFile,
    project: Project,
) : JPanel(), CodeEditor, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {
    override val psiManager: PsiManager<*>? = project.getManager(file)

    override val textModel: TextModel = RopeModel(file.getAsUTF8String())
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }
    override val textStateModel: TextStateModel = TextStateModel(textModel, selector)
    val lang: LanguageService? get() = psiManager?.lang

    var codeFont: Font = FontType.CODE.getFont()
        set(value) {
            field = value
            vLayout.fmCode = getFontMetrics(codeFont)
        }

    var fontBase: Font = FontType.CODE_INFO.getFont()
        set(value) {
            field = value
            vLayout.fmBase = getFontMetrics(fontBase)
        }

    private val vLayout = VirtualLayout()
    private val renderer = Renderer()
    private val mouseHandler = InputHandler()
    private val keyHandler = KeyHandler()

    private val updateJob = AtomicReference<Job?>(null)
    private var buffer: Image? = null // will be resized later

    init {
        isFocusable = true
        cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
        addKeyListener(keyHandler)

        revalidate()
        repaint()
    }

    private fun getBuffer(): Image {
        if (buffer == null || buffer?.getWidth(this) != width || buffer?.getHeight(this) != height) {
            buffer = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        }
        return buffer!!
    }


    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (width <= 0 || height <= 0) return

        val currBuffer = getBuffer()
        val bufferGraphics = currBuffer.graphics.create() as Graphics2D
        bufferGraphics.background = background
        bufferGraphics.clearRect(0, 0, width, height)

        /*bufferGraphics.color = Color.RED
        bufferGraphics.drawRect(0, 0, width - 1, height - 1)*/

        runBlocking {
            val visibleRect = visibleRect

            val visibleLines = vLayout.getVisibleLines(visibleRect)
            renderer.render(bufferGraphics, visibleLines)

            g.drawImage(buffer, 0, 0, this@PerformantCodeEditor)
        }

        /*(g as? Graphics2D)?.let { g2d ->
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g2d.drawImage(currBuffer, 0, 0, width, height, this)
        }*/
    }

    override fun getPreferredSize(): Dimension {
        return vLayout.getDocumentSize()
    }

    fun updateContent() {
        updateJob.getAndSet(launch {
            updateJob.get()?.cancelAndJoin()
            updateLayoutAndWidgets()
            SwingUtilities.invokeLater { repaint() }
        })?.cancel()
        revalidate()
        repaint()
    }

    private suspend fun updateLayoutAndWidgets() {
        vLayout.invalidateAllLines()
        revalidate()
        repaint()
        // TODO update psi
    }

    inner class VirtualLayout {
        private val lineCache = ConcurrentHashMap<Int, Deferred<LineInfo>>()
        var fmBase = getFontMetrics(fontBase) // TODO make listen to Theme/Scale changes
            set(value) {
                field = value
                widgetPadding = fmCode.height - fmBase.height / 2
            }
        var fmCode = getFontMetrics(codeFont) // TODO make listen to Theme/Scale changes
            set(value) {
                field = value
                fmLineHeight = value.height
                fmColumnWidth = value.charWidth(' ')
                widgetPadding = fmCode.height - fmBase.height / 2
            }
        var fmLineHeight: Int = fmCode.height
        var fmColumnWidth: Int = fmCode.charWidth(' ') // TODO make listen to Theme/Scale changes

        var widgetPadding = fmCode.height - fmBase.height / 2

        val lineNumberWidth get() = fmCode.stringWidth((textModel.lines + 1).toString())
        val rowHeaderWidth get() = lineNumberWidth + fmCode.height + 3 * vLayout.widgetPadding

        private fun Widget.calcSize(): Dimension {
            val width = fmBase.stringWidth(content)
            return Dimension(width + 2 * widgetPadding, fmBase.height + 2 * widgetPadding)
        }

        suspend fun getVisibleLines(visibleRect: Rectangle): List<LineInfo> = coroutineScope {
            val startLine = visibleRect.y / fmLineHeight
            val endLine = (visibleRect.y + visibleRect.height) / fmLineHeight

            (startLine..endLine).map { lineNumber ->
                lineCache.getOrPut(lineNumber) {
                    async(Dispatchers.Default) {
                        calculateLineInfo(lineNumber)
                    }
                }.await()
            }
        }

        fun invalidateAllLines() {
            lineCache.clear()
        }

        private suspend fun calculateLineInfo(lineNumber: Int): LineInfo {
            // Implement logic to calculate line information
            // This includes text, widgets, and folding placeholders
            val startIndex = textModel.getIndexFromLineAndColumn(lineNumber, 0)
            val endIndex = textModel.getIndexFromLineAndColumn(lineNumber + 1, 0)
            val info = LineInfo(lineNumber, startIndex, endIndex, textModel.substring(startIndex, endIndex), psiManager.getInterlineWidgets(lineNumber), psiManager.getInlayWidgets(lineNumber), null)
            nativeLog(info.text)
            return info
        }

        fun getDocumentSize(): Dimension {
            val width = textModel.maxColumns * fmColumnWidth
            val height = textModel.lines * fmLineHeight
            return Dimension(width, height)
        }

        suspend fun getLineAndColumnAt(point: Point): LineColumn {
            val line = getLineAtY(point.y)
            val column = getColumnAtX(line, point.x)
            val lc = LineColumn.of(line, column)
            nativeLog("$lc")
            return lc
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private suspend fun getLineAtY(y: Int): Int {
            var yOffset = 0
            var lineNumber = 0
            while (lineNumber < textModel.lines) {
                val lineHeight = try {
                    when (val info = lineCache[lineNumber]?.getCompleted()) {
                        null -> {
                            fmLineHeight
                        }

                        else -> {
                            (info.interlineWidgets.size + 1) * fmLineHeight
                        }
                    }
                } catch (e: IllegalStateException) {
                    fmLineHeight
                }


                if (y in yOffset..<(yOffset + lineHeight)) {
                    return lineNumber
                }
                yOffset += lineHeight
                lineNumber++
            }
            return textModel.lines
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private suspend fun getColumnAtX(line: Int, x: Int): Int {
            // calculate column
            if (x <= 0) return 0

            var xOffset = 0
            var column = 0

            while (column < textModel.maxColumns) {
                val columnWidth = try {
                    when (val info = lineCache[line]?.getCompleted()) {
                        null -> fmColumnWidth
                        else -> {
                            val widgetAtCol = info.inlayWidgets.firstOrNull { it.position.col == column }
                            if (widgetAtCol != null) {
                                widgetAtCol.calcSize().width + fmColumnWidth
                            } else {
                                fmColumnWidth
                            }
                        }
                    }

                } catch (e: IllegalStateException) {
                    fmColumnWidth
                }

                if (x in xOffset..<(xOffset + columnWidth)) {
                    return column
                }
                xOffset += columnWidth
                column++
            }

            return textModel.maxColumns
        }
    }

    inner class Renderer {

        val fmCode get() = vLayout.fmCode
        val fmBase get() = vLayout.fmBase

        var fg: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        var bg: Color = UIStates.theme.get().globalLaF.bgPrimary

        val foldIndication: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.YELLOW).alpha(13)
        val secFGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE4)
        val secBGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE7)

        val selColor = UIStates.theme.get().codeLaF.getColor(CodeStyle.BLUE).alpha(0x55)
        val markBGColor = UIStates.theme.get().codeLaF.getColor(CodeStyle.BLUE).alpha(0x13)

        val collapseIcon = UIStates.icon.get().folderClosed
        val ellapseIcon = UIStates.icon.get().folderOpen

        var rowHeaderWidth = 0

        suspend fun render(g: Graphics2D, visibleLines: List<LineInfo>) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            g.color = bg
            g.fillRect(0, 0, width, height)

            var yOffset = insets.top
            val xOffset = insets.left
            val rowHeaderWidth = vLayout.rowHeaderWidth
            val selection = selector.selection.asRange()

            visibleLines.forEachIndexed { index, lineInfo ->
                //nativeLog("Rendering ${lineInfo.lineNumber} with height ${vLayout.fmLineHeight} at $xOffset, $yOffset")
                // Render interline widgets
                lineInfo.interlineWidgets.filter { it.position.line == lineInfo.lineNumber }.forEach {
                    val widgetDimension = g.drawWidget(it, rowHeaderWidth, yOffset)
                    yOffset += widgetDimension.height
                }

                g.renderLine(lineInfo, selection, xOffset + rowHeaderWidth, yOffset)

                g.drawLineNumber(lineInfo, Rectangle(xOffset, yOffset, vLayout.lineNumberWidth, vLayout.fmLineHeight))

                yOffset += vLayout.fmLineHeight
            }
        }

        /**
         * @param indicator starting with 0 to [textModel.lines]
         *
         * @return height of line (with drawn widgets)
         */
        private fun Graphics2D.renderLine(lineInfo: LineInfo, selection: IntRange?, xOffset: Int, yOffset: Int) {
            val internalYOffset = yOffset
            var internalXOffset = xOffset

            // Render line text with syntax highlighting
            val startingIndex = lineInfo.startIndex
            val endIndex = lineInfo.endIndex
            //nativeLog("Line $lineNumber: ${textModel.substring(startingIndex, endIndex)}")

            val lineContent = lineInfo.text
            val highlights = lang?.highlightProvider?.fastHighlight(lineContent) ?: emptyList()

            if (selector.caret.line == lineInfo.lineNumber) {
                color = markBGColor
                fillRect(0, yOffset, size.width, fmCode.height)
            }

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

                font = codeFont
                drawString(char.toString(), internalXOffset, internalYOffset + fmCode.ascent)

                // Draw Underline
                lang?.annotationProvider?.cachedAnnotations?.firstOrNull {
                    it.range.contains(charIndex)
                }?.let {
                    color = it.severity.toColor(lang).toColor()
                    fillRect(internalXOffset, internalYOffset + fmCode.height - fmCode.descent / 2, charWidth, CEditorArea.strokeWidth)
                }

                // Draw Caret
                if (selector.caret.index == charIndex) {
                    color = foreground
                    fillRect(internalXOffset, internalYOffset, CEditorArea.strokeWidth, fmCode.height)
                }

                internalXOffset += charWidth

                // Render inlay widgets
                lineInfo.inlayWidgets.filter { it.position.index == charIndex }.forEach {
                    val widgetDim = drawWidget(it, internalXOffset, internalYOffset)
                    internalXOffset += widgetDim.width
                }
            }

            // Render inlay widgets
            lang?.widgetProvider?.cachedPostLineWidget?.filter { it.position.line == lineInfo.lineNumber }?.forEach {
                val widgetDim = drawWidget(it, internalXOffset, internalYOffset)
                internalXOffset += widgetDim.width
            }

            // Draw EOF Caret
            if (endIndex == textModel.length && selector.caret.index == textModel.length && selector.caret.line == lineInfo.lineNumber) {
                color = foreground
                fillRect(internalXOffset, internalYOffset, CEditorArea.strokeWidth, fmCode.height)
            }

            if (lineInfo.foldingPlaceholder != null) {
                val width = fmCode.stringWidth(lineInfo.foldingPlaceholder)
                color = markBGColor
                fillRect(internalXOffset, internalYOffset, width, fmCode.height)

                color = foreground
                drawString(lineInfo.foldingPlaceholder, internalXOffset, internalYOffset + fmCode.ascent)
            }
        }

        private fun Graphics2D.drawLineNumber(lineInfo: LineInfo, rect: Rectangle) {
            color = secFGColor
            font = fontBase
            drawString((lineInfo.lineNumber + 1).toString(), rect.x + rect.width - fmBase.stringWidth((lineInfo.lineNumber + 1).toString()), rect.y + fmCode.height / 2 - fmBase.height / 2 + fmBase.ascent)

            val foldRegions = lang?.codeFoldingProvider?.cachedFoldRegions ?: return

            foldRegions.firstOrNull { it.startLine == lineInfo.lineNumber }?.let {
                if (it.isFolded) {
                    drawImage(collapseIcon.derive(fmCode.height, fmCode.height).image, rect.x + rect.width + CEditorArea.internalPadding, rect.y, null)
                } else {
                    drawImage(ellapseIcon.derive(fmCode.height, fmCode.height).image, rect.x + rect.width + CEditorArea.internalPadding, rect.y, null)
                }
            }
        }

        /**
         * Draws a Widget and returns the needed Dimension.
         */
        private fun Graphics2D.drawWidget(widget: Widget, xOffset: Int, yOffset: Int): Dimension {
            val widgetContentWidth = fmBase.getStringBounds(widget.content, this)
            val contentRect = Rectangle(xOffset + CEditorArea.internalPadding, yOffset + fmCode.height / 2 - widgetContentWidth.height.toInt() / 2, widgetContentWidth.width.toInt(), widgetContentWidth.height.toInt())

            if (widget.type != Widget.Type.INTERLINE) {
                val bgRect = Rectangle(contentRect.x - CEditorArea.internalPadding / 2, contentRect.y - CEditorArea.internalPadding / 2, contentRect.width + CEditorArea.internalPadding, contentRect.height + CEditorArea.internalPadding)
                color = secBGColor
                fillRoundRect(bgRect.x, bgRect.y, bgRect.width, bgRect.height, CEditorArea.internalPadding, CEditorArea.internalPadding)
            }

            color = secFGColor
            font = fontBase
            drawString(widget.content, contentRect.x, contentRect.y + fmBase.ascent)

            val widgetDimension = Dimension(widgetContentWidth.width.toInt() + CEditorArea.internalPadding * 2, fmCode.height)

            return widgetDimension
        }

        private fun Int?.toColor(): Color {
            return if (this == null) fg else Color(this)
        }

    }

    inner class InputHandler : MouseListener, MouseMotionListener {
        override fun mouseClicked(e: MouseEvent?) {

        }

        override fun mousePressed(e: MouseEvent) {
            launch {
                val lc = vLayout.getLineAndColumnAt(Point(e.x - vLayout.rowHeaderWidth - insets.left, e.y - insets.top))
                selector.moveCaretTo(lc.line, lc.column, e.isShiftDown)
                repaint()
            }
        }

        override fun mouseReleased(e: MouseEvent?) {

        }

        override fun mouseEntered(e: MouseEvent?) {

        }

        override fun mouseExited(e: MouseEvent?) {

        }

        override fun mouseDragged(e: MouseEvent) {
            launch {
                val (line, column) = vLayout.getLineAndColumnAt(Point(e.x - vLayout.rowHeaderWidth - insets.left, e.y - insets.top))
                selector.moveCaretTo(line, column, true)
                repaint()
            }
        }

        override fun mouseMoved(e: MouseEvent?) {

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
            updateContent()
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
            updateContent()
            revalidate()
            repaint()
        }

        override fun keyReleased(e: KeyEvent?) {}
    }


}