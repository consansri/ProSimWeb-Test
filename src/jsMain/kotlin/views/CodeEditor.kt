package views

import AppLogic
import StyleConst
import emotion.react.css
import extendable.ArchConst
import extendable.components.connected.FileHandler
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
import react.dom.html.ReactHTML.mark
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import react.dom.html.TableRules.Companion.all
import tools.DebugTools
import views.components.TranscriptView
import web.cssom.ClassName
import web.timers.*
import web.cssom.*
import web.cssom.LineStyle.Companion.solid

external interface CodeEditorProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
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

    /* ----------------- REACT STATES ----------------- */

    val appLogic by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val (checkState, setCheckState) = useState(appLogic.getArch().getState().getState())
    val (currExeLine, setCurrExeLine) = useState(1)
    val (taValueUpdate, setTaValueUpdate) = useState(false)

    val (showRenameTab, setShowRenameTab) = useState(false)
    val (showAddTab, setShowAddTab) = useState(false)
    val (showInfoPanel, setShowInfoPanel) = useState(false)
    val (lineNumbers, setLineNumbers) = useState<Int>(1)
    val (infoPanelText, setInfoPanelText) = useState("")

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
            val lineID = lineAddressMap.get(pcValue.toHex().getRawHexStr())
            lineID?.let {
                setCurrExeLine(it + 1)
            }
        }, 10)
    }

    /* ----------------- UPDATE VISUAL COMPONENTS ----------------- */

    fun updateLineNumbers() {
        val textarea = textareaRef.current ?: return
        val numberOfLines = textarea.value.split("\n").size
        setLineNumbers(numberOfLines)
    }

    fun updateTAResize() {
        var height = 0

        textareaRef.current?.let {
            val lineCount = it.value.split("\n").size + 1
            height = lineCount * StyleConst.Main.Editor.TextField.lineHeight
            it.style.height = "auto"
            it.style.height = "${height}px"
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
                btnClearRef.current?.classList?.remove(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
            } else {
                btnClearRef.current?.classList?.add(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    fun updateUndoRedoButton() {
        if (appLogic.getArch().getFileHandler().getCurrUndoLength() > 1) {
            btnUndoRef.current?.classList?.remove(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
        } else {
            btnUndoRef.current?.classList?.add(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
        }
        if (appLogic.getArch().getFileHandler().getCurrRedoLength() > 0) {
            btnRedoRef.current?.classList?.remove(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
        } else {
            btnRedoRef.current?.classList?.add(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun checkCode(immediate: Boolean) {
        val valueToCheck = appLogic.getArch().getFileHandler().getCurrContent()
        val delay: Int
        val size = valueToCheck.split("\n").size

        delay = when {
            size < 500 -> 1000
            size > 3000 -> 3000
            else -> size
        }

        if (immediate) {
            setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
            setCheckState(appLogic.getArch().getState().getState())
        } else {
            checkTimeOutRef.current?.let {
                clearTimeout(it)
            }
            checkTimeOutRef.current = setTimeout({
                setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
                setCheckState(appLogic.getArch().getState().getState())
            }, delay)
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
        }, 300)
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
        className = ClassName(StyleConst.Main.Editor.CLASS)

        div {
            className = ClassName(StyleConst.Main.Editor.Controls.CLASS)

            a {
                id = "switch"
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL)
                ref = btnSwitchRef
                title = "Transcript Switch"
                ReactHTML.img {
                    src = StyleConst.Icons.disassembler
                }

                onClick = {
                    setTranscriptView(!transcriptView)
                }
            }

            a {
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL)

                onClick = {
                    checkCode(true)
                }

                when (checkState) {
                    ArchConst.STATE_UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.Main.CLASS_ANIM_ROTATION)
                            src = StyleConst.Icons.status_loading
                        }
                    }

                    ArchConst.STATE_EXECUTABLE -> {
                        title = "Status: ready to build"
                        img {
                            src = StyleConst.Icons.status_fine
                        }
                    }

                    ArchConst.STATE_HASERRORS -> {
                        title = "Status: fix errors!"
                        img {
                            src = StyleConst.Icons.status_error
                        }
                    }

                    ArchConst.STATE_EXECUTION -> {
                        title = "Status: executing..."
                        img {
                            src = StyleConst.Icons.status_fine
                        }
                    }

                    else -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.Main.CLASS_ANIM_ROTATION)
                            src = StyleConst.Icons.status_loading
                        }
                    }
                }
            }

            a {
                id = "undo"
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL)
                ref = btnUndoRef
                title = "Undo"

                img {
                    src = StyleConst.Icons.backwards
                }

                onClick = {
                    undo()
                }
            }

            a {
                id = "redo"
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL)
                ref = btnRedoRef
                title = "Redo"

                img {
                    src = StyleConst.Icons.forwards
                }

                onClick = {
                    redo()
                }
            }

            a {
                id = "info"
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL)

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
                        - Transcript Switch
                        - Start of Program (click on linenumber)
                        
                    
                """.trimIndent()

                img {
                    src = StyleConst.Icons.info
                }
            }

            a {
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_CONTROL + " " + StyleConst.Main.CLASS_DELETE)
                ref = btnClearRef
                title = "Clear"

                img {
                    src = StyleConst.Icons.delete
                }

                onClick = {
                    edit("", false)
                }
            }

            a {
                ref = infoPanelRef
                className = ClassName(StyleConst.Main.Editor.Controls.CLASS_INFOPANEL)

                title = "{$infoPanelText}"

                if (showInfoPanel) {
                    +infoPanelText
                }

                img {
                    src = StyleConst.Icons.tag
                }

                onClick = {
                    setShowInfoPanel(!showInfoPanel)
                }
            }

        }

        div {
            css(ClassName(StyleConst.Main.Editor.TextField.CLASS)) {
                overflow = Overflow.hidden
                display = Display.flex
                flexDirection = FlexDirection.column
                maxHeight = 100.pc
                width = 100.pc
                this.lineHeight = StyleConst.Main.Editor.TextField.lineHeight.px
                fontFamily = FontFamily.monospace
                backgroundColor = StyleConst.Main.Editor.BgColor.get()
                color = StyleConst.Main.Editor.FgColor.get()
                caretColor = important(StyleConst.Main.Editor.FgColor.get())
                borderRadius = 2.px
                padding = 1.pc
                boxShadow = BoxShadow(0.px, 3.px, 6.px, Color("#000000F7"))
            }

            ref = editorContainerRef

            if (transcriptView) {
                TranscriptView {
                    this.ta_val = ta_val ?: ""
                    this.transcript = appLogic.getArch().getTranscript()
                    this.appLogic = appLogic
                }
            } else {

                div {

                    className = ClassName(StyleConst.Main.Editor.TextField.CLASS_TABS)

                    for (fileID in appLogic.getArch().getFileHandler().getAllFiles().indices) {
                        val file = appLogic.getArch().getFileHandler().getAllFiles()[fileID]
                        div {

                            className = ClassName(StyleConst.Main.Editor.TextField.CLASS_TAB + if (file == appLogic.getArch().getFileHandler().getCurrent()) " ${StyleConst.Main.Editor.TextField.CLASS_TAB_ACTIVE}" else "")

                            if (file == appLogic.getArch().getFileHandler().getCurrent()) {
                                if (!showRenameTab) {
                                    a {
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
                                                    it.classList.add(StyleConst.ANIM_BLINKGREEN)
                                                    setTimeout({
                                                        it.classList.remove(StyleConst.ANIM_BLINKGREEN)
                                                    }, 500)
                                                } else {
                                                    it.classList.add(StyleConst.ANIM_SHAKERED)
                                                    setTimeout({
                                                        it.classList.remove(StyleConst.ANIM_SHAKERED)
                                                    }, 500)
                                                }
                                            }

                                        }

                                    }
                                }

                                img {
                                    src = StyleConst.Icons.delete

                                    onClick = {
                                        appLogic.getArch().getFileHandler().remove(file)
                                        edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
                                        setShowAddTab(false)
                                        setTaValueUpdate(!taValueUpdate)
                                    }
                                }
                            } else {
                                a {

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
                            className = ClassName(StyleConst.Main.Editor.TextField.CLASS_TAB)

                            input {
                                ref = addtabinput
                                type = InputType.text
                                placeholder = "name"

                                onBlur = {
                                    val input = addtabinput.current
                                    input?.let {
                                        val success = appLogic.getArch().getFileHandler().import(FileHandler.File(it.value, ""))
                                        if (success) {
                                            edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
                                            setTaValueUpdate(!taValueUpdate)
                                            setShowAddTab(false)
                                        } else {
                                            input.classList.add(StyleConst.ANIM_SHAKERED)
                                            setTimeout({
                                                input.classList.remove(StyleConst.ANIM_SHAKERED)
                                            }, 300)
                                        }
                                    }
                                }

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
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
                                            input.classList.add(StyleConst.ANIM_SHAKERED)
                                            setTimeout({
                                                input.classList.remove(StyleConst.ANIM_SHAKERED)
                                            }, 300)
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        a {
                            className = ClassName(StyleConst.Main.Editor.TextField.CLASS_TAB)

                            +"+"

                            onClick = { event ->
                                setShowAddTab(true)
                            }
                        }
                    }
                }

                div {
                    className = ClassName(StyleConst.Main.Editor.TextField.CLASS_SCROLL_CONTAINER)

                    div {
                        css(ClassName(StyleConst.Main.Editor.TextField.CLASS_LINE_NUMBERS)) {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            alignItems = AlignItems.flexEnd
                            minWidth = StyleConst.Main.Editor.TextField.minLineNumWidth
                            textAlign = TextAlign.right

                            span {
                                cursor = Cursor.pointer
                                paddingRight = StyleConst.paddingSize
                                fontFamily = FontFamily.monospace
                                borderRight = Border(1.px, LineStyle.solid, StyleConst.Main.Editor.TextField.LineNumbersBorderColor.get())
                                color = StyleConst.Main.Editor.TextField.LineNumbersColor.get()

                                ".${StyleConst.Main.Editor.TextField.CLASS_LINE_ACTIVE}" {
                                    color = important(StyleConst.Main.Editor.TextField.LineActiveColor.get())
                                }
                            }
                        }
                        ref = lineNumbersRef

                        for (lineNumber in 1..lineNumbers) {
                            span {
                                onClick = { event ->
                                    appLogic.getArch().exeUntilLine(lineNumber - 1)
                                    props.updateParent()
                                }

                                if (lineNumber == currExeLine) {
                                    className = ClassName(StyleConst.Main.Editor.TextField.CLASS_LINE_ACTIVE)
                                    +"► $lineNumber"

                                } else {

                                    +"$lineNumber"
                                }
                            }
                        }
                    }

                    div {
                        className = ClassName(StyleConst.Main.Editor.TextField.CLASS_INPUT_DIV)
                        ref = inputDivRef

                        textarea {
                            className = ClassName(StyleConst.Main.Editor.TextField.CLASS_AREA)
                            ref = textareaRef
                            autoComplete = AutoComplete.off
                            autoCorrect = "off"
                            cols = 50
                            autoCapitalize = "off"
                            spellCheck = false
                            placeholder = "Enter ${appLogic.getArch().getName()} Assembly ..."

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
                            className = ClassName(StyleConst.Main.Editor.TextField.CLASS_HIGHLIGHTING)
                            ariaHidden = true

                            code {
                                css(ClassName(StyleConst.Main.Editor.TextField.CLASS_HIGHLIGHTING_CONTENT)) {
                                    caretColor = StyleConst.Main.Editor.FgColor.get()
                                    color = StyleConst.Main.Editor.FgColor.get()
                                    display = Display.block
                                    width = 100.pct
                                    height = 100.pct
                                    tabSize = StyleConst.Main.Editor.TextField.tabSize.ch
                                    lineHeight = StyleConst.Main.Editor.TextField.lineHeight.px
                                    whiteSpace = WhiteSpace.pre
                                    overflowX = Overflow.clip
                                    overflowWrap = OverflowWrap.normal
                                    paddingLeft = StyleConst.paddingSize
                                    border = Border(0.px, LineStyle.hidden)
                                    background = StyleConst.transparent
                                    resize = Resize.block

                                    for (entry in StyleConst.Main.Editor.HL.entries) {
                                        ".${entry.getFlag()}" {
                                            when (entry.appendsOn) {
                                                StyleConst.Main.Editor.On.BackgroundColor -> {
                                                    backgroundColor = important(entry.color.get())
                                                }

                                                StyleConst.Main.Editor.On.Color -> {
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

    useEffect(update) {
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
            ArchConst.STATE_EXECUTABLE -> {
                btnSwitchRef.current?.classList?.remove(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
            }

            else -> {
                btnSwitchRef.current?.classList?.add(StyleConst.Main.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    useEffect(transcriptView) {
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
            edit(it.value, false)
        }
        btnSwitchRef.current?.let {
            if (transcriptView) {
                it.classList.add(StyleConst.Main.Editor.Controls.CLASS_ACTIVE)
            } else {
                it.classList.remove(StyleConst.Main.Editor.Controls.CLASS_ACTIVE)
            }
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

}

object Formatter {
    val reformats = listOf<ReFormat>(
        ReFormat(Regex("""(\s{2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", ")
    )
}

data class ReFormat(val regex: Regex, val replace: String)