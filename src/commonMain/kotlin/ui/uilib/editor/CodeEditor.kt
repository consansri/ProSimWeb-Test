package ui.uilib.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.*
import ui.uilib.ComposeTools
import ui.uilib.UIState
import ui.uilib.params.FontType

@Composable
fun CodeEditor(
    file: VirtualFile,
    project: Project,
    modifier: Modifier = Modifier
) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icon = UIState.Icon.value

    val codeStyle = FontType.CODE.getStyle()
    val codeSmallStyle = FontType.CODE_SMALL.getStyle()
    val baseStyle = FontType.SMALL.getStyle()

    val manager = remember { project.getManager(file) }
    val lang = remember { manager?.lang }
    val service = remember { lang?.psiService }

    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(file.getAsUTF8String())) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    var lineNumberLabelingBounds by remember { mutableStateOf<Size>(Size.Zero) }
    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var rowHeaderWidth by remember { mutableStateOf<Float>(0f) }

    var currentLine by remember { mutableStateOf(0) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    var caretOffset by remember { mutableStateOf<Offset>(Offset(0f, 0f)) }
    var currentElement by remember { mutableStateOf<PsiElement?>(null) }

    var analyticsAreUpToDate by remember { mutableStateOf(false) }
    var annotationOverlayJob by remember { mutableStateOf<Job?>(null) }
    var allAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var localAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var isAnnotationVisible by remember { mutableStateOf(false) }

    var completionOverlayJob by remember { mutableStateOf<Job?>(null) }
    var completions by remember { mutableStateOf<List<Completion>>(emptyList()) }
    var isCompletionVisible by remember { mutableStateOf(false) }
    var selectedCompletionIndex by remember { mutableStateOf(0) }

    fun buildAnnotatedString(code: String, psiFile: PsiFile? = null): AnnotatedString {
        // Fast Lexing Highlighting
        val hls = emptyList<HLInfo>()// lang?.highlightProvider?.fastHighlight(code) ?: emptyList()
        val styles = hls.mapNotNull {
            if (!it.range.isEmpty()) {
                AnnotatedString.Range<SpanStyle>(SpanStyle(color = Color(it.color or 0xFF000000.toInt())), it.range.first, it.range.last + 1)
            } else null
        }

        if (service != null && psiFile != null) {
            // Complete Highlighting
            allAnnotations = service.collectNotations(psiFile)
            val annotationStyles = allAnnotations.mapNotNull {
                val annoCodeStyle = it.severity.color ?: CodeStyle.BASE0
                val style = SpanStyle(textDecoration = TextDecoration.Underline, color = theme.getColor(annoCodeStyle))
                if (!it.range.isEmpty()) {
                    AnnotatedString.Range<SpanStyle>(style, it.range.first, it.range.last + 1)
                } else null
            }

            val highlightStyles = service.collectHighlights(psiFile).mapNotNull { (range, style) ->
                if (!range.isEmpty()) {
                    AnnotatedString.Range<SpanStyle>(SpanStyle(color = theme.getColor(style)), range.first, range.last + 1)
                } else null
            }

            return AnnotatedString(code, annotationStyles + highlightStyles + styles)
        } else {
            return AnnotatedString(code, spanStyles = styles)
        }
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
            }

            newText.length < oldText.length -> {
                // Delete
                analyticsAreUpToDate = false
                val startIndex = newText.commonPrefixWith(oldText).length
                val length = oldText.length - newText.length
                manager?.deleted(file, startIndex, startIndex + length)
            }

            newText != oldText -> {
                analyticsAreUpToDate = false
            }
        }

        textFieldValue = new
    }

    suspend fun locatePSIElement() {
        val caretPosition = textFieldValue.selection.start
        val psiFile = manager?.getPsiFile(file)
        currentElement = if (psiFile != null) {
            lang?.psiService?.findElementAt(psiFile, caretPosition)
        } else null
    }

    fun analyze() {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                manager?.updatePsi(file) {
                    textFieldValue = textFieldValue.copy(buildAnnotatedString(it.content, it))
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
                TransformedText(buildAnnotatedString(str.text, manager?.getPsiFile(file)), OffsetMapping.Identity)
            },
            onValueChange = { newValue ->
                // TODO  Performantly get inserted or deleted event with index of insertion (and value) or deletion range
                onTextChange(newValue)
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
                    contentAlignment = Alignment.TopEnd
                ) {
                    Row(Modifier.padding(scale.SIZE_INSET_SMALL), verticalAlignment = Alignment.CenterVertically) {

                        if (analyticsAreUpToDate) {
                            val errors = allAnnotations.count { it.severity == Severity.ERROR }
                            val warnings = allAnnotations.count { it.severity == Severity.WARNING }
                            val infos = allAnnotations.count { it.severity == Severity.INFO }

                            Icon(icon.statusFine, "info", Modifier.size(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_GREEN)
                            Text("$infos", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                            Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                            Icon(icon.info, "warnings", Modifier.size(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_YELLOW)
                            Text("$warnings", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                            Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                            Icon(icon.statusError, "errors", Modifier.size(scale.SIZE_CONTROL_SMALL), tint = theme.COLOR_RED)
                            Text("$errors", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                        } else {
                            ComposeTools.Rotating { rotation ->
                                Icon(icon.statusLoading, "loading", Modifier.size(scale.SIZE_CONTROL_SMALL).rotate(rotation), tint = theme.COLOR_FG_1)
                            }
                        }
                    }
                }

                Box(
                    Modifier.matchParentSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val selection = textFieldValue.selection
                        val line = textLayout?.getLineForOffset(selection.start) ?: 0
                        val column = selection.start - (textLayout?.getLineStart(line) ?: 0)

                        Text("${line + 1}:${column + 1}", modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM), fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                }

                Box(
                    Modifier.matchParentSize()
                        .verticalScroll(scrollVertical)
                ) {

                    // Add a light blue background for the current line
                    textLayout?.let { layout ->
                        val lineTop = layout.getLineTop(currentLine)
                        val lineBottom = layout.getLineBottom(currentLine)
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


            }

        }
    }

    LaunchedEffect(textFieldValue) {
        coroutineScope.launch {
            caretOffset = textLayout?.getCursorRect(textFieldValue.selection.start)?.bottomCenter ?: Offset(0f, 0f)
            // Update the current line when the text changes
            currentLine = textLayout?.getLineForOffset(textFieldValue.selection.start) ?: -1
            locatePSIElement()
            fetchCompletions()
        }
    }

    LaunchedEffect(lineCount) {
        lineNumberLabelingBounds = textMeasurer.measure(lineCount.toString(), codeSmallStyle).size.toSize()
    }

    LaunchedEffect(completions) {
        isCompletionVisible = completions.isNotEmpty()
        selectedCompletionIndex = 0
    }

    LaunchedEffect(hoverPosition) {
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

    LaunchedEffect(allAnnotations) {
        val psiFile = manager?.getPsiFile(file) ?: return@LaunchedEffect
        allAnnotations.forEach {
            nativeLog(it.createConsoleMessage(psiFile))
        }
    }

    LaunchedEffect(localAnnotations) {
        isAnnotationVisible = localAnnotations.isNotEmpty()
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

