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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cengine.editor.completion.Completion
import cengine.editor.selection.Selector
import cengine.lang.asm.CodeStyle
import cengine.project.Project
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile
import emulator.kit.nativeWarn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ui.uilib.UIState
import ui.uilib.params.FontType

@Composable
fun Editor(
    modifier: Modifier,
    file: VirtualFile,
    project: Project
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val codeStyle = FontType.CODE.getStyle()
    val codeSmallStyle = FontType.CODE_SMALL.getStyle()
    val baseStyle = FontType.SMALL.getStyle()
    val textMeasurer = rememberTextMeasurer()

    val manager = project.getManager(file)
    val lang = manager?.lang
    val service = lang?.psiService

    val coroutineScope = rememberCoroutineScope()

    fun buildAnnotatedString(code: String, psiFile: PsiFile? = null): AnnotatedString {
        // Fast Lexing Highlighting
        val hls = lang?.highlightProvider?.fastHighlight(code) ?: emptyList()
        val styles = hls.mapNotNull {
            if (!it.range.isEmpty()) {
                AnnotatedString.Range<SpanStyle>(SpanStyle(color = Color(it.color or 0xFF000000.toInt())), it.range.first, it.range.last + 1)
            } else null
        }

        if (service != null && psiFile != null) {
            // Complete Highlighting
            val annotationStyles = service.collectNotations(psiFile).mapNotNull {
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

            return AnnotatedString(code, styles + highlightStyles + annotationStyles)
        } else {
            return AnnotatedString(code, spanStyles = styles)
        }
    }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(buildAnnotatedString(file.getAsUTF8String()))) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    fun updatePSI(code: String) {
        file.setAsUTF8String(code)
        manager?.queueUpdate(file) {
            textFieldValue = textFieldValue.copy(annotatedString = buildAnnotatedString(code, it))
        }
    }

    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }
    var rowHeaderWidth by remember { mutableStateOf<Float>(0f) }

    var completionJob by remember { mutableStateOf<Job?>(null) }
    var completions by remember { mutableStateOf<List<Completion>>(emptyList()) }

    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()
    var viewSize by remember { mutableStateOf(Size.Zero) }
    var imperativeRepaintState by remember { mutableStateOf(false) }

    val state = remember {
        EditorState(
            file,
            project.getManager(file),
        ) {
            imperativeRepaintState = !imperativeRepaintState
        }
    }

    fun fetchCompletions(showIfPrefixIsEmpty: Boolean = false, onlyHide: Boolean = false) {
        completionJob?.cancel()

        completions = emptyList()

        if (!onlyHide) {
            completionJob = coroutineScope.launch {
                try {
                    val prefixIndex = state.selector.indexOfWordStart(state.selector.caret.index, Selector.DEFAULT_SPACING_SET, false)
                    val prefix = state.textModel.substring(prefixIndex, state.selector.caret.index)
                    if (showIfPrefixIsEmpty || prefix.isNotEmpty()) {
                        completions = state.lang?.completionProvider?.fetchCompletions(prefix, state.currentElement, state.psiManager?.getPsiFile(file)) ?: emptyList()
                    }
                } catch (e: Exception) {
                    nativeWarn("Completion canceled by edit.")
                }
            }
        }
    }

    Box(
        modifier
            .verticalScroll(scrollVertical)
            .horizontalScroll(scrollHorizontal)
            .onGloballyPositioned {
                viewSize = it.size.toSize()
            }

    ) {
        Row {
            Box(modifier = Modifier.padding(horizontal = scale.SIZE_INSET_MEDIUM).onGloballyPositioned {
                rowHeaderWidth = it.size.toSize().width
            }) {
                repeat(lineCount) { line ->
                    val thisLineContent = (line + 1).toString()
                    val thisLineTop = textLayout?.multiParagraph?.getLineTop(line) ?: (lineHeight * line)
                    val thisLineHeight = textLayout?.multiParagraph?.getLineHeight(line) ?: lineHeight
                    val thisLineContentLayout = textMeasurer.measure(thisLineContent, codeSmallStyle)
                    val thisTopOffset = (thisLineHeight - thisLineContentLayout.size.height) / 2

                    Text(
                        modifier = Modifier.offset(y = with(LocalDensity.current) {
                            (thisLineTop + thisTopOffset).toDp()
                        }, x = with(LocalDensity.current) {
                            (rowHeaderWidth - thisLineContentLayout.size.width).toDp()
                        }),
                        text = thisLineContent,
                        color = theme.COLOR_FG_1,
                        style = codeSmallStyle
                    )
                }
            }
            Box(modifier = Modifier.width(10.dp).fillMaxHeight().background(theme.COLOR_FG_1))
            BasicTextField(
                modifier = Modifier.fillMaxSize(),
                value = textFieldValue,
                cursorBrush = SolidColor(theme.COLOR_FG_0),
                textStyle = codeStyle.copy(
                    color = theme.COLOR_FG_0
                ),
                onValueChange = {
                    val text = it.text
                    textFieldValue = it.copy(annotatedString = buildAnnotatedString(text))
                    updatePSI(text)
                },
                onTextLayout = { result ->
                    textLayout = result
                    setLineCount(result.lineCount)
                    setLineHeight(result.multiParagraph.height / result.lineCount)
                }
            )
        }
    }
}


