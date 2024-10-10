package ui.uilib.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.toSize
import cengine.editor.annotation.Annotation
import cengine.editor.completion.Completion
import cengine.lang.asm.CodeStyle
import cengine.project.Project
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val codeStyle = FontType.CODE.getStyle()
    val codeSmallStyle = FontType.CODE_SMALL.getStyle()
    val baseStyle = FontType.SMALL.getStyle()
    val textMeasurer = rememberTextMeasurer()

    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }
    var currentLine by remember { mutableStateOf(0) }
    var rowHeaderWidth by remember { mutableStateOf<Float>(0f) }

    var completionJob by remember { mutableStateOf<Job?>(null) }

    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()
    var viewSize by remember { mutableStateOf(Size.Zero) }
    var contentSize by remember { mutableStateOf(Size.Zero) }

    val manager by remember { mutableStateOf(project.getManager(file)) }
    val lang = remember { manager?.lang }
    val service = remember { lang?.psiService }

    val coroutineScope = rememberCoroutineScope()
    var processJob by remember { mutableStateOf<Job?>(null) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(file.getAsUTF8String())) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    var allAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }

    var annotationOverlayJob by remember { mutableStateOf<Job?>(null) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }

    var localAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var isAnnotationVisible by remember { mutableStateOf(false) }

    var completions by remember { mutableStateOf<List<Completion>>(emptyList()) }
    var isCompletionVisible by remember { mutableStateOf(false) }
    var selectedCompletionIndex by remember { mutableStateOf(0) }

    var caretOffset by remember { mutableStateOf<Offset>(Offset(0f, 0f)) }
    var currentElement by remember { mutableStateOf<PsiElement?>(null) }

    suspend fun buildAnnotatedString(code: String, psiFile: PsiFile? = null): AnnotatedString {
        // Fast Lexing Highlighting
        val hls = lang?.highlightProvider?.fastHighlight(code) ?: emptyList()
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

    suspend fun locatePSIElement() {
        val caretPosition = textFieldValue.selection.start
        val psiFile = manager?.getPsiFile(file)
        currentElement = if (psiFile != null) {
            lang?.psiService?.findElementAt(psiFile, caretPosition)
        } else null
    }

    suspend fun updatePSI(code: String) {
        file.setAsUTF8String(code)
        manager?.queueUpdate(file) {
            textFieldValue = textFieldValue.copy(annotatedString = buildAnnotatedString(code, it))
            locatePSIElement()
        }
    }

    fun processTextFieldValue() {
        processJob?.cancel()
        processJob = coroutineScope.launch {
            val newCode = textFieldValue.text
            val annotatedString = buildAnnotatedString(newCode)
            textFieldValue = textFieldValue.copy(annotatedString)
            updatePSI(newCode)
        }
    }

    fun insertCompletion(completion: Completion) {
        val start = textFieldValue.selection.start
        val newText = textFieldValue.text.substring(0, start) + completion.insertion + textFieldValue.text.substring(start)

        textFieldValue = textFieldValue.copy(text = newText, selection = TextRange(start + completion.insertion.length))
    }

    fun fetchCompletions(showIfPrefixIsEmpty: Boolean = false, onlyHide: Boolean = false) {
        completionJob?.cancel()

        if (!onlyHide) {
            completionJob = coroutineScope.launch {
                val layout = textLayout
                if (layout != null) {
                    try {
                        val lineIndex = layout.getLineForOffset(textFieldValue.selection.start)
                        val lineStart = layout.getLineStart(lineIndex)
                        val lineContentBefore = textFieldValue.text.substring(lineStart, textFieldValue.selection.start)

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

    Box(
        modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
            .horizontalScroll(scrollHorizontal)
            .onGloballyPositioned {
                viewSize = it.size.toSize()
            }
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
            }

    ) {

        // Add a light blue background for the current line
        textLayout?.let { layout ->
            val lineTop = layout.getLineTop(currentLine)
            val lineBottom = layout.getLineBottom(currentLine)
            Box(
                modifier = Modifier
                    .offset(y = with(LocalDensity.current) { lineTop.toDp() })
                    .height(with(LocalDensity.current) { (lineBottom - lineTop).toDp() })
                    .width(with(LocalDensity.current) {
                        contentSize.width.toDp()
                    })
                    .background(theme.COLOR_SELECTION.copy(alpha = 0.10f))  // Light blue with 10% opacity
            )
        }

        Row(
            modifier = Modifier
                .sizeIn(minWidth = with(LocalDensity.current) {
                    viewSize.width.toDp()
                }, minHeight = with(LocalDensity.current) {
                    viewSize.height.toDp()
                })
                .onGloballyPositioned {
                    contentSize = it.size.toSize()
                }
        ) {
            val lineNumberLabelingBounds = with(LocalDensity.current) {
                textMeasurer.measure(lineCount.toString(), codeSmallStyle).size.toSize().toDpSize()
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = scale.SIZE_INSET_MEDIUM).onGloballyPositioned {
                        rowHeaderWidth = it.size.toSize().width
                    }.width(lineNumberLabelingBounds.width)
            ) {
                repeat(lineCount) { line ->
                    val thisLineContent = (line + 1).toString()
                    val thisLineTop = textLayout?.multiParagraph?.getLineTop(line) ?: (lineHeight * line)
                    Text(
                        modifier = Modifier
                            .width(lineNumberLabelingBounds.width)
                            .height(
                                with(LocalDensity.current) {
                                    lineHeight.toDp()
                                }
                            ).offset(y = with(LocalDensity.current) {
                                (thisLineTop).toDp() + (lineHeight.toDp() - lineNumberLabelingBounds.height) / 2
                            }),
                        textAlign = TextAlign.Right,
                        text = thisLineContent,
                        color = theme.COLOR_FG_1,
                        style = codeSmallStyle
                    )
                }
            }

            Box(modifier = Modifier
                .height(
                    with(LocalDensity.current) {
                        contentSize.height.toDp()
                    }
                )
                .width(scale.SIZE_BORDER_THICKNESS)
                .background(theme.COLOR_BORDER))

            BasicTextField(
                modifier = Modifier
                    .sizeIn(
                        minWidth = with(LocalDensity.current) {
                            viewSize.width.toDp() - rowHeaderWidth.toDp()
                        },
                        minHeight = with(LocalDensity.current) {
                            viewSize.height.toDp()
                        }
                    )
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.Tab -> {

                                    textFieldValue = if (keyEvent.isShiftPressed) {
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
                                    true
                                }

                                Key.Enter -> {
                                    val completion = completions.getOrNull(selectedCompletionIndex)
                                    if (isCompletionVisible && completion != null) {
                                        insertCompletion(completion)
                                    } else {
                                        textFieldValue = insertNewlineAndIndent(textFieldValue, textLayout)
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
                        } else {
                            false
                        }
                    },
                value = textFieldValue,
                cursorBrush = SolidColor(theme.COLOR_FG_0),
                textStyle = codeStyle.copy(
                    color = theme.COLOR_FG_0
                ),
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    // Update the current line when the text changes
                    textLayout?.let { layout ->
                        currentLine = layout.getLineForOffset(newValue.selection.start)
                    }
                },
                onTextLayout = { result ->
                    textLayout = result
                    setLineCount(result.lineCount)
                    setLineHeight(result.multiParagraph.height / result.lineCount)
                    // Update the current line when the layout changes
                    currentLine = result.getLineForOffset(textFieldValue.selection.start)
                }
            )
        }

        if (isCompletionVisible) {
            CompletionOverlay(
                Modifier
                    .offset(
                        x = with(LocalDensity.current) {
                            caretOffset.x.toDp() + rowHeaderWidth.toDp() + scale.SIZE_BORDER_THICKNESS
                        },
                        y = with(LocalDensity.current) {
                            caretOffset.y.toDp()
                        }
                    ),
                completions,
                selectedCompletionIndex
            )
        }

        if (isAnnotationVisible) {
            hoverPosition?.let {
                AnnotationOverlay(
                    Modifier.offset(
                        with(LocalDensity.current) {
                            it.x.toDp() + rowHeaderWidth.toDp()
                        },
                        with(LocalDensity.current) {
                            it.y.toDp()
                        }
                    ),
                    localAnnotations,
                )
            }
        }
    }

    ComposeTools.TrackStateChanges(textFieldValue) { oldValue, newValue ->
        if (newValue.text != oldValue.text) {
            // Text was changed
            processTextFieldValue()
        }

        coroutineScope.launch {
            caretOffset = textLayout?.getCursorRect(newValue.selection.start)?.bottomCenter ?: Offset(0f, 0f)
            locatePSIElement()
            fetchCompletions()
        }
    }

    LaunchedEffect(completions) {
        isCompletionVisible = completions.isNotEmpty()
        selectedCompletionIndex = 0
    }

    LaunchedEffect(hoverPosition) {
        isAnnotationVisible = false

        hoverPosition?.let { hoverPosition ->
            val inCodePosition = Offset(hoverPosition.x - rowHeaderWidth, hoverPosition.y)
            val index = textLayout?.getOffsetForPosition(inCodePosition) ?: return@let
            val psiFile = manager?.getPsiFile(file) ?: return@let
            localAnnotations = service?.collectNotations(psiFile, index) ?: return@let
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

