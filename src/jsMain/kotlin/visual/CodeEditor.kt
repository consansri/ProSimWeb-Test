package visual

import StyleAttr
import emotion.react.css
import emulator.kit.common.FileHandler
import kotlinx.browser.window
import kotlinx.coroutines.*
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
import emulator.kit.Architecture
import emulator.kit.common.ArchState
import web.cssom.ClassName
import web.timers.*
import web.cssom.*

external interface CodeEditorProps : Props {
    var archState: StateInstance<Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var fileChangeEvent: StateInstance<Boolean>
}

@OptIn(DelicateCoroutinesApi::class)
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

    val (state, setState) = useState(props.archState.component1().getState().state)

    val (currExeLine, setCurrExeLine) = useState(-1)
    val (exeFile, setExeFile) = useState<FileHandler.File>()
    val (taValueUpdate, setTaValueUpdate) = useState(false)

    val (showRenameTab, setShowRenameTab) = useState(false)
    val (showAddTab, setShowAddTab) = useState(false)
    val (showInfoPanel, setShowInfoPanel) = useState(false)
    val (lineNumbers, setLineNumbers) = useState<Int>(1)
    val (infoPanelText, setInfoPanelText) = useState("")

    val (tsActive, setTSActive) = useState(false)
    val (undoActive, setUndoActive) = useState(false)
    val (redoActive, setRedoActive) = useState(false)
    /* ----------------- localStorage Sync Objects ----------------- */

    val (vc_rows, setvc_rows) = useState<List<String>>(emptyList())
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
        var width = 0

        textareaRef.current?.let {
            val lineCount = it.value.split("\n").size + 1
            width = it.value.split("\n").map { it.length }.max()
            height = lineCount * StyleAttr.Main.Editor.TextField.lineHeight
            it.style.height = "auto"
            it.style.height = "${height}px"
            it.style.width = "auto"
            it.style.width = "${width + 1}ch"
        }
        inputDivRef.current?.let {
            it.style.height = "auto"
            if (height != 0) {
                it.style.height = "${height}px"
            }
        }
    }

    fun updateTsButton() {
        if (props.archState.component1().getState().state == ArchState.State.EXECUTABLE || props.archState.component1().getState().state == ArchState.State.EXECUTION) {
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
        if (props.archState.component1().getFileHandler().getCurrUndoLength() > 1) {
            btnUndoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            setUndoActive(true)
        } else {
            setUndoActive(false)
            btnUndoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
        if (props.archState.component1().getFileHandler().getCurrRedoLength() > 0) {
            setRedoActive(true)
            btnRedoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        } else {
            setRedoActive(false)
            btnRedoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun checkCode(immediate: Boolean) {
        val valueToCheck = props.archState.component1().getFileHandler().getCurrContent()
        val delay = 1000

        if (immediate) {
            /* ----------------- COROUTINES ----------------- */
            checkTimeOutRef.current?.let {
                clearTimeout(it)
            }
            checkTimeOutRef.current = setTimeout({
                props.archState.component1().getState().edit()
                setvc_rows(props.archState.component1().check(valueToCheck, currExeLine).split("\n"))
                props.compileEventState.component2().invoke(!props.compileEventState.component1())
                setState(props.archState.component1().getState().state)
            }, 0)

        } else {
            val size = valueToCheck.split("\n").size
            if (size < 250 && !DebugTools.REACT_deactivateAutomaticBuilds) {
                checkTimeOutRef.current?.let {
                    clearTimeout(it)
                }
                checkTimeOutRef.current = setTimeout({
                    props.archState.component1().getState().edit()
                    setvc_rows(props.archState.component1().check(valueToCheck, currExeLine).split("\n"))
                    props.compileEventState.component2().invoke(!props.compileEventState.component1())
                    setState(props.archState.component1().getState().state)
                }, delay)
            }
        }

    }

    fun preHighlight() {
        val value = props.archState.component1().getFileHandler().getCurrContent()
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current = setTimeout({
            val hlTaList = props.archState.component1().getPreHighlighting(value).split("\n")
            setvc_rows(hlTaList)
        }, 1000)
    }

    /* ----------------- CHANGE EVENTS ----------------- */
    fun edit(content: String, immediate: Boolean) {
        props.archState.component1().getState().edit()

        props.archState.component1().getFileHandler().editCurr(content)
        textareaRef.current?.let {
            it.value = props.archState.component1().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(immediate)
        setTaValueUpdate(!taValueUpdate)
    }

    fun undo() {
        props.archState.component1().getState().edit()

        props.archState.component1().getFileHandler().undoCurr()
        textareaRef.current?.let {
            it.value = props.archState.component1().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(true)
        setTaValueUpdate(!taValueUpdate)
    }

    fun redo() {
        props.archState.component1().getState().edit()


        props.archState.component1().getFileHandler().redoCurr()
        textareaRef.current?.let {
            it.value = props.archState.component1().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(true)
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
                gap = StyleAttr.paddingSize
                minWidth = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding

                a {
                    width = StyleAttr.Main.Editor.Controls.iconSize + 2 * StyleAttr.Main.Editor.Controls.iconPadding
                    boxShadow = BoxShadow(0.px, 3.px, 8.px, rgb(0, 0, 0, 0.24))
                    padding = StyleAttr.Main.Editor.Controls.iconPadding
                    borderRadius = StyleAttr.Main.Editor.Controls.borderRadius
                    background = StyleAttr.Main.Editor.Controls.BgColor.get()
                    color = StyleAttr.Main.Editor.Controls.FgColor.get()
                    transition = Transition(TransitionProperty.all, 0.1.s, TransitionTimingFunction.ease)
                }
                img {
                    width = StyleAttr.Main.Editor.Controls.iconSize
                    height = StyleAttr.Main.Editor.Controls.iconSize
                    filter = StyleAttr.Main.Editor.Controls.iconFilter
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

                    else -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleAttr.Main.CLASS_ANIM_ROTATION)
                            src = StyleAttr.Icons.status_loading
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

                        hover {
                            filter = important(StyleAttr.iconActiveFilter)
                        }
                    }
                    id = "build"
                    title = "build"

                    img {
                        src = StyleAttr.Icons.build
                    }
                    onClick = {
                        checkCode(true)
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
                        - CTRL + Z  (Undo)
                        - CTRL + C  (Copy)
                        - CTRL + V  (Insert)
                        - CTRL + A  (Select All)
                        - TAB       (Insert Tab)
                        - CTRL + ALT + L (Reformat)
                        
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
                        background = important(StyleAttr.Main.DeleteColor)
                        cursor = Cursor.pointer
                    }
                    ref = btnClearRef
                    title = "Clear"

                    img {
                        src = StyleAttr.Icons.delete_black
                    }

                    onClick = {
                        edit("", false)
                    }
                }

                a {
                    ref = infoPanelRef
                    css {
                        cursor = Cursor.pointer
                        position = Position.absolute
                        bottom = 0.rem
                        display = Display.block
                        writingMode = WritingMode.verticalRl
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
                borderRadius = StyleAttr.borderRadius
                padding = StyleAttr.paddingSize
                boxShadow = StyleAttr.Main.elementShadow
            }

            ref = editorContainerRef

            if (transcriptView) {
                TranscriptView {
                    this.ta_val = ta_val
                    this.arch = props.archState
                    this.exeEventState = props.exeEventState
                    this.compileEventState = props.compileEventState
                }
            } else {

                div {

                    className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TABS)

                    for (fileID in files.indices) {
                        val file = props.archState.component1().getFileHandler().getAllFiles()[fileID]
                        div {

                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TAB + if (file == props.archState.component1().getFileHandler().getCurrent()) " ${StyleAttr.Main.Editor.TextField.CLASS_TAB_ACTIVE}" else "")

                            img {
                                src = if (file.getLinkedTree() != null) {
                                    StyleAttr.Icons.file_compiled
                                } else {
                                    StyleAttr.Icons.file_not_compiled
                                }
                            }

                            if (file == props.archState.component1().getFileHandler().getCurrent()) {
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
                                            val success = props.archState.component1().getFileHandler().renameCurrent(it.currentTarget.value)
                                            renameinput.current?.let {
                                                if (success) {
                                                    it.classList.add(StyleAttr.ANIM_BLINKGREEN)
                                                    setTimeout({
                                                        it.classList.remove(StyleAttr.ANIM_BLINKGREEN)
                                                    }, 500)
                                                } else {
                                                    it.classList.add(StyleAttr.ANIM_SHAKERED)
                                                    setTimeout({
                                                        it.classList.remove(StyleAttr.ANIM_SHAKERED)
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
                                            props.archState.component1().getFileHandler().remove(file)
                                            edit(props.archState.component1().getFileHandler().getCurrContent(), false)
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
                                        props.archState.component1().getFileHandler().setCurrent(fileID)
                                        edit(props.archState.component1().getFileHandler().getCurrContent(), false)
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
                                            val success = props.archState.component1().getFileHandler().import(FileHandler.File(it.value, ""))
                                            if (success) {
                                                edit(props.archState.component1().getFileHandler().getCurrContent(), false)
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
                                        val success = props.archState.component1().getFileHandler().import(FileHandler.File(it.value, ""))
                                        if (success) {
                                            edit(props.archState.component1().getFileHandler().getCurrContent(), false)
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

                            onClick = { event ->
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
                                onClick = { event ->
                                    props.archState.component1().exeUntilLine(lineNumber - 1)
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

                                    val grammarTree = props.archState.component1().getAssembly().getGrammarTree()
                                    grammarTree?.rootNode?.let { rootNode ->
                                        var path = ""
                                        rootNode.containers.forEach {
                                            it.getAllTokens().forEach {
                                                if (it.lineLoc.lineID == lineID && startIndex in it.lineLoc.startIndex..it.lineLoc.endIndex) {
                                                    val result = grammarTree.contains(it)
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
                                edit(event.currentTarget.value, false)
                            }

                            onKeyDown = { event ->
                                if (event.key == "Tab") {

                                    textareaRef.current?.let {
                                        val start =
                                            it.selectionStart
                                                ?: error("CodeEditor: OnKeyDown Tab handling: start is null")
                                        val end =
                                            it.selectionEnd ?: error("CodeEditor: OnKeyDown Tab handling: end is null")

                                        it.value = it.value.substring(0, start) + '\t' + it.value.substring(end)

                                        it.selectionEnd = end + 1

                                        event.preventDefault()
                                        edit(event.currentTarget.value, false)
                                    }
                                } else if (event.ctrlKey && event.key == "z") {
                                    if (undoActive) {
                                        undo()
                                    }
                                } else if (event.key == "Enter") {
                                    edit(event.currentTarget.value, false)
                                } else if (event.ctrlKey && event.altKey && event.key == "l") {
                                    // REFORMAT CODE
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
                                    edit(formatted, false)
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

                                    for (entry in StyleAttr.Main.Editor.HL.entries) {
                                        ".${entry.getFlag()}" {
                                            when (entry.appendsOn) {
                                                StyleAttr.Main.Editor.On.BackgroundColor -> {
                                                    backgroundColor = important(entry.color.get())
                                                }

                                                StyleAttr.Main.Editor.On.Color -> {
                                                    color = important(entry.color.get())
                                                }
                                            }
                                        }
                                    }
                                }

                                ref = codeAreaRef

                                vc_rows.let {
                                    var contentString = ""
                                    for (i in it.indices) {
                                        contentString += "${it[i]}\n"
                                    }
                                    codeAreaRef.current?.let {
                                        it.innerHTML = contentString
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

    useEffect(props.archState.component1().getFileHandler().getCurrID()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Switched File!")
        }
        /* -- LOAD from localStorage -- */
        setFiles(props.archState.component1().getFileHandler().getAllFiles())
        textareaRef.current?.let {
            it.value = props.archState.component1().getFileHandler().getCurrContent()
            edit(it.value, false)
        }
    }

    useEffect(props.archState) {
        /* -- LOAD from localStorage -- */
        setFiles(props.archState.component1().getFileHandler().getAllFiles())
    }

    useEffect(transcriptView) {
        textareaRef.current?.let {
            it.value = props.archState.component1().getFileHandler().getCurrContent()
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
        setFiles(props.archState.component1().getFileHandler().getAllFiles())
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

        val lineAddressMap = props.archState.component1().getAssembly().getAssemblyMap().lineAddressMap
        val pcValue = props.archState.component1().getRegContainer().pc.variable.get()
        val entry = lineAddressMap.get(pcValue.toHex().getRawHexStr())

        if (entry != null) {
            setExeFile(entry.file)
            if (entry.file == props.archState.component1().getFileHandler().getCurrent()) {
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
        setFiles(props.archState.component1().getFileHandler().getAllFiles())
    }
}

object Formatter {
    val reformats = listOf<ReFormat>(
        ReFormat(Regex("""(\s{2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", ")
    )
}

data class ReFormat(val regex: Regex, val replace: String)