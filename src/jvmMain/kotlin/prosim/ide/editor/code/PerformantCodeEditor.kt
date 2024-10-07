package prosim.ide.editor.code

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.editor.annotation.Annotation
import cengine.editor.indentation.BasicIndenation
import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFile
import cengine.util.text.LineColumn
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.*
import prosim.ide.editor.EditorComponent
import prosim.ide.editor.copyToClipboard
import prosim.ide.editor.getClipboardContent
import prosim.ide.getFileIcon
import prosim.uilib.UIStates
import prosim.uilib.alpha
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.CToolView
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.event.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingUtilities

class PerformantCodeEditor(
    override val file: VirtualFile,
    project: Project
) : EditorComponent(), CodeEditor, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()), CToolView.View {
    companion object {
        const val CARET_SCROLL_LINE_PADDING = 2
    }

    override var currentElement: PsiElement? = null
        set(value) {
            field = value
            if (value != null) nativeLog("Path: ${lang?.psiService?.path(value)?.joinToString(" > ") { it.pathName }}")
        }
    override var annotations: Set<Annotation> = emptySet()

    override val psiManager: PsiManager<*>? = project.getManager(file)

    override val textModel: TextModel = RopeModel(file.getAsUTF8String())
    override val selector: Selector = Selector(textModel)

    override val textStateModel: TextStateModel = TextStateModel(this, textModel, selector)
    override val indentationProvider: IndentationProvider = BasicIndenation(textStateModel, textModel)

    private var scrollPane: CScrollPane? = null

    private val scaleListener = UIStates.scale.createAndAddListener {
        codeFont = FontType.CODE.getFont()
        fontBase = FontType.CODE_INFO.getFont()
        revalidate()
        repaint()
    }

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

    private val modificationOverlay: ModificationOverlay<EditorModification> = ModificationOverlay(this)

    override val component: Component = createScrollPane()

    override val icon: FlatSVGIcon? = psiManager?.lang?.getFileIcon()
    override val title: String get() = file.name
    override val tooltip: String get() = file.path.toString(FPath.DELIMITER)

    private val analytics: Analytics = Analytics(this)

    override val viewname: String
        get() = file.name
    override val content: JComponent
        get() = analytics

    init {
        isFocusable = true
        cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        border = BorderFactory.createEmptyBorder(0, vLayout.internalPadding, 0, vLayout.internalPadding)

        isFocusCycleRoot = true
        focusTraversalKeysEnabled = false

        project.register(this) // to listen to file changes from other sources

        file.onDiskChange = {
            loadFromFile()
            revalidate()
            repaint()
        }

        loadFromFile()

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
        addKeyListener(keyHandler)

        invalidateContent(this)
    }

    fun createScrollPane(): CScrollPane {
        val sp = CScrollPane(this, true).apply {
            CScrollPane.removeArrowKeyScrolling(this)
            verticalScrollBar.blockIncrement = height
            horizontalScrollBar.blockIncrement = width
            verticalScrollBar.unitIncrement = vLayout.lineHeight * 3
            horizontalScrollBar.unitIncrement = vLayout.fmColumnWidth * 3
        }
        scrollPane = sp
        return sp
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (width <= 0 || height <= 0) return
        val g2d = g.create() as Graphics2D

        runBlocking {
            val visibleRect = scrollPane?.visibleRect ?: visibleRect

            val visibleLines = vLayout.getVisibleLines(visibleRect)
            renderer.render(g2d, visibleLines)

            g2d.dispose()
        }
    }


    override fun getPreferredSize(): Dimension {
        return vLayout.getDocumentSize()
    }

    override fun invalidateAnalytics(editor: CodeEditor) {
        analytics.updateNotations()
    }

    override fun invalidateContent(editor: CodeEditor) {
        updateJob.getAndSet(launch {
            psiManager?.getPsiFile(file)?.let {
                currentElement = psiManager.lang.psiService.findElementAt(it, selector.caret.index)
            }
            updateContent()
        })?.cancel()
    }

    private suspend fun updateContent() {
        vLayout.invalidateAllLines()
    }

    private fun scrollToCaret() {
        launch {
            scrollPane?.let {
                val caretPos = runBlocking {
                    vLayout.getCoords(selector.caret.index)
                }

                /**
                 * Square with [CARET_SCROLL_LINE_PADDING] * [vLayout].lineHeight padding around the caret position.
                 */
                val rect = Rectangle(caretPos.x - vLayout.lineHeight * (1 + CARET_SCROLL_LINE_PADDING), caretPos.y - vLayout.lineHeight * (1 + CARET_SCROLL_LINE_PADDING), vLayout.lineHeight * (1 + 2 * CARET_SCROLL_LINE_PADDING), vLayout.lineHeight * (1 + 2 * CARET_SCROLL_LINE_PADDING))
                withContext(Dispatchers.Main) {
                    SwingUtilities.invokeLater {
                        scrollRectToVisible(rect)
                    }
                }
            }
        }
    }

    private fun getAllInlayWidgets(start: Int = 0, end: Int = textModel.length): Set<Widget> {
        val psiFile = psiManager?.getPsiFile(file)
        val inlayWidgets = if (psiFile != null) {
            lang?.psiService?.collectInlayWidgetsInRange(psiFile, start..<end) ?: emptySet()
        } else emptySet()
        return inlayWidgets
    }

    private fun getAllInterlineWidgets(start: Int = 0, end: Int = textModel.length): Set<Widget> {
        val psiFile = psiManager?.getPsiFile(file)
        val interlineWidgets = if (psiFile != null) {
            lang?.psiService?.collectInterlineWidgetsInRange(psiFile, start..<end) ?: emptySet()
        } else emptySet()
        return interlineWidgets
    }

    inner class VirtualLayout {
        private var cachedLines: List<LineInfo> = listOf()

        var fmBase: FontMetrics = getFontMetrics(fontBase)
            set(value) {
                field = value
                internalPadding = (lineHeight - fmBase.height) / 2
            }
        var fmCode: FontMetrics = getFontMetrics(codeFont)
            set(value) {
                field = value
                lineHeight = value.height + 2 * linePadding
                fmColumnWidth = value.charWidth(' ')
                internalPadding = (lineHeight - fmBase.height) / 2
            }

        val linePadding: Int = 2

        var lineHeight: Int = fmCode.height + 2 * linePadding

        var fmColumnWidth: Int = fmCode.charWidth(' ')

        var internalPadding = (lineHeight - fmBase.height) / 2

        val lineIconSize: Int get() = lineHeight
        val lineNumberWidth get() = fmCode.stringWidth((textModel.lines + 1).toString())
        val rowHeaderWidth get() = lineNumberWidth + 2 * lineIconSize + 4 * vLayout.internalPadding
        val foldIndicatorBounds: Rectangle get() = Rectangle(rowHeaderWidth - 2 * internalPadding - lineIconSize, 0, lineIconSize, lineIconSize)
        private val preLineWidgetBounds: Rectangle get() = Rectangle(rowHeaderWidth - 3 * internalPadding - 2 * lineIconSize, 0, lineIconSize, lineIconSize)

        private val bottomPadding = lineHeight * 3
        private val rightPadding = fmColumnWidth * 20

        private fun Widget.calcSize(): Dimension {
            val width = fmBase.stringWidth(content)
            return Dimension(width + 2 * internalPadding, fmBase.height + 2 * internalPadding)
        }

        suspend fun getVisibleLines(visibleRect: Rectangle): VisibleLines = coroutineScope {
            val (startLine, startYOffset) = getLineAtY((-bounds.y).coerceAtLeast(insets.top))
            val (endLine, _) = getLineAtY((-bounds.y + visibleRect.height))

            VisibleLines(cachedLines.subList(startLine.coerceAtLeast(0), endLine + 1), startYOffset)
        }

        suspend fun invalidateAllLines() {
            updateCache()

            withContext(Dispatchers.Main) {
                revalidate()
                repaint()
            }
        }

        private suspend fun updateCache() {
            val possibleLines = psiManager?.lang?.codeFoldingProvider?.getVisibleLines(psiManager.getPsiFile(file), textModel.lines, textModel) ?: List(textModel.lines) {
                LineIndicator(it)
            }
            cachedLines = possibleLines.map { calculateLineInfo(it) }
        }

        private suspend fun calculateLineInfo(indicator: LineIndicator): LineInfo {
            // Implement logic to calculate line information
            // This includes text, widgets, and folding placeholders
            val startIndex = textModel.indexOf(indicator.lineNumber, 0)
            val endIndex = textModel.indexOf(indicator.lineNumber + 1, 0)
            val firstNonWhiteSpaceIndex = selector.indexOfWordEnd(startIndex, Selector.ONLY_SPACES, true)
            val info = LineInfo(
                indicator,
                startIndex,
                endIndex,
                firstNonWhiteSpaceIndex - startIndex,
                firstNonWhiteSpaceIndex == endIndex - 1,
                if (indicator.isFoldedBeginning) indicator.placeHolder else null
            )
            return info
        }

        fun getDocumentSize(): Dimension {
            val possibleLines = cachedLines
            val interlineWidgets = getAllInterlineWidgets().size
            val height = (interlineWidgets + possibleLines.size) * lineHeight + insets.top + insets.bottom + bottomPadding
            val width = rowHeaderWidth + textModel.maxColumns * fmColumnWidth + rightPadding
            return Dimension(width, height)
        }

        suspend fun clickOnRowHeader(point: Point): Boolean {
            val inFoldBounds = (point.x + insets.left + visibleRect.x) in foldIndicatorBounds.x..<(foldIndicatorBounds.x + foldIndicatorBounds.width)
            val (line, _) = getLineAtY(point.y)
            if (inFoldBounds) {
                val realLineNumber = cachedLines[line].lineNumber
                lang?.codeFoldingProvider?.cachedFoldRegions?.get(psiManager?.getPsiFile(file))?.firstOrNull { it.startLine == realLineNumber }?.let {
                    it.isFolded = !it.isFolded
                }
                nativeLog("Click on Fold in line ${line + 1} ($realLineNumber).")
                return true
            }

            val inWidgetBounds = (point.x + insets.left + visibleRect.x) in preLineWidgetBounds.x..<(preLineWidgetBounds.x + preLineWidgetBounds.width)
            if (inWidgetBounds) {
                nativeLog("Click on Widget in line ${line + 1}.")
                return true
            }

            return false
        }

        suspend fun getCoords(index: Int): Point {
            val xOffset = insets.left + rowHeaderWidth
            var yOffset = 0
            var lineStartIndex = 0

            val interlineWidgets = getAllInterlineWidgets(0, index)

            // Add the yOffset added through interlineWidgets
            yOffset += interlineWidgets.size * lineHeight

            // Iterate over cachedLines to find the line containing the index
            for (lineInfo in cachedLines) {

                // Update line start index for the current line
                lineStartIndex = lineInfo.startIndex

                if (index in lineStartIndex..<lineInfo.endIndex) {
                    // Calculate column position within the line
                    val columnInLine = index - lineStartIndex

                    val inlayWidgets = getAllInlayWidgets(lineStartIndex, index)

                    // Calculate x-coordinate based on column position
                    val xPosition = xOffset + columnInLine * fmColumnWidth + inlayWidgets.sumOf { it.calcSize().width }

                    // Calculate y-coordinate
                    yOffset += lineHeight

                    // Return the calculated coordinates
                    return Point(xPosition, yOffset)
                }

                // Move to the next line
                yOffset += lineHeight
            }

            // If the index is out of bounds, return the coordinates of the last character
            return Point(xOffset, yOffset - lineHeight)
        }

        suspend fun getLineAndColumnAt(point: Point): LineColumn {
            val (line, _) = getLineAtY(point.y)
            val column = getColumnAtX(line, point.x)
            val lc = LineColumn.of(cachedLines[line].lineNumber, column)
            return lc
        }

        /**
         * @return the line id to get the LineInfo through [cachedLines] and the yOffset where the line starts.
         */
        private suspend fun getLineAtY(y: Int): Pair<Int, Int> {
            var yOffset = 0
            var lineNumber = 0

            val interlineWidgets = getAllInterlineWidgets()

            while (lineNumber < cachedLines.size) {
                val info = cachedLines[lineNumber]
                val currInterlineWidgets = interlineWidgets.filter { it.index in info.startIndex..<info.endIndex }

                val actualLineHeight = (currInterlineWidgets.size + 1) * lineHeight

                if (y < (yOffset + actualLineHeight)) {
                    return lineNumber to yOffset
                }
                yOffset += actualLineHeight
                lineNumber++
            }
            return lineNumber - 1 to yOffset
        }


        private suspend fun getColumnAtX(line: Int, x: Int): Int {
            // calculate column
            if (x <= insets.left + visibleRect.x + rowHeaderWidth) return 0

            val info = cachedLines[line]
            var xOffset = insets.left + rowHeaderWidth
            var column = 0

            val inlayWidgets = getAllInlayWidgets(info.startIndex, info.endIndex)

            while (column < textModel.maxColumns) {
                val widgetAtCol = inlayWidgets.filter {
                    val lc = textModel.getLineAndColumn(it.index)
                    lc.first == line && lc.second == column
                }

                val columnWidth = fmColumnWidth + widgetAtCol.sumOf { it.calcSize().width }

                if (x + fmColumnWidth / 2 in xOffset..<(xOffset + columnWidth)) {
                    return column
                }

                xOffset += columnWidth
                column++
            }

            return textModel.maxColumns
        }
    }

    inner class Renderer {
        private val strokeWidth: Int = 2
        private val fmCode: FontMetrics get() = vLayout.fmCode
        private val fmBase: FontMetrics get() = vLayout.fmBase

        private val fg: Color get() = UIStates.theme.get().getColor(CodeStyle.BASE0)
        private val bg: Color get() = UIStates.theme.get().COLOR_BG_0

        private val foldIndication: Color get() = UIStates.theme.get().getColor(CodeStyle.YELLOW).alpha(13)
        private val secFGColor: Color get() = UIStates.theme.get().getColor(CodeStyle.BASE4)
        private val secBGColor: Color get() = UIStates.theme.get().getColor(CodeStyle.BASE6)
        private val selColor get() = UIStates.theme.get().getColor(CodeStyle.BLUE).alpha(0x45)
        private val markBGColor get() = UIStates.theme.get().getColor(CodeStyle.BLUE).alpha(0x13)

        private val collapseIcon = UIStates.icon.get().folderClosed.apply {
            colorFilter = ColorFilter {
                if (it == Color.black) {
                    secFGColor
                } else it
            }
        }.derive(vLayout.lineIconSize, vLayout.lineIconSize).image

        private val elapseIcon = UIStates.icon.get().folderOpen.apply {
            colorFilter = ColorFilter {
                if (it == Color.black) {
                    secFGColor
                } else it
            }
        }.derive(vLayout.lineIconSize, vLayout.lineIconSize).image


        suspend fun render(g: Graphics2D, visibleLines: VisibleLines) {
            val psiFile = psiManager?.getPsiFile(file)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            g.background = bg
            g.clearRect(0, 0, width, height)

            var yOffset = visibleLines.yOffset
            val xOffset = insets.left
            val rowHeaderWidth = vLayout.rowHeaderWidth
            val selection = selector.selection.asRange()

            g.color = secBGColor
            val vSplitLineX = xOffset + rowHeaderWidth - vLayout.internalPadding
            g.drawLine(vSplitLineX, 0, vSplitLineX, height)
            val interlineWidgets = getAllInterlineWidgets()
            val inlayWidgets = getAllInlayWidgets()

            visibleLines.lines.forEachIndexed { _, lineInfo ->
                //nativeLog("Rendering ${lineInfo.lineNumber} with height ${vLayout.fmLineHeight} at $xOffset, $yOffset")
                // Render interline widgets

                val currInterlineWidgets = interlineWidgets.filter { it.index in lineInfo.startIndex..<lineInfo.endIndex }
                currInterlineWidgets.forEach {
                    val column = it.index - lineInfo.startIndex
                    if(selection != null && it.index in selection){
                        g.color = selColor
                        g.fillRect(xOffset + rowHeaderWidth, yOffset, width, vLayout.lineHeight)
                    }
                    val widgetDim = g.drawWidget(it, xOffset + rowHeaderWidth + column * vLayout.fmColumnWidth, yOffset)
                    yOffset += widgetDim.height
                }

                val currInlayWidgets = inlayWidgets.filter { it.index in lineInfo.startIndex..<lineInfo.endIndex }

                g.renderLine(psiFile, currInlayWidgets, lineInfo, selection, xOffset + rowHeaderWidth, yOffset)

                g.drawLineNumber(lineInfo, Rectangle(xOffset, yOffset, vLayout.lineNumberWidth, vLayout.lineHeight))

                yOffset += vLayout.lineHeight
            }
            g.drawCaretInfo()
        }

        /**
         * @param lineInfo starting with 0 to [textModel].lines
         *
         * @return height of line (with drawn widgets)
         */
        private fun Graphics2D.renderLine(psiFile: PsiFile?, inlayWidgets: List<Widget>, lineInfo: LineInfo, selection: IntRange?, xOffset: Int, yOffset: Int) {
            var internalXOffset = xOffset

            // Render line text with syntax highlighting
            val startIndex = lineInfo.startIndex
            val endIndex = lineInfo.endIndex
            //nativeLog("Line $lineNumber: ${textModel.substring(startingIndex, endIndex)}")

            val lineContent = textModel.substring(startIndex, endIndex)
            val highlights = lang?.highlightProvider?.fastHighlight(lineContent) ?: emptyList()

            if (selector.caret.line == lineInfo.lineNumber) {
                color = markBGColor
                fillRect(0, yOffset, size.width, vLayout.lineHeight)
            }

            for ((colID, charIndex) in (startIndex until endIndex).withIndex()) {

                val psiElement = psiFile?.let {
                    lang?.psiService?.findElementAt(psiFile, charIndex)
                }

                val psiHighlights = if (psiElement != null) {
                    lang?.highlightProvider?.getHighlights(psiElement)
                } else emptyList()

                val char = lineContent[colID]
                val charWidth = when (char) {
                    '\n' -> width
                    '\t' -> fmCode.charWidth(' ')
                    '\r' -> fmCode.charWidth(' ')
                    else -> fmCode.charWidth(char)
                }

                // Draw Indentation Line
                if (colID != 0 && colID < lineInfo.firstNonWhitespaceCol && !lineInfo.containsOnlySpaces && colID % indentationProvider.spaces == 0) {
                    color = secBGColor
                    drawLine(internalXOffset, yOffset, internalXOffset, yOffset + vLayout.lineHeight)
                }

                // Draw Selection
                selection?.let {
                    if (charIndex in it) {
                        color = selColor
                        fillRect(internalXOffset, yOffset, charWidth, vLayout.lineHeight)
                    }
                }

                // Draw Char
                color = (highlights.firstOrNull { it.range.contains(colID) }?.color ?: psiHighlights?.firstOrNull { it.range.contains(charIndex) }?.color).toColor()

                font = codeFont
                drawString(char.toString(), internalXOffset, yOffset + fmCode.ascent + vLayout.linePadding)

                // Draw Underline
                psiElement?.annotations?.minByOrNull { it.severity }?.let {
                    color = it.severity.toColor().toColor()
                    drawLine(internalXOffset, yOffset + vLayout.lineHeight - vLayout.linePadding, internalXOffset + charWidth, yOffset + vLayout.lineHeight - vLayout.linePadding)
                }

                // Draw Caret
                if (selector.caret.index == charIndex) {
                    color = fg
                    fillRect(internalXOffset, yOffset + vLayout.linePadding, strokeWidth, fmCode.height)
                }

                internalXOffset += charWidth

                // Render inlay widgets
                inlayWidgets.filter { it.index == charIndex }.forEach { widget ->
                    val widgetDim = drawWidget(widget, internalXOffset, yOffset)
                    // Draw Selection under Widget
                    selection?.let {
                        if (charIndex in it) {
                            color = selColor
                            fillRect(internalXOffset, yOffset, widgetDim.width, vLayout.lineHeight)
                        }
                    }
                    internalXOffset += widgetDim.width
                }
            }

            // Render postline widgets
            /*lang?.widgetProvider?.cachedPostLineWidget?.filter { textModel.getLineAndColumn(it.position).first == lineInfo.lineNumber }?.forEach {
                val widgetDim = drawWidget(it, internalXOffset, yOffset)
                internalXOffset += widgetDim.width
            }*/

            // Draw EOF Caret
            if (endIndex == textModel.length && selector.caret.index == textModel.length && selector.caret.line == lineInfo.lineNumber) {
                color = fg
                fillRect(internalXOffset, yOffset + vLayout.linePadding, strokeWidth, fmCode.height)
            }

            if (lineInfo.foldingPlaceholder != null) {
                val width = fmCode.stringWidth(lineInfo.foldingPlaceholder)
                color = foldIndication
                fillRect(internalXOffset, yOffset, width, vLayout.lineHeight)

                color = fg
                drawString(lineInfo.foldingPlaceholder, internalXOffset, yOffset + vLayout.linePadding + fmCode.ascent)
            }
        }

        private fun Graphics2D.drawLineNumber(lineInfo: LineInfo, rect: Rectangle) {
            color = secFGColor
            font = fontBase
            drawString((lineInfo.lineNumber + 1).toString(), rect.x + rect.width - fmBase.stringWidth((lineInfo.lineNumber + 1).toString()), rect.y + vLayout.lineHeight / 2 - fmBase.height / 2 + fmBase.ascent)

            val foldRegions = lang?.codeFoldingProvider?.cachedFoldRegions ?: return

            foldRegions[psiManager?.getPsiFile(file)]?.firstOrNull { it.startLine == lineInfo.lineNumber }?.let {
                val bounds = vLayout.foldIndicatorBounds
                if (it.isFolded) {
                    drawImage(collapseIcon, rect.x + bounds.x, rect.y, null)
                } else {
                    drawImage(elapseIcon, rect.x + bounds.x, rect.y, null)
                }
            }
        }

        /**
         * Draws a Widget and returns the necessary Dimension.
         */
        private fun Graphics2D.drawWidget(widget: Widget, xOffset: Int, yOffset: Int): Dimension {
            val widgetContentWidth = fmBase.getStringBounds(widget.content, this)
            val contentRect = Rectangle(xOffset + vLayout.internalPadding, yOffset + vLayout.lineHeight / 2 - widgetContentWidth.height.toInt() / 2, widgetContentWidth.width.toInt(), widgetContentWidth.height.toInt())

            if (widget.type != Widget.Type.INTERLINE) {
                val bgRect = Rectangle(contentRect.x - vLayout.internalPadding / 2, contentRect.y - vLayout.internalPadding / 2, contentRect.width + vLayout.internalPadding, contentRect.height + vLayout.internalPadding)
                color = secBGColor
                fillRoundRect(bgRect.x, bgRect.y, bgRect.width, bgRect.height, vLayout.internalPadding, vLayout.internalPadding)
            }

            color = secFGColor
            font = fontBase
            drawString(widget.content, contentRect.x, contentRect.y + fmBase.ascent)

            val widgetDimension = Dimension(widgetContentWidth.width.toInt() + vLayout.internalPadding * 2, vLayout.lineHeight)

            return widgetDimension
        }

        private fun Graphics2D.drawCaretInfo() {
            color = secFGColor
            font = fontBase
            val infoStr = "${selector.caret.line + 1}:${selector.caret.col + 1}"
            val strWidth = fmBase.stringWidth(infoStr)

            drawString(infoStr, visibleRect.width - bounds.x - strWidth - insets.right, visibleRect.height - bounds.y - insets.bottom - vLayout.lineHeight + vLayout.linePadding + fmCode.ascent)
        }

        private fun Int?.toColor(): Color {
            return if (this == null) fg else Color(this)
        }

    }

    inner class InputHandler : MouseListener, MouseMotionListener {
        private var hoverJob: Job? = null
        private var lastMouseX: Int = 0
        private var lastMouseY: Int = 0

        override fun mouseClicked(e: MouseEvent) {
            launch {
                vLayout.clickOnRowHeader(e.point)
                if (e.clickCount == 2) {
                    val line = vLayout.getLineAndColumnAt(e.point)
                    val index = textModel.indexOf(line.line, line.column)
                    selector.selectCurrentWord(index, Selector.DEFAULT_SYMBOL_CHARS, true)
                }
                invalidateContent(this@PerformantCodeEditor)
            }
        }

        override fun mousePressed(e: MouseEvent) {
            launch {
                val lc = vLayout.getLineAndColumnAt(e.point)
                selector.internalMoveCaret(lc.line, lc.column, e.isShiftDown)
                repaint()
            }
            requestFocusInWindow()
        }

        override fun mouseReleased(e: MouseEvent?) {

        }

        override fun mouseEntered(e: MouseEvent?) {

        }

        override fun mouseExited(e: MouseEvent?) {

        }

        override fun mouseDragged(e: MouseEvent) {
            launch {
                val (line, column) = vLayout.getLineAndColumnAt(e.point)
                selector.internalMoveCaret(line, column, true)
                repaint()
            }
        }

        override fun mouseMoved(e: MouseEvent) {
            lastMouseX = e.x
            lastMouseY = e.y

            hoverJob?.cancel()

            SwingUtilities.invokeLater {
                modificationOverlay.makeInvisible()
            }

            hoverJob = launch {
                delay(100)

                val (line, column) = vLayout.getLineAndColumnAt(e.point)
                val index = textModel.indexOf(line, column)
                val psiFile = psiManager?.getPsiFile(file)
                if (psiFile != null) {
                    val annotations = lang?.psiService?.findElementAt(psiFile, index)?.annotations ?: emptyList()
                    if (annotations.isNotEmpty()) {
                        SwingUtilities.invokeLater {
                            modificationOverlay.showOverlay(annotations.sortedBy { it.severity }, e.x, e.y, this@PerformantCodeEditor)
                        }
                    }
                }
            }
        }
    }

    inner class KeyHandler : KeyAdapter() {
        private var completionJob: Job? = null
        override fun keyTyped(e: KeyEvent) {
            // Character Insertion
            when {
                e.keyChar.isISOControl() -> {

                }

                e.keyChar.isDefined() && !e.isControlDown -> {
                    val newChar = e.keyChar.toString()
                    textStateModel.deleteSelection(selector)
                    textStateModel.insert(selector.caret, newChar)
                }
            }
            scrollToCaret()
            fetchCompletions()
            invalidateContent(this@PerformantCodeEditor)
            e.consume()
        }

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_TAB -> {
                    if (e.isShiftDown) {
                        // remove Indent
                        indentationProvider.unindentSelection(selector)
                    } else {
                        // indent
                        indentationProvider.indentSelection(selector)
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
                        textStateModel.deleteSelection(selector)

                        content?.let { text ->
                            textStateModel.insert(selector.caret, text.replace("\t", " "))
                        }
                    }
                }

                KeyEvent.VK_X -> {
                    if (e.isControlDown) {
                        val selected = textModel.substring(selector.selection)
                        if (selected.isNotEmpty()) {
                            copyToClipboard(selected)
                            textStateModel.deleteSelection(selector)
                        }
                    }
                }

                KeyEvent.VK_SPACE -> {
                    when {
                        e.isControlDown -> {
                            fetchCompletions(true)
                        }
                    }
                }

                KeyEvent.VK_ENTER -> {
                    when {
                        e.isAltDown -> {
                            launch {
                                val annotations = lang?.annotationProvider?.cachedNotations?.get(psiManager?.getPsiFile(file))?.filter { selector.caret.index in it.range } ?: listOf()
                                val coords = vLayout.getCoords(selector.caret.index)
                                if (annotations.isNotEmpty()) {
                                    SwingUtilities.invokeLater {
                                        modificationOverlay.showOverlay(annotations, coords.x, coords.y + vLayout.lineHeight, this@PerformantCodeEditor)
                                    }
                                }
                            }
                        }

                        else -> {
                            textStateModel.deleteSelection(selector)
                            textStateModel.insert(selector.caret, "\n")
                        }
                    }
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
                        textStateModel.deleteSelection(selector)
                    } else {
                        if (selector.caret.index > 0) {
                            textStateModel.delete(selector.caret.index - 1, selector.caret.index)
                            selector.moveCaretLeft(1, false)
                        }
                    }
                }

                KeyEvent.VK_DELETE -> {
                    if (selector.selection.valid()) {
                        val deleted = textStateModel.deleteSelection(selector)
                    } else {
                        if (selector.caret.index < textModel.length) {
                            textStateModel.delete(selector.caret.index, selector.caret.index + 1)
                        }
                    }
                }

                KeyEvent.VK_HOME -> {
                    selector.home(e.isShiftDown, e.isControlDown)
                }

                KeyEvent.VK_END -> {
                    selector.end(e.isShiftDown, e.isControlDown)
                }

                KeyEvent.VK_F -> {
                    // TODO if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.FIND)
                }

                KeyEvent.VK_R -> {
                    // TODO if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.REPLACE)
                }

                KeyEvent.VK_L -> {
                    if (e.isControlDown && e.isAltDown) {
                        psiManager?.getPsiFile(file)?.let { psiFile ->
                            val string = lang?.formatter?.formatted(psiFile)
                            string?.let { content ->
                                textStateModel.replaceAll(content)
                            }
                        }
                    }
                }

                // Save File
                KeyEvent.VK_S -> {
                    if (e.isControlDown) {
                        saveToFile()
                    }
                }
            }
            scrollToCaret()
            fetchCompletions(onlyHide = true)
            invalidateContent(this@PerformantCodeEditor)
            e.consume()
        }

        override fun keyReleased(e: KeyEvent?) {}

        private fun fetchCompletions(showIfPrefixIsEmpty: Boolean = false, onlyHide: Boolean = false) {
            completionJob?.cancel()

            SwingUtilities.invokeLater {
                modificationOverlay.makeInvisible()
            }

            if (!onlyHide) {
                completionJob = launch {
                    try {
                        val prefixIndex = selector.indexOfWordStart(selector.caret.index, Selector.DEFAULT_SPACING_SET, false)
                        val prefix = textModel.substring(prefixIndex, selector.caret.index)
                        if (showIfPrefixIsEmpty || prefix.isNotEmpty()) {
                            val completions = lang?.completionProvider?.fetchCompletions(prefix, currentElement, psiManager?.getPsiFile(file)) ?: emptyList()
                            val coords = vLayout.getCoords(selector.caret.index)
                            if (completions.isNotEmpty()) {
                                SwingUtilities.invokeLater {
                                    modificationOverlay.showOverlay(completions, coords.x, coords.y + vLayout.lineHeight, this@PerformantCodeEditor)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        nativeWarn("Completion canceled by edit.")
                    }
                }
            }
        }
    }

    data class VisibleLines(val lines: List<LineInfo>, val yOffset: Int)


}