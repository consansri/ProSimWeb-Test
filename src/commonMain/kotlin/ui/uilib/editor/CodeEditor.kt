package ui.uilib.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.toSize
import cengine.editor.annotation.Annotation
import cengine.editor.annotation.Severity
import cengine.editor.completion.Completion
import cengine.editor.highlighting.HLInfo
import cengine.lang.asm.CodeStyle
import cengine.project.Project
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiReference
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.*
import ui.uilib.ComposeTools
import ui.uilib.ComposeTools.delete
import ui.uilib.ComposeTools.insert
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.params.IconType
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@Composable
fun CodeEditor(
    file: VirtualFile,
    project: Project,
    codeStyle: TextStyle,
    codeSmallStyle: TextStyle,
    baseSmallStyle: TextStyle,
    modifier: Modifier = Modifier
) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icon = UIState.Icon.value

    val manager = remember { project.getManager(file) }
    val lang = remember { manager?.lang }
    val service = remember { lang?.psiService }

    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(file.getAsUTF8String())) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val spanStyles = remember { mutableListOf<AnnotatedString.Range<SpanStyle>>() }

    var lineNumberLabelingBounds by remember { mutableStateOf<Size>(Size.Zero) }
    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var rowHeaderWidth by remember { mutableStateOf<Float>(0f) }

    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    var caretOffset by remember { mutableStateOf<Offset>(Offset(0f, 0f)) }
    var currentElement by remember { mutableStateOf<PsiElement?>(null) }
    var references by remember { mutableStateOf<List<PsiReference>>(emptyList()) }

    var analyticsAreUpToDate by remember { mutableStateOf(false) }
    var annotationOverlayJob by remember { mutableStateOf<Job?>(null) }
    var allAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var localAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var isAnnotationVisible by remember { mutableStateOf(false) }

    var completionOverlayJob by remember { mutableStateOf<Job?>(null) }
    var completions by remember { mutableStateOf<List<Completion>>(emptyList()) }
    var isCompletionVisible by remember { mutableStateOf(false) }
    var selectedCompletionIndex by remember { mutableStateOf(0) }


    fun buildSpanStyles(code: String, psiFile: PsiFile? = null) {
        // Fast Lexing Highlighting
        spanStyles.clear()
        val hls = emptyList<HLInfo>()// lang?.highlightProvider?.fastHighlight(code) ?: emptyList()
        spanStyles.addAll(hls.mapNotNull {
            if (!it.range.isEmpty()) {
                AnnotatedString.Range<SpanStyle>(SpanStyle(color = Color(it.color or 0xFF000000.toInt())), it.range.first, it.range.last + 1)
            } else null
        })

        if (service == null || psiFile == null) return

        // Complete Highlighting
        allAnnotations = service.collectNotations(psiFile)
        // Annotation Styles
        spanStyles.addAll(allAnnotations.mapNotNull {
            val annoCodeStyle = it.severity.color ?: CodeStyle.BASE0
            val style = SpanStyle(textDecoration = TextDecoration.Underline, color = theme.getColor(annoCodeStyle))
            if (!it.range.isEmpty()) {
                AnnotatedString.Range<SpanStyle>(style, it.range.first, it.range.last + 1)
            } else null
        })

        // Highlight Styles
        spanStyles.addAll(service.collectHighlights(psiFile).mapNotNull { (range, style) ->
            if (!range.isEmpty()) {
                AnnotatedString.Range<SpanStyle>(SpanStyle(color = theme.getColor(style)), range.first, range.last + 1)
            } else null
        })
    }

    fun onTextChange(new: TextFieldValue) {
        val oldText = textFieldValue.annotatedString
        val newText = new.annotatedString

        when {
            newText.length > oldText.length -> {
                // Insert
                analyticsAreUpToDate = false
                val startIndex = oldText.commonPrefixWith(newText).length
                val length = newText.length - oldText.length
                manager?.inserted(file, startIndex, length)
                spanStyles.insert(startIndex, length)
            }

            newText.length < oldText.length -> {
                // Delete
                analyticsAreUpToDate = false
                val startIndex = newText.commonPrefixWith(oldText).length
                val length = oldText.length - newText.length
                manager?.deleted(file, startIndex, startIndex + length)
                spanStyles.delete(startIndex, startIndex + length)
            }

            newText != oldText -> {
                analyticsAreUpToDate = false
            }
        }

        textFieldValue = new
    }

    suspend fun locatePSIElement() {
        val time = measureTime {
            val caretPosition = textFieldValue.selection.start
            val psiFile = manager?.getPsiFile(file)
            currentElement = if (psiFile != null) {
                lang?.psiService?.findElementAt(psiFile, caretPosition)
            } else null
        }
        if (time.inWholeMilliseconds > 5) nativeLog("locatePSIElement took ${time.inWholeMilliseconds}ms")
    }

    fun analyze() {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                manager?.updatePsi(file) {
                    buildSpanStyles(it.content, it)
                    onTextChange(textFieldValue.copy(it.content))
                    locatePSIElement()
                    analyticsAreUpToDate = true
                }
            }
        }
    }

    fun insertCompletion(completion: Completion) {
        val start = textFieldValue.selection.start
        val newText = textFieldValue.annotatedString.subSequence(0, start) + AnnotatedString(completion.insertion) + textFieldValue.annotatedString.subSequence(start, textFieldValue.annotatedString.length)

        onTextChange(textFieldValue.copy(annotatedString = newText, selection = TextRange(start + completion.insertion.length)))
    }

    fun fetchCompletions(showIfPrefixIsEmpty: Boolean = false, onlyHide: Boolean = false) {
        completionOverlayJob?.cancel()

        if (!onlyHide) {
            completionOverlayJob = coroutineScope.launch {
                val time = measureTime {
                    val layout = textLayout
                    if (layout != null) {
                        try {
                            val lineIndex = layout.getLineForOffset(textFieldValue.selection.start)
                            val lineStart = layout.getLineStart(lineIndex)
                            val lineContentBefore = textFieldValue.annotatedString.substring(lineStart, textFieldValue.selection.start)

                            if (showIfPrefixIsEmpty || lineContentBefore.isNotEmpty()) {
                                completions = lang?.completionProvider?.fetchCompletions(lineContentBefore, currentElement, manager?.getPsiFile(file)) ?: emptyList()
                            } else {
                                completions = emptyList()
                            }
                        } catch (e: Exception) {
                            nativeWarn("Completion canceled by edit.")
                        }
                    }
                }

                if (time.inWholeMilliseconds > 5) nativeLog("fetchCompletions took ${time.inWholeMilliseconds}ms")
            }
        }
    }

    with(LocalDensity.current) {
        BasicTextField(
            modifier = Modifier
                .onGloballyPositioned {
                    textFieldSize = it.size.toSize()
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when {
                            !keyEvent.isShiftPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> {
                                when (keyEvent.key) {
                                    Key.Tab -> {
                                        onTextChange(
                                            if (keyEvent.isShiftPressed) {
                                                if (textFieldValue.selection.length == 0) {
                                                    removeIndentation(textFieldValue, textLayout)
                                                } else {
                                                    removeIndentationForSelectedLines(textFieldValue, textLayout)
                                                }
                                            } else {
                                                if (textFieldValue.selection.length == 0) {
                                                    insertIndentation(textFieldValue)
                                                } else {
                                                    insertIndentationForSelectedLines(textFieldValue, textLayout)
                                                }
                                            }
                                        )
                                        true
                                    }

                                    Key.Enter -> {
                                        val completion = completions.getOrNull(selectedCompletionIndex)
                                        if (isCompletionVisible && completion != null) {
                                            insertCompletion(completion)
                                        } else {
                                            onTextChange(insertNewlineAndIndent(textFieldValue, textLayout))
                                        }

                                        true
                                    }

                                    Key.DirectionDown -> {
                                        if (isCompletionVisible && selectedCompletionIndex < completions.size - 1) {
                                            selectedCompletionIndex++
                                            true
                                        } else {
                                            false
                                        }
                                    }

                                    Key.DirectionUp -> {
                                        if (isCompletionVisible && selectedCompletionIndex > 0) {
                                            selectedCompletionIndex--
                                            true
                                        } else {
                                            false
                                        }
                                    }


                                    else -> false
                                }
                            }

                            keyEvent.isShiftPressed && keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> {
                                when (keyEvent.key) {
                                    Key.S -> {
                                        analyze()

                                        true
                                    }

                                    else -> false
                                }
                            }

                            else -> false
                        }

                    } else {
                        false
                    }
                },
            value = textFieldValue,
            cursorBrush = SolidColor(theme.COLOR_FG_0),
            textStyle = codeStyle.copy(
                color = theme.COLOR_FG_0
            ),
            visualTransformation = { str ->
                val (value, time) = measureTimedValue {
                    TransformedText(AnnotatedString(str.text, spanStyles), OffsetMapping.Identity)
                }
                if (time.inWholeMilliseconds > 5) nativeLog("visualTransformation took ${time.inWholeMilliseconds}ms")
                value
            },
            onValueChange = { newValue ->
                val time = measureTime {
                    onTextChange(newValue)
                }
                if (time.inWholeMilliseconds > 5) nativeLog("onValueChange took ${time.inWholeMilliseconds}ms")
            },
            onTextLayout = { result ->
                textLayout = result
                setLineCount(result.lineCount)
                setLineHeight(result.multiParagraph.height / result.lineCount)
            }
        ) { textField ->

            Box(
                Modifier
                    .fillMaxSize()
            ) {
                // Globally Paint Box
                Box(
                    Modifier.matchParentSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val selection = textFieldValue.selection
                        val line = textLayout?.getLineForOffset(selection.start) ?: 0
                        val column = selection.start - (textLayout?.getLineStart(line) ?: 0)

                        currentElement?.let { element ->
                            val path = service?.path(element) ?: return@let
                            Text(path.joinToString(" > ") { it.pathName }, modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_1)
                        }

                        Text("${line + 1}:${column + 1}", modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                }

                Box(
                    Modifier.matchParentSize()
                        .verticalScroll(scrollVertical)
                ) {

                    // Add a light blue background for the current line
                    textLayout?.let { layout ->
                        val lineTop: Float
                        val lineBottom: Float

                        if (textFieldValue.selection.collapsed) {
                            val currentLine = layout.getLineForOffset(textFieldValue.selection.start)
                            lineTop = layout.getLineTop(currentLine)
                            lineBottom = layout.getLineBottom(currentLine)

                        } else {
                            val minLine = layout.getLineForOffset(textFieldValue.selection.min)
                            val maxLine = layout.getLineForOffset(textFieldValue.selection.max)
                            lineTop = layout.getLineTop(minLine)
                            lineBottom = layout.getLineBottom(maxLine)
                        }

                        Box(
                            modifier = Modifier
                                .offset(y = lineTop.toDp())
                                .height((lineBottom - lineTop).toDp())
                                .fillMaxWidth()
                                .background(theme.COLOR_SELECTION.copy(alpha = 0.10f))  // Light blue with 10% opacity
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        // Scroll Container

                        Box(
                            modifier = Modifier
                                .padding(horizontal = scale.SIZE_INSET_MEDIUM)
                                .height(textFieldSize.height.toDp())
                                .width(lineNumberLabelingBounds.width.toDp())
                                .onGloballyPositioned {
                                    rowHeaderWidth = it.size.toSize().width
                                }
                        ) {
                            repeat(lineCount) { line ->
                                val thisLineContent = (line + 1).toString()
                                val thisLineTop = textLayout?.multiParagraph?.getLineTop(line) ?: (lineHeight * line)
                                Text(
                                    modifier = Modifier
                                        .width(lineNumberLabelingBounds.width.toDp())
                                        .height(lineHeight.toDp()).offset(y = (thisLineTop).toDp() + (lineHeight.toDp() - lineNumberLabelingBounds.height.toDp()) / 2),
                                    textAlign = TextAlign.Right,
                                    text = thisLineContent,
                                    color = theme.COLOR_FG_1,
                                    style = codeSmallStyle
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(textFieldSize.height.toDp())
                                .width(scale.SIZE_BORDER_THICKNESS)
                                .background(theme.COLOR_BORDER)
                        )

                        Box(
                            Modifier.fillMaxWidth()
                                .horizontalScroll(scrollHorizontal)
                        ) {
                            textLayout?.let { layout ->
                                // Add marks from references

                                if (references.isEmpty()) return@let
                                val root = references.firstOrNull()?.referencedElement ?: return@let

                                Canvas(Modifier.size(layout.size.toSize().toDpSize())) {
                                    val rootRange = root.range
                                    val rootPath = layout.getPathForRange(rootRange.first, rootRange.last + 1)
                                    drawPath(rootPath, theme.COLOR_SEARCH_RESULT, style = Fill)

                                    references.forEach {
                                        val range = it.element.range
                                        val path = layout.getPathForRange(range.first, range.last + 1)

                                        drawPath(path, theme.COLOR_SEARCH_RESULT, style = Fill)
                                    }
                                }
                            }

                            Box(Modifier
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.type == PointerEventType.Move) {
                                                val position = event.changes.first().position

                                                hoverPosition = null
                                                annotationOverlayJob?.cancel()
                                                annotationOverlayJob = coroutineScope.launch {
                                                    delay(500)
                                                    hoverPosition = position
                                                }
                                            }
                                        }
                                    }
                                }) {
                                textField()
                            }

                            if (isCompletionVisible) {
                                CompletionOverlay(
                                    Modifier
                                        .offset(x = caretOffset.x.toDp(), y = caretOffset.y.toDp()),
                                    codeStyle,
                                    completions,
                                    selectedCompletionIndex
                                )
                            }

                            if (isAnnotationVisible) {
                                hoverPosition?.let {
                                    AnnotationOverlay(
                                        Modifier.offset(it.x.toDp(), it.y.toDp()),
                                        codeStyle,
                                        localAnnotations,
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(Modifier.padding(scale.SIZE_INSET_SMALL), verticalAlignment = Alignment.CenterVertically) {
                        if (analyticsAreUpToDate) {
                            val errors = allAnnotations.count { it.severity == Severity.ERROR }
                            val warnings = allAnnotations.count { it.severity == Severity.WARNING }
                            val infos = allAnnotations.count { it.severity == Severity.INFO }

                            Icon(icon.statusFine, "info", Modifier.height(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_GREEN)
                            Text("$infos", fontFamily = baseSmallStyle.fontFamily, fontSize = baseSmallStyle.fontSize, color = theme.COLOR_FG_0)
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Icon(icon.info, "warnings", Modifier.height(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_YELLOW)
                            Text("$warnings", fontFamily = baseSmallStyle.fontFamily, fontSize = baseSmallStyle.fontSize, color = theme.COLOR_FG_0)
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Icon(icon.statusError, "errors", Modifier.height(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_RED)
                            Text("$errors", fontFamily = baseSmallStyle.fontFamily, fontSize = baseSmallStyle.fontSize, color = theme.COLOR_FG_0)
                        } else {
                            ComposeTools.Rotating { rotation ->
                                Text("CTRL+SHIFT+S to analyze", fontFamily = baseSmallStyle.fontFamily, fontSize = baseSmallStyle.fontSize, color = theme.COLOR_FG_0)
                                Icon(icon.statusLoading, "loading", Modifier.size(scale.SIZE_CONTROL_SMALL).rotate(rotation), tint = theme.COLOR_FG_0)
                                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                                CButton(icon = icon.build, iconType = IconType.SMALL, onClick = {
                                    analyze()
                                })
                            }
                        }
                    }


                    // Draw Vertical ScrollBar

                    val containerHeight = scrollVertical.maxValue.toFloat() + scrollVertical.viewportSize.toFloat()
                    val scrollRatio = if (containerHeight == 0f) 0f else scrollVertical.value.toFloat() / containerHeight

                    // Calculate scrollbar thumb height based on the content height and viewport
                    val thumbHeightRatio = if (containerHeight == 0f) 1f else scrollVertical.viewportSize.toFloat() / containerHeight
                    val thumbHeightPx = thumbHeightRatio * scrollVertical.viewportSize.toFloat()

                    Canvas(Modifier.fillMaxHeight().width(scale.SIZE_CONTROL_MEDIUM)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                // Calculate new scroll position based on drag amount
                                val newScroll = (scrollVertical.value + (dragAmount / thumbHeightRatio)).toInt().coerceIn(0, scrollVertical.maxValue)
                                coroutineScope.launch {
                                    scrollVertical.scrollTo(newScroll)
                                }
                            }
                        }) {
                        // Draw the scrollbar thumb
                        drawRoundRect(
                            color = theme.COLOR_FG_1,
                            style = Fill,
                            topLeft = Offset(x = size.width / 2, y = scrollRatio * size.height),
                            size = size.copy(width = size.width / 2, height = thumbHeightPx),
                            cornerRadius = CornerRadius(scale.SIZE_CORNER_RADIUS.value, scale.SIZE_CORNER_RADIUS.value)
                        )

                        // Draw References
                        textLayout?.let { layout ->
                            val root = references.firstOrNull()?.referencedElement ?: return@let
                            val rootRange = root.range
                            val rootLine = layout.getLineForOffset(rootRange.first)
                            val rootRatioTop = layout.getLineTop(rootLine) / layout.size.height
                            val rootRatioBottom = layout.getLineBottom(rootLine) / layout.size.height
                            val rootRatioHeight = rootRatioBottom - rootRatioTop

                            drawRect(theme.COLOR_SEARCH_RESULT, topLeft = Offset(0f, rootRatioTop * size.height), size = Size(size.width, rootRatioHeight * size.height), style = Fill)

                            references.forEach {
                                val range = it.element.range
                                val line = layout.getLineForOffset(range.first)
                                val ratioTop = layout.getLineTop(line) / layout.size.height
                                val ratioBottom = layout.getLineBottom(line) / layout.size.height
                                val ratioHeight = ratioBottom - ratioTop

                                drawRect(theme.COLOR_SEARCH_RESULT, topLeft = Offset(0f, ratioTop * size.height), size = Size(size.width, ratioHeight * size.height), style = Fill)
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(textFieldValue) {
        coroutineScope.launch {
            val time = measureTime {
                caretOffset = textLayout?.getCursorRect(textFieldValue.selection.start)?.bottomCenter ?: Offset(0f, 0f)
                // Update the current line when the text changes


                file.setAsUTF8String(textFieldValue.text)

                locatePSIElement()
                fetchCompletions()
            }
            if (time.inWholeMilliseconds > 5) nativeLog("LaunchedEffect(textFieldValue) took ${time.inWholeMilliseconds}ms")
        }
    }

    LaunchedEffect(lineCount) {
        val time = measureTime {
            lineNumberLabelingBounds = textMeasurer.measure(lineCount.toString(), codeSmallStyle).size.toSize()
        }
        if (time.inWholeMilliseconds > 5) nativeLog("LaunchedEffect(lineCount) took ${time.inWholeMilliseconds}ms")
    }

    LaunchedEffect(completions) {
        isCompletionVisible = completions.isNotEmpty()
        selectedCompletionIndex = 0
    }

    LaunchedEffect(hoverPosition) {
        val time = measureTime {
            isAnnotationVisible = false
            localAnnotations = emptySet()

            hoverPosition?.let { hoverPosition ->
                val inCodePosition = Offset(hoverPosition.x - rowHeaderWidth, hoverPosition.y)
                val index = textLayout?.getOffsetForPosition(inCodePosition) ?: return@let
                val psiFile = manager?.getPsiFile(file) ?: return@let
                val annotations = service?.collectNotations(psiFile, index) ?: return@let
                localAnnotations = annotations
            }
        }
        if (time.inWholeMilliseconds > 5) nativeLog("LaunchedEffect(hoverPosition) took ${time.inWholeMilliseconds}ms")
    }

    LaunchedEffect(allAnnotations) {
        val time = measureTime {
            val psiFile = manager?.getPsiFile(file) ?: return@LaunchedEffect
            allAnnotations.forEach {
                nativeLog(it.createConsoleMessage(psiFile))
            }
        }
        if (time.inWholeMilliseconds > 5) nativeLog("LaunchedEffect(allAnnotations) took ${time.inWholeMilliseconds}ms")
    }

    LaunchedEffect(localAnnotations) {
        isAnnotationVisible = localAnnotations.isNotEmpty()
    }

    LaunchedEffect(currentElement) {
        // Add marks from references
        currentElement?.let { element ->
            val root = if (element is PsiReference) {
                element.referencedElement
            } else {
                element
            }

            references = if (root == null) {
                emptyList()
            } else {
                service?.findReferences(root) ?: emptyList()
            }
        }
    }
}

fun insertIndentationForSelectedLines(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val indent = "    " // 4 spaces or use "\t" for tab-based indentation
    val startOffset = value.selection.min
    val endOffset = value.selection.max

    val startLine = layoutResult?.getLineForOffset(startOffset) ?: 0
    val endLine = layoutResult?.getLineForOffset(endOffset) ?: 0

    val lines = value.text.lines().toMutableList()

    for (lineIndex in startLine..endLine) {
        val lineStart = layoutResult?.getLineStart(lineIndex) ?: 0
        lines[lineIndex] = indent + lines[lineIndex]
    }

    val newText = lines.joinToString("\n")
    val newSelection = TextRange(
        start = startOffset + indent.length,
        end = endOffset + (endLine - startLine + 1) * indent.length
    )

    return value.copy(text = newText, selection = newSelection)
}

fun removeIndentationForSelectedLines(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val indent = "    " // 4 spaces or "\t"
    val startOffset = value.selection.min
    val endOffset = value.selection.max

    val startLine = layoutResult?.getLineForOffset(startOffset) ?: 0
    val endLine = layoutResult?.getLineForOffset(endOffset) ?: 0

    val lines = value.text.lines().toMutableList()

    for (lineIndex in startLine..endLine) {
        val lineStart = layoutResult?.getLineStart(lineIndex) ?: 0
        if (lines[lineIndex].startsWith(indent)) {
            lines[lineIndex] = lines[lineIndex].removePrefix(indent)
        }
    }

    val newText = lines.joinToString("\n")
    val newSelection = TextRange(
        start = startOffset - indent.length.coerceAtLeast(0),
        end = endOffset - ((endLine - startLine + 1) * indent.length).coerceAtLeast(0)
    )

    return value.copy(text = newText, selection = newSelection)
}

fun insertIndentation(value: TextFieldValue): TextFieldValue {
    // Insert 4 spaces (or tab character, depending on your preference)
    val indent = "    "  // You can replace with "\t" for tabs
    val newText = value.text.substring(0, value.selection.start) +
            indent +
            value.text.substring(value.selection.start)

    // Move the caret after the indent
    val newSelection = TextRange(start = value.selection.start + indent.length, end = value.selection.end + indent.length)

    return value.copy(text = newText, selection = newSelection)
}

fun insertNewlineAndIndent(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    // Find the line the caret is on and replicate its indentation
    val caretPosition = value.selection.start
    val currentLineIndex = layoutResult?.getLineForOffset(caretPosition) ?: 0
    val currentLineStart = layoutResult?.getLineStart(currentLineIndex) ?: 0
    val currentLineText = value.text.substring(currentLineStart, caretPosition)

    // Extract the leading whitespace (indentation) from the current line
    val leadingWhitespace = currentLineText.takeWhile { it.isWhitespace() }

    // Insert newline + leading whitespace
    val newText = value.text.substring(0, caretPosition) +
            "\n" +
            leadingWhitespace +
            value.text.substring(caretPosition)

    // Move the caret after the newline and indentation
    val newCaretPosition = caretPosition + 1 + leadingWhitespace.length

    val newSelection = TextRange(newCaretPosition, newCaretPosition)

    return value.copy(text = newText, selection = newSelection)
}

fun removeIndentation(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val caretPosition = value.selection.start
    val currentLineIndex = layoutResult?.getLineForOffset(caretPosition) ?: 0
    val currentLineStart = layoutResult?.getLineStart(currentLineIndex) ?: 0
    val currentLineText = value.text.substring(currentLineStart, caretPosition)

    // Detect leading whitespace and remove 4 spaces or a tab
    val indent = "    " // or "\t"
    val leadingWhitespace = currentLineText.takeWhile { it.isWhitespace() }

    val newText = if (leadingWhitespace.endsWith(indent)) {
        value.text.substring(0, currentLineStart + leadingWhitespace.length - indent.length) +
                value.text.substring(currentLineStart + leadingWhitespace.length)
    } else {
        value.text
    }

    // Adjust caret position
    val newCaretPosition = caretPosition - indent.length
    val newSelection = TextRange(start = newCaretPosition.coerceAtLeast(currentLineStart), end = newCaretPosition.coerceAtLeast(currentLineStart))

    return value.copy(text = newText, selection = newSelection)
}

