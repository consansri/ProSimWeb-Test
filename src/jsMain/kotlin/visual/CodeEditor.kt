package visual

import emulator.Emulator
import StyleAttr
import emotion.react.css
import emulator.kit.Settings
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
import web.cssom.ClassName
import web.timers.*
import web.cssom.*

external interface CodeEditorProps : Props {
    var emulator: Emulator
    var update: Boolean
    var updateParent: () -> Unit
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
    val executionPointInterval = useRef<Timeout>(null)

    var job: Job? = null

    /* ----------------- REACT STATES ----------------- */

    val appLogic by useState(props.emulator)
    val (checkState, setCheckState) = useState(appLogic.getArch().getState().getState())
    val (currExeLine, setCurrExeLine) = useState(-1)
    val (exeFile, setExeFile) = useState<FileHandler.File>()
    val (taValueUpdate, setTaValueUpdate) = useState(false)

    val (showRenameTab, setShowRenameTab) = useState(false)
    val (showAddTab, setShowAddTab) = useState(false)
    val (showInfoPanel, setShowInfoPanel) = useState(false)
    val (lineNumbers, setLineNumbers) = useState<Int>(1)
    val (infoPanelText, setInfoPanelText) = useState("")

    val (undoActive, setUndoActive) = useState(false)
    val (redoActive, setRedoActive) = useState(false)
    /* ----------------- localStorage Sync Objects ----------------- */

    val (vc_rows, setvc_rows) = useState<List<String>>(emptyList())
    val (files, setFiles) = useState<List<FileHandler.File>>()
    val (transcriptView, setTranscriptView) = useState(false)

    /* ----------------- Initiate Intervals ----------------- */

    executionPointInterval.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        executionPointInterval.current = setInterval({
            val lineAddressMap = appLogic.getArch().getAssembly().getAssemblyMap().lineAddressMap
            val pcValue = appLogic.getArch().getRegisterContainer().pc.value.get()
            val entry = lineAddressMap.get(pcValue.toHex().getRawHexStr())

            if (entry != null) {
                setExeFile(entry.file)
                if (entry.file == appLogic.getArch().getFileHandler().getCurrent()) {
                    setCurrExeLine(entry.lineID + 1)
                } else {
                    setCurrExeLine(-1)
                }
            } else {
                setCurrExeLine(-1)
                setExeFile(null)
            }
        }, 50)
    }

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
        if (appLogic.getArch().getFileHandler().getCurrUndoLength() > 1) {
            btnUndoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            setUndoActive(true)
        } else {
            setUndoActive(false)
            btnUndoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
        if (appLogic.getArch().getFileHandler().getCurrRedoLength() > 0) {
            setRedoActive(true)
            btnRedoRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        } else {
            setRedoActive(false)
            btnRedoRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun checkCode(immediate: Boolean) {
        val valueToCheck = appLogic.getArch().getFileHandler().getCurrContent()
        val delay = 1000

        if (immediate) {
            setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
            setCheckState(appLogic.getArch().getState().getState())
        } else {
            val size = valueToCheck.split("\n").size
            if (size < 250) {
                checkTimeOutRef.current?.let {
                    clearTimeout(it)
                }
                checkTimeOutRef.current = setTimeout({
                    setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
                    setCheckState(appLogic.getArch().getState().getState())
                }, delay)
            }
        }
    }

    fun preHighlight() {
        val value = appLogic.getArch().getFileHandler().getCurrContent()
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current = setTimeout({
            val hlTaList = appLogic.getArch().getPreHighlighting(value).split("\n")
            setvc_rows(hlTaList)
        }, 1000)
    }

    /* ----------------- CHANGE EVENTS ----------------- */
    fun edit(content: String, immediate: Boolean) {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().editCurr(content)
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(immediate)
        setTaValueUpdate(!taValueUpdate)
    }

    fun undo() {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().undoCurr()
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(true)
        setTaValueUpdate(!taValueUpdate)
    }

    fun redo() {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().redoCurr()
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
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

                if (checkState == Settings.STATE_EXECUTABLE) {
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

                when (checkState) {
                    Settings.STATE_UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleAttr.Main.CLASS_ANIM_ROTATION)
                            src = StyleAttr.Icons.status_loading
                        }
                    }

                    Settings.STATE_EXECUTABLE -> {
                        title = "Status: ready to build"
                        img {
                            src = StyleAttr.Icons.status_fine
                        }
                    }

                    Settings.STATE_HASERRORS -> {
                        title = "Status: fix errors!"
                        img {
                            src = StyleAttr.Icons.status_error
                        }
                    }

                    Settings.STATE_EXECUTION -> {
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
                    this.ta_val = ta_val ?: ""
                    this.transcript = appLogic.getArch().getTranscript()
                    this.emulator = appLogic
                    this.updateParent = props.updateParent
                }
            } else {

                div {

                    className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TABS)

                    for (fileID in appLogic.getArch().getFileHandler().getAllFiles().indices) {
                        val file = appLogic.getArch().getFileHandler().getAllFiles()[fileID]
                        div {

                            className = ClassName(StyleAttr.Main.Editor.TextField.CLASS_TAB + if (file == appLogic.getArch().getFileHandler().getCurrent()) " ${StyleAttr.Main.Editor.TextField.CLASS_TAB_ACTIVE}" else "")

                            img {
                                src = if (file.getLinkedTree() != null) {
                                    StyleAttr.Icons.file_compiled
                                } else {
                                    StyleAttr.Icons.file_not_compiled
                                }
                            }

                            if (file == appLogic.getArch().getFileHandler().getCurrent()) {
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
                                            val success = appLogic.getArch().getFileHandler().renameCurrent(it.currentTarget.value)
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
                                            appLogic.getArch().getFileHandler().remove(file)
                                            edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
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
                                        appLogic.getArch().getFileHandler().setCurrent(fileID)
                                        edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
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
                                            val success = appLogic.getArch().getFileHandler().import(FileHandler.File(it.value, ""))
                                            if (success) {
                                                edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
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
                                        val success = appLogic.getArch().getFileHandler().import(FileHandler.File(it.value, ""))
                                        if (success) {
                                            edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
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
                                    appLogic.getArch().exeUntilLine(lineNumber - 1)
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
                            placeholder = "Enter ${appLogic.getArch().getDescription().name} Assembly ..."

                            onSelect = { event ->
                                val cursorPosition = event.currentTarget.selectionStart
                                cursorPosition.let { cursorPos ->

                                    val lines = event.currentTarget.value.substring(0, cursorPos).split("\n")
                                    val lineID = lines.size - 1
                                    val startIndex = lines[lineID].length

                                    val grammarTree = appLogic.getArch().getAssembly().getGrammarTree()
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
                                    undo()
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

    useEffect(props.update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) CodeEditor")
        }
        /* Component RELOAD */
        appLogic.getArch().getFileHandler().getFromLocalStorage()
        /* -- LOAD from localStorage -- */
        setFiles(appLogic.getArch().getFileHandler().getAllFiles())
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
            edit(it.value, false)
        }
    }

    useEffect(checkState) {
        when (checkState) {
            Settings.STATE_EXECUTABLE -> {
                if (!appLogic.getArch().getTranscript().deactivated()) {
                    btnSwitchRef.current?.classList?.remove(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
                }
            }

            else -> {
                btnSwitchRef.current?.classList?.add(StyleAttr.Main.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    useEffect(transcriptView) {
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
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

    useEffect(props.update) {
        setFiles(appLogic.getArch().getFileHandler().getAllFiles())
    }

    useEffect(StyleAttr.mode) {
        checkCode(false)
    }
}

object Formatter {
    val reformats = listOf<ReFormat>(
        ReFormat(Regex("""(\s{2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", ")
    )
}

data class ReFormat(val regex: Regex, val replace: String)