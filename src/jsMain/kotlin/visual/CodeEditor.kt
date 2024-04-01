package visual

import Constants
import StyleAttr
import emotion.react.css
import emulator.kit.common.FileHandler
import kotlinx.browser.window
import web.html.*
import react.*
import react.dom.aria.ariaHidden
import react.dom.html.AutoComplete
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import debug.DebugTools
import emulator.kit.assembly.Compiler
import emulator.kit.common.ArchState
import visual.StyleExt.get
import visual.StyleExt.getVCRows
import web.cssom.ClassName
import web.timers.*
import web.cssom.*
import kotlin.time.measureTime

external interface CodeEditorProps : Props {
    var archState: StateInstance<emulator.kit.Architecture>
    var fileState: StateInstance<FileHandler>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var fileChangeEvent: StateInstance<Boolean>
}

val CodeEditor = FC<CodeEditorProps> { props ->
    /* ----------------- REACT REFERENCES ----------------- */

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnSwitchRef = useRef<HTMLAnchorElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val btnRedoRef = useRef<HTMLAnchorElement>(null)
    val addtabinput = useRef<HTMLInputElement>(null)
    val renameinput = useRef<HTMLInputElement>(null)

    val infoPanelRef = useRef<HTMLAnchorElement>(null)
    val editorContainerRef = useRef<HTMLDivElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)
    val preHLTimeoutRef = useRef<Timeout>(null)
    val checkTimeOutRef = useRef<Timeout>(null)

    /* ----------------- REACT STATES ----------------- */

    val (state, setState) = useState(props.archState.component1().getState().currentState)

    val (currExeLine, setCurrExeLine) = useState(-1)
    val (exeFile, setExeFile) = useState<FileHandler.File>()
    val (taValueUpdate, setTaValueUpdate) = useState(false)

    val (showRenameTab, setShowRenameTab) = useState(false)
    val (showAddTab, setShowAddTab) = useState(false)
    val (showInfoPanel, setShowInfoPanel) = useState(false)
    val (lineNumbers, setLineNumbers) = useState(1)
    val (infoPanelText, setInfoPanelText) = useState("")

    val (tsActive, setTSActive) = useState(false)
    val (undoActive, setUndoActive) = useState(false)
    val (redoActive, setRedoActive) = useState(false)

    val (lowPerformanceMode, setLowPerformanceMode) = useState(false)
    /* ----------------- localStorage Sync Objects ----------------- */

    val (vcRows, setvcRows) = useState<List<String>>(emptyList())
    val (files, setFiles) = useState<List<FileHandler.File>>(emptyList())
    val (transcriptView, setTranscriptView) = useState(false)


    /* ----------------- SUGAR ----------------- */


    /* ----------------- UPDATE VISUAL COMPONENTS ----------------- */

    fun updateLineNumbers() {
        val textarea = textareaRef.current ?: return
        val numberOfLines = textarea.value.split("\n").size
        setLineNumbers(numberOfLines)
    }

    fun updateTAResize() {
        var height = 0
        var width: Int

        textareaRef.current?.let { element ->
            val lineCount = element.value.split("\n").size + 1
            width = element.value.split("\n").maxOfOrNull { it.length } ?: 0
            height = lineCount * StyleAttr.Main.Editor.TextField.lineHeight
            element.style.height = "auto"
            element.style.height = "${height}px"
            element.style.width = "auto"
            element.style.width = "${width + 1}ch"
        }
        inputDivRef.current?.let {
            it.style.height = "auto"
            if (height != 0) {
                it.style.height = "${height}px"
            }
        }
    }

    fun updateTsButton() {
        if (props.archState.component1().getState().currentState == ArchState.State.EXECUTABLE || props.archState.component1().getState().currentState == ArchState.State.EXECUTION) {
            btnSwitchRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            setTSActive(true)
        } else {
            btnSwitchRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            setTSActive(false)
        }
    }

    fun updateClearButton() {
        textareaRef.current?.let {
            if (it.value != "") {
                btnClearRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            } else {
                btnClearRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    fun updateUndoRedoButton() {
        if (props.fileState.component1().getCurrUndoLength() > 1) {
            btnUndoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            setUndoActive(true)
        } else {
            setUndoActive(false)
            btnUndoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
        if (props.fileState.component1().getCurrRedoLength() > 0) {
            setRedoActive(true)
            btnRedoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        } else {
            setRedoActive(false)
            btnRedoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun build() {
        val valueToCheck = props.fileState.component1().getCurrContent()

        /* ----------------- COROUTINES ----------------- */
        checkTimeOutRef.current?.let {
            clearTimeout(it)
        }
        checkTimeOutRef.current = setTimeout({
            val analysisTime = measureTime {
                props.archState.component1().getState().edit()
                val compilationResult = props.archState.component1().compile(valueToCheck, props.fileState.component1().getCurrent().getName(), props.fileState.component1().getOthers(), build = true)
                props.fileState.component1().getCurrent().linkGrammarTree(compilationResult.tree)
                val hlTaList = compilationResult.tokens.getVCRows()
                setvcRows(hlTaList)
                props.compileEventState.component2().invoke(!props.compileEventState.component1())
                setState(props.archState.component1().getState().currentState)
            }
            if (analysisTime.inWholeMilliseconds <= Constants.EDITOR_MAX_ANALYSIS_MILLIS) {
                setLowPerformanceMode(false)
            }
        }, 0)
    }

    fun preHighlight() {
        val value = props.fileState.component1().getCurrContent()
        val rows = value.split("\n")
        setvcRows(rows)
        val delay = 1500
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        if (!lowPerformanceMode) {
            preHLTimeoutRef.current = setTimeout({
                val analysisTime = measureTime {
                    props.archState.component1().getState().edit()
                    val compilationResult = props.archState.component1().compile(value, props.fileState.component1().getCurrent().getName(), props.fileState.component1().getOthers(), build = false)
                    props.fileState.component1().getCurrent().linkGrammarTree(compilationResult.tree)
                    val hlTaList = compilationResult.tokens.getVCRows()
                    setvcRows(hlTaList)
                    props.compileEventState.component2().invoke(!props.compileEventState.component1())
                    setState(props.archState.component1().getState().currentState)
                }
                if (analysisTime.inWholeMilliseconds > Constants.EDITOR_MAX_ANALYSIS_MILLIS) {
                    setLowPerformanceMode(true)
                    props.archState.component1().getConsole()
                        .compilerInfo("Automatic syntax analysis disabled cause last analysis took more than ${Constants.EDITOR_MAX_ANALYSIS_MILLIS}ms!\nBuild the project to recheck performance. If analysis time improved automatic syntax analysis will be reactivated.")
                }
            }, delay)
        }
    }

    /* ----------------- CHANGE EVENTS ----------------- */
    fun edit(content: String) {
        props.archState.component1().getState().edit()

        props.fileState.component1().editCurr(content)
        textareaRef.current?.let {
            it.value = props.fileState.component1().getCurrContent()
        }

        preHighlight()
        setTaValueUpdate(!taValueUpdate)
    }

    fun undo() {
        props.archState.component1().getState().edit()

        props.fileState.component1().undoCurr()
        textareaRef.current?.let {
            it.value = props.fileState.component1().getCurrContent()
        }

        preHighlight()
        setTaValueUpdate(!taValueUpdate)
    }

    fun redo() {
        props.archState.component1().getState().edit()

        props.fileState.component1().redoCurr()
        textareaRef.current?.let {
            it.value = props.fileState.component1().getCurrContent()
        }

        preHighlight()
        setTaValueUpdate(!taValueUpdate)
    }

    /* ----------------- DOM ----------------- */

    div {
        className = ClassName(StyleAttr.Main.Editor.CLASS)

        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.start
                alignItems = AlignItems.start
                //padding = StyleAttr.paddingSize
                minWidth = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                a {
                    width = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                    //boxShadow = BoxShadow(0.px, 3.px, 8.px, rgb(0, 0, 0, 0.24))
                    padding = StyleAttr.Main.Editor.Controls.iconPadding
                    //borderRadius = StyleAttr.Main.Editor.Controls.borderRadius
                    borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                    background = StyleAttr.Main.Editor.Controls.BgColor.get()
                    color = StyleAttr.Main.Editor.Controls.FgColor.get()
                    transition = Transition(TransitionProperty.all, 0.1.s, TransitionTimingFunction.ease)
                }
                img {
                    width = StyleAttr.Main.Editor.Controls.iconSize
                    //height = StyleAttr.Main.Editor.Controls.iconSize
                    filter = StyleAttr.Main.Editor.Controls.iconFilter.get()
                }
            }

            a {
                css {
                    height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                    cursor = Cursor.pointer
                    if (transcriptView) {
                        filter = important(StyleAttr.iconActiveFilter)
                    }
                }

                id = "switch"
                ref = btnSwitchRef
                title = "Transcript Switch"
                ReactHTML.img {
                    src = StyleAttr.Icons.disassembler
                }

                if (tsActive) {
                    onClick = {
                        setTranscriptView(!transcriptView)
                    }
                }
            }

            a {
                css {
                    height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                    cursor = Cursor.pointer
                }

                when (state) {
                    ArchState.State.UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleAttr.Main.CLASS_ANIM_ROTATION)
                            src = StyleAttr.Icons.status_loading
                        }
                    }

                    ArchState.State.EXECUTABLE -> {
                        title = "Status: ready to build"
                        img {
                            src = StyleAttr.Icons.status_fine
                        }
                    }

                    ArchState.State.HASERRORS -> {
                        title = "Status: fix errors!"
                        img {
                            src = StyleAttr.Icons.status_error
                        }
                    }

                    ArchState.State.EXECUTION -> {
                        title = "Status: executing..."
                        img {
                            src = StyleAttr.Icons.status_fine
                        }
                    }

                }
            }

            if (!transcriptView) {
                a {
                    css {
                        height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                        cursor = Cursor.pointer
                    }
                    id = "undo"
                    ref = btnUndoRef
                    title = "Undo"

                    img {
                        src = StyleAttr.Icons.backwards
                    }
                    if (undoActive) {
                        onClick = {
                            undo()
                        }
                    }
                }

                a {
                    css {
                        height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                        cursor = Cursor.pointer
                    }
                    id = "redo"
                    ref = btnRedoRef
                    title = "Redo"

                    img {
                        src = StyleAttr.Icons.forwards
                    }
                    if (redoActive) {
                        onClick = {
                            redo()
                        }
                    }
                }

                a {
                    css {
                        height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                        cursor = Cursor.pointer
                    }
                    id = "build"
                    title = "build"

                    img {
                        src = StyleAttr.Icons.build
                    }
                    onClick = {
                        build()
                    }
                }
            }


            a {
                css {
                    height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                    cursor = Cursor.pointer
                }
                id = "info"

                title = """
                    Code Editor Info
                 
                        Shortcuts
                        - CTRL + S  (Build)
                        - CTRL + Z  (Undo)
                        - CTRL + C  (Copy)
                        - CTRL + V  (Insert)
                        - CTRL + A  (Select All)
                        - TAB       (Insert Tab or Indent Selection)                        
                        - SHIFT + TAB       (Trim Indent of Selection)
                        - CTRL + ALT + L    (Reformat)
                        
                        Features
                        - Tab Handling
                        - Undo History
                        - Clear Button
                        - Code Highlighting
                        - Execute until (click on linenumber or address)
                        
                        - Transcript (switch type by click on the left vertical title)
                            - Compiled (generated from code compilation)
                            - Disassembled (generated from memory)                        
                    
                """.trimIndent()

                img {
                    src = StyleAttr.Icons.info
                }
            }

            if (!transcriptView) {
                a {
                    css {
                        height = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                        background = important(StyleAttr.Main.DeleteColor.get())
                        cursor = Cursor.pointer
                    }
                    ref = btnClearRef
                    title = "Clear"

                    img {
                        css {
                            filter = important(invert(100.pct))
                        }
                        src = StyleAttr.Icons.delete_black
                    }

                    onClick = {
                        edit("")
                    }
                }

                a {
                    ref = infoPanelRef
                    css {
                        cursor = Cursor.pointer
                        position = Position.absolute
                        bottom = 0.px
                        display = Display.block
                        writingMode = WritingMode.verticalRl
                        borderTop = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                        borderBottom = important(Border(0.px, LineStyle.solid))
                    }

                    title = "{$infoPanelText}"

                    if (showInfoPanel) {
                        +infoPanelText
                    }

                    img {
                        src = StyleAttr.Icons.tag
                    }

                    onClick = {
                        setShowInfoPanel(!showInfoPanel)
                    }
                }
            }
        }

        div {
            css(ClassName(StyleAttr.Main.Editor.TextField.CLASS)) {
                overflow = Overflow.hidden
                display = Display.flex
                flexDirection = FlexDirection.column
                maxHeight = 100.pct
                width = 100.pct
                this.lineHeight = StyleAttr.Main.Editor.TextField.lineHeight.px
                fontFamily = FontFamily.monospace
                backgroundColor = StyleAttr.Main.Editor.BgColor.get()
                color = StyleAttr.Main.Editor.FgColor.get()
                caretColor = important(StyleAttr.Main.Editor.FgColor.get())
                borderLeft = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                borderRight = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                //borderRadius = StyleAttr.borderRadius
                padding = StyleAttr.paddingSize
                //boxShadow = StyleAttr.Main.elementShadow
            }

            ref = editorContainerRef

            if (transcriptView) {
                TranscriptView {
                    this.taVal = taVal
                    this.arch = props.archState
                    this.exeEventState = props.exeEventState
                    this.compileEventState = props.compileEventState
                }
            } else {

                div {

                    className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TABS)

                    for (fileID in files.indices) {
                        val file = props.fileState.component1().getAllFiles()[fileID]
                        div {

                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TAB + if (file == props.fileState.component1().getCurrent()) " ${StyleAttr.Main.Editor.TextField.CLASS_TAB_ACTIVE}" else "")

                            img {
                                src = if (file.getLinkedTree() != null) {
                                    StyleAttr.Icons.file_compiled
                                } else {
                                    StyleAttr.Icons.file_not_compiled
                                }
                            }

                            if (file == props.fileState.component1().getCurrent()) {
                                if (!showRenameTab) {
                                    a {
                                        css {
                                            if (file == exeFile) {
                                                color = important(StyleAttr.Main.Editor.HL.greenPCMark.color.get())
                                            }
                                        }
                                        +file.getName()

                                        onClick = {
                                            setShowRenameTab(true)
                                        }

                                    }
                                } else {
                                    input {
                                        ref = renameinput
                                        type = InputType.text
                                        placeholder = "name"
                                        defaultValue = file.getName()

                                        onBlur = {
                                            setShowRenameTab(false)
                                        }

                                        onKeyDown = { event ->
                                            if (event.key == "Enter") {
                                                event.currentTarget.blur()
                                            }
                                        }

                                        onChange = {
                                            val success = props.fileState.component1().renameCurrent(it.currentTarget.value)
                                            renameinput.current?.let { input ->
                                                if (success) {
                                                    input.classList.add(StyleAttr.ANIM_BLINKGREEN)
                                                    setTimeout({
                                                        input.classList.remove(StyleAttr.ANIM_BLINKGREEN)
                                                    }, 500)
                                                } else {
                                                    input.classList.add(StyleAttr.ANIM_SHAKERED)
                                                    setTimeout({
                                                        input.classList.remove(StyleAttr.ANIM_SHAKERED)
                                                    }, 500)
                                                }
                                            }

                                        }

                                    }
                                }

                                img {
                                    css {
                                        filter = StyleAttr.Main.DeleteFilter
                                    }

                                    src = StyleAttr.Icons.delete_black

                                    onClick = {
                                        val response = window.confirm("Do you really want to delete the file '${file.getName()}'?\nThis can't be undone!")
                                        if (response) {
                                            props.fileState.component1().remove(file)
                                            edit(props.fileState.component1().getCurrContent())
                                            setShowAddTab(false)
                                            setTaValueUpdate(!taValueUpdate)
                                        }
                                    }
                                }
                            } else {
                                a {
                                    css {
                                        if (file == exeFile) {
                                            color = important(StyleAttr.Main.Editor.HL.greenPCMark.color.get())
                                        }
                                    }
                                    +file.getName()

                                    onClick = {
                                        props.fileState.component1().setCurrent(fileID)
                                        edit(props.fileState.component1().getCurrContent())
                                        setShowAddTab(false)
                                        setTaValueUpdate(!taValueUpdate)
                                    }
                                }
                            }
                        }
                    }

                    if (showAddTab) {
                        div {
                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TAB)

                            input {
                                ref = addtabinput
                                type = InputType.text
                                placeholder = "name"

                                onBlur = {
                                    setShowAddTab(false)
                                }

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        val input = addtabinput.current
                                        input?.let {
                                            val success = props.fileState.component1().import(FileHandler.File(it.value, ""))
                                            if (success) {
                                                edit(props.fileState.component1().getCurrContent())
                                                setTaValueUpdate(!taValueUpdate)
                                                setShowAddTab(false)
                                            } else {
                                                input.classList.add(StyleAttr.ANIM_SHAKERED)
                                                setTimeout({
                                                    input.classList.remove(StyleAttr.ANIM_SHAKERED)
                                                }, 300)
                                            }
                                        }
                                    }
                                }
                            }

                            a {
                                +"+"

                                onClick = {
                                    val input = addtabinput.current
                                    input?.let {
                                        val success = props.fileState.component1().import(FileHandler.File(it.value, ""))
                                        if (success) {
                                            edit(props.fileState.component1().getCurrContent())
                                            setTaValueUpdate(!taValueUpdate)
                                            setShowAddTab(false)
                                        } else {
                                            input.classList.add(StyleAttr.ANIM_SHAKERED)
                                            setTimeout({
                                                input.classList.remove(StyleAttr.ANIM_SHAKERED)
                                            }, 300)
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        a {
                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TAB)

                            +"+"

                            onClick = { _ ->
                                setShowAddTab(true)
                            }
                        }
                    }
                }

                div {
                    className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_SCROLL_CONTAINER)

                    div {
                        css(ClassName(StyleAttr.Main.Editor.TextField.CLASS_LINE_NUMBERS)) {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            alignItems = AlignItems.flexEnd
                            minWidth = StyleAttr.Main.Editor.TextField.minLineNumWidth
                            textAlign = TextAlign.right

                            span {
                                cursor = Cursor.pointer
                                paddingRight = StyleAttr.paddingSize
                                fontFamily = FontFamily.monospace
                                borderRight = Border(1.px, LineStyle.solid, StyleAttr.Main.Editor.TextField.LineNumbersBorderColor.get())
                                color = StyleAttr.Main.Editor.TextField.LineNumbersColor.get()

                                ".${StyleAttr.Main.Editor.TextField.CLASS_LINE_ACTIVE}" {
                                    color = important(StyleAttr.Main.Editor.TextField.LineActiveColor.get())
                                }
                            }
                        }
                        ref = lineNumbersRef

                        for (lineNumber in 1..lineNumbers) {
                            span {
                                onClick = { _ ->
                                    props.archState.component1().exeUntilLine(lineNumber - 1, props.fileState.component1().getCurrent().getName())
                                    props.exeEventState.component2().invoke(!props.exeEventState.component1())
                                }
                                css {
                                    if (currExeLine == lineNumber) {
                                        color = important(StyleAttr.Main.Editor.HL.greenPCMark.color.get())
                                    }
                                }

                                if (lineNumber == currExeLine) {
                                    +"► $lineNumber"
                                } else {
                                    +"$lineNumber"
                                }
                            }
                        }
                    }

                    div {
                        className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_INPUT_DIV)
                        ref = inputDivRef

                        textarea {
                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_AREA)
                            ref = textareaRef
                            autoComplete = AutoComplete.off
                            autoCorrect = "off"
                            cols = 50
                            autoCapitalize = "off"
                            spellCheck = false
                            placeholder = "Enter ${props.archState.component1().getDescription().name} Assembly ..."

                            onSelect = { event ->
                                val cursorPosition = event.currentTarget.selectionStart
                                cursorPosition.let { cursorPos ->

                                    val lines = event.currentTarget.value.substring(0, cursorPos).split("\n")
                                    val lineID = lines.size - 1
                                    val startIndex = lines[lineID].length

                                    val grammarTree = props.archState.component1().getCompiler().getGrammarTree()
                                    grammarTree?.rootNode?.let { rootNode ->
                                        var path = ""
                                        rootNode.containers.forEach {
                                            it.getAllTokens().forEach { token ->
                                                if (token.lineLoc.lineID == lineID && startIndex in token.lineLoc.startIndex..token.lineLoc.endIndex) {
                                                    val result = grammarTree.contains(token)
                                                    if (result != null) {
                                                        path = result.path
                                                    }
                                                }
                                            }
                                        }
                                        setInfoPanelText(path)
                                    }
                                }
                            }

                            onChange = { event ->
                                edit(event.currentTarget.value)
                            }

                            onKeyDown = { event ->
                                if (event.key == "Tab") {
                                    if (event.shiftKey) {
                                        textareaRef.current?.let {
                                            val start =
                                                it.selectionStart
                                            val end =
                                                it.selectionEnd

                                            // multi line remove indent
                                            val lastLineBreakBeforeSelStart = it.value.lastIndexOf('\n', start - 1)
                                            val indentStart = if (lastLineBreakBeforeSelStart != -1) lastLineBreakBeforeSelStart else 0
                                            val content = it.value.substring(indentStart, end)
                                            val before = it.value.substring(0, indentStart)
                                            val after = it.value.substring(end)

                                            val indentContent = if (indentStart == 0) {
                                                content.removePrefix("\t").replace("\n\t", "\n")
                                            } else {
                                                content.replace("\n\t", "\n")
                                            }
                                            it.value = before + indentContent + after
                                            it.selectionStart = start
                                            it.selectionEnd = end + indentContent.length - content.length

                                            event.preventDefault()
                                            edit(event.currentTarget.value)
                                        }
                                    } else {
                                        textareaRef.current?.let {
                                            val start =
                                                it.selectionStart
                                            val end =
                                                it.selectionEnd

                                            if (start == end) {
                                                // insert tab
                                                it.value = it.value.substring(0, start) + '\t' + it.value.substring(end)
                                                it.selectionEnd = end + 1
                                            } else {
                                                // multi line prepend indent
                                                val lastLineBreakBeforeSelStart = it.value.lastIndexOf('\n', start - 1)
                                                val indentStart = if (lastLineBreakBeforeSelStart != -1) lastLineBreakBeforeSelStart else 0
                                                val content = it.value.substring(indentStart, end)
                                                val before = it.value.substring(0, indentStart)
                                                val after = it.value.substring(end)

                                                val indentContent = if (indentStart == 0) {
                                                    ("\t" + content).replace("\n", "\n\t")
                                                } else {
                                                    content.replace("\n", "\n\t")
                                                }

                                                it.value = before + indentContent + after
                                                it.selectionStart = start
                                                it.selectionEnd = end + indentContent.length - content.length
                                            }

                                            event.preventDefault()
                                            edit(event.currentTarget.value)
                                        }
                                    }
                                }

                                if (event.ctrlKey && event.key == "z") {
                                    if (undoActive) {
                                        undo()
                                    }
                                }

                                if (event.ctrlKey && event.key == "s") {
                                    event.preventDefault()
                                    build()
                                }

                                if (event.key == "Enter") {
                                    edit(event.currentTarget.value)
                                }

                                if (event.ctrlKey && event.altKey && event.key == "l") {
                                    // REFORMAT CODE
                                    event.preventDefault()
                                    val lines = event.currentTarget.value.split("\n").toMutableList()
                                    for (lineID in lines.indices) {
                                        var lineContent = lines[lineID]
                                        for (reformat in Formatter.reformats) {
                                            lineContent = lineContent.replace(reformat.regex) {
                                                it.value.replaceFirst(it.groupValues[1], reformat.replace)
                                            }
                                        }
                                        lines[lineID] = lineContent
                                    }

                                    val formatted = lines.joinToString("\n") { it }
                                    event.currentTarget.value = formatted
                                    edit(formatted)
                                }
                            }
                        }

                        pre {
                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_HIGHLIGHTING)
                            ariaHidden = true

                            code {
                                css(ClassName(StyleAttr.Main.Editor.TextField.CLASS_HIGHLIGHTING_CONTENT)) {
                                    caretColor = StyleAttr.Main.Editor.FgColor.get()
                                    color = StyleAttr.Main.Editor.FgColor.get()
                                    display = Display.block
                                    width = 100.pct
                                    height = 100.pct
                                    tabSize = StyleAttr.Main.Editor.TextField.tabSize.ch
                                    lineHeight = StyleAttr.Main.Editor.TextField.lineHeight.px
                                    whiteSpace = WhiteSpace.pre
                                    paddingLeft = StyleAttr.paddingSize
                                    border = Border(0.px, LineStyle.hidden)
                                    background = StyleAttr.transparent
                                    resize = Resize.block

                                    Compiler.CodeStyle.entries.forEach {
                                        ".${it.name}" {
                                            color = important(it.get(StyleAttr.mode))
                                        }
                                    }

                                    Compiler.SeverityType.entries.forEach {
                                        ".${it.name}" {
                                            textDecoration = important(TextDecoration.underline)
                                            textDecorationColor = important(it.codeStyle.get(StyleAttr.mode))
                                        }
                                    }
                                }

                                ref = codeAreaRef

                                vcRows.let {
                                    var contentString = ""
                                    for (i in it.indices) {
                                        contentString += "${it[i]}\n"
                                    }
                                    codeAreaRef.current?.let { element ->
                                        element.innerHTML = contentString
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* ----------------- USEEFFECTS (Save and Reload from localStorage) ----------------- */

    useEffect(props.fileState.component1().getCurrID()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Switched File!")
        }
        /* -- LOAD from localStorage -- */
        setFiles(props.fileState.component1().getAllFiles())
        textareaRef.current?.let {
            it.value = props.fileState.component1().getCurrContent()
            edit(it.value)
        }
    }

    useEffect(props.archState) {
        /* -- LOAD from localStorage -- */
        setFiles(props.fileState.component1().getAllFiles())
    }

    useEffect(transcriptView) {
        textareaRef.current?.let {
            it.value = props.fileState.component1().getCurrContent()
        }
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
    }

    useEffect(taValueUpdate) {
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
        updateUndoRedoButton()
    }

    useEffect(props.compileEventState) {
        updateTsButton()
        setFiles(props.fileState.component1().getAllFiles())
    }

    useEffect(state) {
        when (state) {
            ArchState.State.EXECUTABLE -> {
                if (!props.archState.component1().getTranscript().deactivated()) {
                    btnSwitchRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
                }
            }

            else -> {
                btnSwitchRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    useEffect(props.exeEventState) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event Changed!")
        }

        val lineAddressMap = props.archState.component1().getCompiler().getAssemblyMap().lineAddressMap
        val pcValue = props.archState.component1().getRegContainer().pc.variable.get()
        val entry = lineAddressMap[pcValue.toHex().getRawHexStr()]

        if (entry != null) {
            setExeFile(props.fileState.component1().getOrNull(entry.fileName))
            if (entry.fileName == props.fileState.component1().getCurrent().getName()) {
                setCurrExeLine(entry.lineID + 1)
            } else {
                setCurrExeLine(-1)
            }
        } else {
            setCurrExeLine(-1)
            setExeFile(null)
        }
    }

    useEffect(props.fileChangeEvent) {
        setFiles(props.fileState.component1().getAllFiles())
    }
}

object Formatter {
    val reformats = listOf(
        ReFormat(Regex("""( {2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", "),
        ReFormat(Regex("""\s+$"""), ""),
        ReFormat(Regex("""\r\n?"""), "\n"),
    )
}

data class ReFormat(val regex: Regex, val replace: String)