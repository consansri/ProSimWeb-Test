package prosim.uilib.styled.editor3

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.editor.folding.LineIndicator
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
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import prosim.uilib.UIStates
import prosim.uilib.alpha
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingUtilities

class PerformantCodeEditor(
    override val file: VirtualFile,
    project: Project,
) : JComponent(), CodeEditor, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {
    companion object {
        val DEFAULT_SYMBOL_CHARS = ('a'.rangeTo('z') + 'A'.rangeTo('Z') + '0'.rangeTo('9') + '_').toCharArray()
    }

    override val psiManager: PsiManager<*>? = project.getManager(file)

    override val textModel: TextModel = RopeModel(file.getAsUTF8String())
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }
    override val textStateModel: TextStateModel = TextStateModel(textModel, selector)
    val lang: LanguageService? get() = psiManager?.lang

    private var scrollPane: CScrollPane? = null

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

    private val modificationOverlay: ModificationOverlay<EditorModification> = ModificationOverlay<EditorModification>(this)

    init {
        isFocusable = true
        cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        border = BorderFactory.createEmptyBorder(0, vLayout.internalPadding, 0, vLayout.internalPadding)

        project.register(this) // to listen to file changes from other sources

        file.onDiskChange = {
            loadFromFile()
            nativeLog("Reloaded Editor Content")
            revalidate()
            repaint()
        }

        loadFromFile()

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
        addKeyListener(keyHandler)

        invalidateContent()
    }

    fun createScrollPane(): CScrollPane {
        val sp = CScrollPane(true, this).apply {
            CScrollPane.removeArrowKeyScrolling(this)
            verticalScrollBar.blockIncrement = height
            horizontalScrollBar.blockIncrement = width
            verticalScrollBar.unitIncrement = vLayout.lineHeight * 3
            horizontalScrollBar.unitIncrement = vLayout.fmColumnWidth * 3
        }
        scrollPane = sp
        return sp
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

    fun invalidateContent() {
        updateJob.getAndSet(launch {
            updateLayoutAndWidgets()
        })?.cancel()
    }

    private suspend fun updateLayoutAndWidgets() {
        vLayout.invalidateAllLines()
        // TODO update psi
    }

    inner class VirtualLayout {
        private var cachedLines: List<LineInfo> = listOf()
        var fmBase = getFontMetrics(fontBase) // TODO make listen to Theme/Scale changes
            set(value) {
                field = value
                internalPadding = lineHeight - fmBase.height / 2
            }
        var fmCode = getFontMetrics(codeFont) // TODO make listen to Theme/Scale changes
            set(value) {
                field = value
                lineHeight = value.height + 2 * linePadding
                fmColumnWidth = value.charWidth(' ')
                internalPadding = lineHeight - fmBase.height / 2
            }

        val linePadding: Int = 2

        var lineHeight: Int = fmCode.height + 2 * linePadding

        var fmColumnWidth: Int = fmCode.charWidth(' ') // TODO make listen to Theme/Scale changes

        var internalPadding = (lineHeight - fmBase.height) / 2

        val lineIconSize: Int get() = lineHeight
        val lineNumberWidth get() = fmCode.stringWidth((textModel.lines + 1).toString())
        val rowHeaderWidth get() = lineNumberWidth + 2 * lineIconSize + 4 * vLayout.internalPadding
        val foldIndicatorBounds: Rectangle get() = Rectangle(rowHeaderWidth - 2 * internalPadding - lineIconSize, 0, lineIconSize, lineIconSize)
        val preLineWidgetBounds: Rectangle get() = Rectangle(rowHeaderWidth - 3 * internalPadding - 2 * lineIconSize, 0, lineIconSize, lineIconSize)

        private val bottomPadding = lineHeight * 3
        private val rightPadding = fmColumnWidth * 20

        private fun Widget.calcSize(): Dimension {
            val width = fmBase.stringWidth(content)
            return Dimension(width + 2 * internalPadding, fmBase.height + 2 * internalPadding)
        }

        suspend fun getVisibleLines(visibleRect: Rectangle): VisibleLines = coroutineScope {
            val (startLine, startYOffset) = getLineAtY((-bounds.y).coerceAtLeast(insets.top))
            val (endLine, endYOffset) = getLineAtY((-bounds.y + visibleRect.height))

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
            val possibleLines = psiManager?.lang?.codeFoldingProvider?.getVisibleLines(textModel.lines) ?: List(textModel.lines) {
                LineIndicator(it)
            }
            cachedLines = possibleLines.map { calculateLineInfo(it) }
        }

        private suspend fun calculateLineInfo(indicator: LineIndicator): LineInfo {
            // Implement logic to calculate line information
            // This includes text, widgets, and folding placeholders
            val startIndex = textModel.indexOf(indicator.lineNumber, 0)
            val endIndex = textModel.indexOf(indicator.lineNumber + 1, 0)
            val info = LineInfo(indicator, startIndex, endIndex, psiManager.getInterlineWidgets(indicator.lineNumber), psiManager.getInlayWidgets(indicator.lineNumber), if (indicator.isFoldedBeginning) indicator.placeHolder else null)
            return info
        }

        fun getDocumentSize(): Dimension {
            val possibleLines = cachedLines
            val interlineWidgets = possibleLines.sumOf { it.interlineWidgets.size }
            val height = (interlineWidgets + possibleLines.size) * lineHeight + insets.top + insets.bottom + bottomPadding
            val width = rowHeaderWidth + textModel.maxColumns * fmColumnWidth + rightPadding
            return Dimension(width, height)
        }

        suspend fun clickOnRowHeader(point: Point): Boolean {
            val inFoldBounds = (point.x + insets.left + visibleRect.x) in foldIndicatorBounds.x..<(foldIndicatorBounds.x + foldIndicatorBounds.width)
            val (line, yOffset) = getLineAtY(point.y)
            if (inFoldBounds) {
                val realLineNumber = cachedLines[line].lineNumber
                lang?.codeFoldingProvider?.cachedFoldRegions?.firstOrNull { it.startLine == realLineNumber }?.let {
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
            var yOffset = 0
            val xOffset = insets.left + rowHeaderWidth

            for (lineInfo in cachedLines) {

                if (index >= lineInfo.startIndex && index <= lineInfo.endIndex) {
                    // Found the line containing the index
                    val lineStartIndex = lineInfo.startIndex
                    val columnInLine = index - lineStartIndex

                    // Calculate y-coordinate
                    yOffset += lineInfo.interlineWidgets.size * lineHeight

                    var currentColumn = 0
                    var currentXOffset = xOffset

                    while (currentColumn < columnInLine) {
                        val widgetAtCol = lineInfo.inlayWidgets.firstOrNull { it.position.col == currentColumn }
                        if (widgetAtCol != null) {
                            currentXOffset += widgetAtCol.calcSize().width
                        }
                        currentXOffset += fmColumnWidth
                        currentColumn++
                    }

                    return Point(currentXOffset, yOffset)
                }

                // Move to the next line
                yOffset += (lineInfo.interlineWidgets.size + 1) * lineHeight
            }

            // If index is out of bounds, return the coordinates of the last character
            return Point(xOffset, yOffset - lineHeight)
        }

        suspend fun getLineAndColumnAt(point: Point): LineColumn {
            val (line, yOffset) = getLineAtY(point.y)
            val column = getColumnAtX(line, point.x)
            val lc = LineColumn.of(cachedLines[line].lineNumber, column)
            return lc
        }

        /**
         * @return the lineid to get the LineInfo through [cachedLines] and the yOffset where the line starts.
         */
        private suspend fun getLineAtY(y: Int): Pair<Int, Int> {
            var yOffset = 0
            var lineNumber = 0
            while (lineNumber < cachedLines.size) {
                val info = cachedLines[lineNumber]
                val actualLineHeight = (info.interlineWidgets.size + 1) * lineHeight

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
            if (x <= insets.left - bounds.y + rowHeaderWidth) return 0

            val info = cachedLines[line]
            var xOffset = insets.left + rowHeaderWidth
            var column = 0

            while (column < textModel.maxColumns) {
                val widgetAtCol = info.inlayWidgets.firstOrNull { it.position.col == column }
                val columnWidth = if (widgetAtCol != null) {
                    widgetAtCol.calcSize().width + fmColumnWidth
                } else {
                    fmColumnWidth
                }

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

        private var fg: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        private var bg: Color = UIStates.theme.get().globalLaF.bgPrimary

        private val foldIndication: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.YELLOW).alpha(13)
        private val secFGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE4)
        private val secBGColor: Color = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE7)

        private val selColor = UIStates.theme.get().codeLaF.getColor(CodeStyle.BLUE).alpha(0x55)
        private val markBGColor = UIStates.theme.get().codeLaF.getColor(CodeStyle.BLUE).alpha(0x13)

        private val collapseIcon = UIStates.icon.get().folderClosed.apply {
            colorFilter = ColorFilter() {
                secFGColor
            }
        }

        private val ellapseIcon = UIStates.icon.get().folderOpen.apply {
            colorFilter = ColorFilter {
                secFGColor
            }
        }

        suspend fun render(g: Graphics2D, visibleLines: VisibleLines) {
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

            visibleLines.lines.forEachIndexed { index, lineInfo ->
                //nativeLog("Rendering ${lineInfo.lineNumber} with height ${vLayout.fmLineHeight} at $xOffset, $yOffset")
                // Render interline widgets
                lineInfo.interlineWidgets.filter { it.position.line == lineInfo.lineNumber }.forEach {
                    val widgetDimension = g.drawWidget(it, xOffset + rowHeaderWidth, yOffset)
                    yOffset += widgetDimension.height
                }

                g.renderLine(lineInfo, selection, xOffset + rowHeaderWidth, yOffset)

                g.drawLineNumber(lineInfo, Rectangle(xOffset, yOffset, vLayout.lineNumberWidth, vLayout.lineHeight))

                yOffset += vLayout.lineHeight
            }
            g.drawCaretInfo()
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
                val char = lineContent[colID]
                val charWidth = fmCode.charWidth(char)

                // Draw Selection
                selection?.let {
                    if (charIndex in it) {
                        color = selColor
                        fillRect(internalXOffset, internalYOffset, if (char == '\n') width else charWidth, vLayout.lineHeight)
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
                drawString(char.toString(), internalXOffset, internalYOffset + fmCode.ascent + vLayout.linePadding)

                // Draw Underline
                lang?.annotationProvider?.cachedAnnotations?.firstOrNull {
                    it.range.contains(charIndex)
                }?.let {
                    color = it.severity.toColor(lang).toColor()
                    fillRect(internalXOffset, internalYOffset + vLayout.lineHeight - vLayout.linePadding, charWidth, strokeWidth)
                }

                // Draw Caret
                if (selector.caret.index == charIndex) {
                    color = foreground
                    fillRect(internalXOffset, internalYOffset + vLayout.linePadding, strokeWidth, fmCode.height)
                }

                internalXOffset += charWidth

                // Render inlay widgets
                lineInfo.inlayWidgets.filter { it.position.index == charIndex }.forEach {

                    val widgetDim = drawWidget(it, internalXOffset, internalYOffset)
                    // Draw Selection under Widget
                    selection?.let {
                        if (charIndex in it) {
                            color = selColor
                            fillRect(internalXOffset, internalYOffset, widgetDim.width, vLayout.lineHeight)
                        }
                    }
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
                color = fg
                fillRect(internalXOffset, internalYOffset + vLayout.linePadding, strokeWidth, fmCode.height)
            }

            if (lineInfo.foldingPlaceholder != null) {
                val width = fmCode.stringWidth(lineInfo.foldingPlaceholder)
                color = foldIndication
                fillRect(internalXOffset, internalYOffset, width, vLayout.lineHeight)

                color = fg
                drawString(lineInfo.foldingPlaceholder, internalXOffset, internalYOffset + vLayout.linePadding + fmCode.ascent)
            }
        }

        private fun Graphics2D.drawLineNumber(lineInfo: LineInfo, rect: Rectangle) {
            color = secFGColor
            font = fontBase
            drawString((lineInfo.lineNumber + 1).toString(), rect.x + rect.width - fmBase.stringWidth((lineInfo.lineNumber + 1).toString()), rect.y + vLayout.lineHeight / 2 - fmBase.height / 2 + fmBase.ascent)

            val foldRegions = lang?.codeFoldingProvider?.cachedFoldRegions ?: return

            foldRegions.firstOrNull { it.startLine == lineInfo.lineNumber }?.let {
                val bounds = vLayout.foldIndicatorBounds
                if (it.isFolded) {
                    drawImage(collapseIcon.derive(bounds.width, bounds.height).image, rect.x + bounds.x, rect.y, null)
                } else {
                    drawImage(ellapseIcon.derive(bounds.width, bounds.height).image, rect.x + bounds.x, rect.y, null)
                }
            }
        }

        /**
         * Draws a Widget and returns the needed Dimension.
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
            val infoStr = "${selector.caret.line}:${selector.caret.col}"
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
                    selector.selectCurrentWord(index, DEFAULT_SYMBOL_CHARS, true)
                }
                invalidateContent()
            }
        }

        override fun mousePressed(e: MouseEvent) {
            launch {
                val lc = vLayout.getLineAndColumnAt(e.point)
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
                val (line, column) = vLayout.getLineAndColumnAt(e.point)
                selector.moveCaretTo(line, column, true)
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
                val annotations = lang?.annotationProvider?.cachedAnnotations?.filter { index in it.range } ?: listOf()

                if (annotations.isNotEmpty()) {
                    SwingUtilities.invokeLater {
                        modificationOverlay.showOverlay(annotations, e.x, e.y, this@PerformantCodeEditor)
                    }
                }
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
            invalidateContent()
        }

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_TAB -> {
                    if (e.isShiftDown) {
                        // remove Indent TODO
                    } else {
                        // indent TODO
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
                    when {
                        e.isAltDown -> {
                            launch {
                                val annotations = lang?.annotationProvider?.cachedAnnotations?.filter { selector.caret.index in it.range } ?: listOf()
                                val coords = vLayout.getCoords(selector.caret.index)
                                if (annotations.isNotEmpty()) {
                                    SwingUtilities.invokeLater {
                                        modificationOverlay.showOverlay(annotations, coords.x, coords.y + vLayout.lineHeight, this@PerformantCodeEditor)
                                    }
                                }
                            }
                        }

                        else -> {
                            textStateModel.delete(selector.selection)
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
            invalidateContent()
        }

        override fun keyReleased(e: KeyEvent?) {}
    }

    data class VisibleLines(val lines: List<LineInfo>, val yOffset: Int)


}