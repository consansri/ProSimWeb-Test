package views

import AppLogic
import StorageKey
import StyleConst
import csstype.ClassName
import extendable.ArchConst
import extendable.components.assembly.Grammar
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.js.timers.*
import org.w3c.dom.*
import react.*
import react.dom.aria.ariaHidden
import react.dom.html.AutoComplete
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import tools.DebugTools
import views.components.TranscriptView

external interface CodeEditorProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}


val CodeEditor = FC<CodeEditorProps> { props ->

    /* ----------------- STATIC VARIABLES ----------------- */

    val lineHeight = 21

    /* ----------------- REACT REFERENCES ----------------- */

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnSwitchRef = useRef<HTMLAnchorElement>(null)
    val btnDarkModeRef = useRef<HTMLAnchorElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val infoPanelRef = useRef<HTMLAnchorElement>(null)
    val editorContainerRef = useRef<HTMLDivElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)

    val undoTimeoutRef = useRef<Timeout>(null)
    val preHLTimeoutRef = useRef<Timeout>(null)
    val checkTimeOutRef = useRef<Timeout>(null)
    val executionPointInterval = useRef<Timeout>(null)

    /* ----------------- REACT STATES ----------------- */

    val appLogic by useState(props.appLogic)
    val (update, setUpdate) = useState(props.update)
    val (internalUpdate, setIUpdate) = useState(false)
    val (checkState, setCheckState) = useState(appLogic.getArch().getState().getState())
    val (currExeLine, setCurrExeLine) = useState(1)

    val (lineNumbers, setLineNumbers) = useState<Int>(1)
    val (darkMode, setDarkMode) = useState(false)
    val (infoPanelText, setInfoPanelText) = useState("")

    /* ----------------- localStorage Sync Objects ----------------- */

    val (vc_rows, setvc_rows) = useState<List<String>>()
    val (ta_val, setta_val) = useState<String>()
    val (ta_val_ss, setta_val_ss) = useState<List<String>>()
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
        }, 500)
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
            height = lineCount * lineHeight
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
                btnClearRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
            } else {
                btnClearRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    fun updateUndoButton() {
        if (ta_val_ss != null && ta_val_ss.size > 1) {
            btnUndoRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
        } else {
            btnUndoRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun addUndoState(taValue: String, immediate: Boolean) {
        if (immediate) {
            val tempTaValSS = ta_val_ss?.toMutableList() ?: mutableListOf<String>()
            if (tempTaValSS.size > 30) {
                tempTaValSS.removeFirst()
            }
            tempTaValSS += taValue
            setta_val_ss(tempTaValSS)
        } else {
            undoTimeoutRef.current?.let {
                clearTimeout(it)
            }
            undoTimeoutRef.current = setTimeout({
                val tempTaValSS = ta_val_ss?.toMutableList() ?: mutableListOf<String>()
                if (tempTaValSS.size > 30) {
                    tempTaValSS.removeFirst()
                }
                tempTaValSS += taValue
                setta_val_ss(tempTaValSS)
            }, 1000)
        }
    }

    fun checkCode(taValue: String, immediate: Boolean) {
        val delay: Int
        val size = taValue.split("\n").size

        delay = when {
            size < 500 -> 1000
            size > 3000 -> 3000
            else -> size
        }

        if (immediate) {
            setvc_rows(appLogic.getArch().check(taValue, currExeLine).split("\n"))
            setCheckState(appLogic.getArch().getState().getState())
        } else {
            checkTimeOutRef.current?.let {
                clearTimeout(it)
            }
            checkTimeOutRef.current = setTimeout({
                setvc_rows(appLogic.getArch().check(taValue, currExeLine).split("\n"))
                setCheckState(appLogic.getArch().getState().getState())
            }, delay)
        }
    }

    fun preHighlight(taValue: String) {
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        setvc_rows(taValue.split("\n"))
        preHLTimeoutRef.current = setTimeout({
            val hlTaList = appLogic.getArch().getPreHighlighting(taValue).split("\n")
            setvc_rows(hlTaList)
        }, 300)
    }

    fun preHighlight(taChangedRows: Map<Int, String>) {
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        preHLTimeoutRef.current = setTimeout({
            val prevLines = vc_rows?.toMutableList()
            prevLines?.let {
                for (line in taChangedRows) {
                    prevLines[line.key] = appLogic.getArch().getPreHighlighting(line.value)
                }
            }
            setvc_rows(prevLines)
        }, 0)

    }

    /* ----------------- SYNC EVENTS ----------------- */

    fun undo() {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())
        val tempTaValSS = ta_val_ss?.toMutableList()
        tempTaValSS?.let {
            if (it.last() == ta_val) {
                it.removeLast()
            }
            setta_val(it.last())
            checkCode(it.last(), true)
            setta_val_ss(tempTaValSS)
        }

    }

    /* ----------------- DOM ----------------- */

    div {
        className = ClassName(StyleConst.CLASS_EDITOR)

        div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTROLS)

            a {
                id = "switch"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
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
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)

                onClick = {
                    if (ta_val != null) {
                        checkCode(ta_val, true)
                    }
                }

                when (checkState) {
                    ArchConst.STATE_UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
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
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
                            src = StyleConst.Icons.status_loading
                        }
                    }
                }
            }

            a {
                id = "darkmode"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnDarkModeRef
                title = "Editor Mode"

                img {
                    if (darkMode) {
                        src = StyleConst.Icons.darkmode
                    } else {
                        src = StyleConst.Icons.lightmode
                    }
                }

                onClick = { event ->
                    setDarkMode(!darkMode)
                }

            }

            a {
                id = "undo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
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
                id = "info"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)

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
                id = "editor-clear"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnClearRef
                title = "Clear"

                img {
                    src = StyleConst.Icons.delete
                }

                onClick = {
                    ta_val?.let {
                        addUndoState(ta_val, true)
                    }
                    setta_val("")
                    setvc_rows(listOf(""))
                }
            }

            a {
                id = "editor-info-panel"
                ref = infoPanelRef

                title = "{$infoPanelText}"

                img {
                    src = StyleConst.Icons.tag
                }


            }

        }

        div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTAINER)
            ref = editorContainerRef

            if (transcriptView) {
                TranscriptView {
                    this.ta_val = ta_val ?: ""
                    this.transcript = appLogic.getArch().getTranscript()
                    this.appLogic = appLogic
                }
            } else {

                div {
                    className = ClassName(StyleConst.CLASS_EDITOR_SCROLL_CONTAINER)

                    div {
                        className = ClassName(StyleConst.CLASS_EDITOR_LINE_NUMBERS)
                        ref = lineNumbersRef

                        for (lineNumber in 1..lineNumbers) {
                            span {

                                if (lineNumber == currExeLine) {
                                    className = ClassName(StyleConst.CLASS_EDITOR_LINE_ACTIVE)

                                    +"► $lineNumber"
                                    onClick = {
                                        setCurrExeLine(1)
                                    }
                                } else {
                                    onClick = {
                                        setCurrExeLine(lineNumber)
                                    }
                                    +"$lineNumber"
                                }
                            }
                        }
                    }

                    div {
                        className = ClassName(StyleConst.CLASS_EDITOR_INPUT_DIV)
                        ref = inputDivRef

                        textarea {
                            className = ClassName(StyleConst.CLASS_EDITOR_AREA)
                            ref = textareaRef
                            autoComplete = AutoComplete.off
                            autoCorrect = "off"
                            cols = 50
                            autoCapitalize = "off"
                            spellCheck = false
                            placeholder = "Enter ${appLogic.getArch().getName()} Assembly ..."

                            onSelect = { event ->
                                val cursorPosition = event.currentTarget.selectionStart
                                cursorPosition?.let { cursorPos ->

                                    val lines = event.currentTarget.value.substring(0, cursorPos).split("\n")
                                    val lineID = lines.size - 1
                                    val startIndex = lines[lineID].length

                                    val grammarTree = appLogic.getArch().getAssembly().getGrammarTree()
                                    grammarTree?.rootNode?.let { rootNode ->
                                        for (node in rootNode.sections) {
                                            when (node) {
                                                is Grammar.TreeNode.CollectionNode -> {
                                                    val firstTokenLineID = node.tokenNodes[0].tokens.first().lineLoc.lineID
                                                    val firstTokenStart = node.tokenNodes[0].tokens.first().lineLoc.startIndex
                                                    val lastTokenEnd = node.tokenNodes[node.tokenNodes.lastIndex].tokens.last().lineLoc.endIndex
                                                    var text = ""
                                                    if (firstTokenLineID == lineID && (startIndex in firstTokenStart..lastTokenEnd)) {
                                                        text += node.name + " -> "
                                                        val childs = mutableListOf<String>()
                                                        for (tokenNode in node.tokenNodes) {
                                                            val firstChildLineLoc = tokenNode.tokens.first().lineLoc
                                                            val childLineID = firstChildLineLoc.lineID
                                                            val firstChildStart = firstChildLineLoc.startIndex
                                                            val lastChildEnd = tokenNode.tokens.last().lineLoc.endIndex
                                                            if (startIndex in firstChildStart..lastChildEnd) {
                                                                childs += tokenNode.name
                                                            }
                                                        }
                                                        setInfoPanelText(text + childs.joinToString(" , ") { it } + " ${node.tokenNodes[0].tokens.first().id}")
                                                        break
                                                    }
                                                }

                                                is Grammar.TreeNode.TokenNode -> {
                                                    val firstTokenLineID = node.tokens.first().lineLoc.lineID
                                                    val firstTokenStart = node.tokens.first().lineLoc.startIndex
                                                    val lastTokenEnd = node.tokens.last().lineLoc.endIndex
                                                    if (firstTokenLineID == lineID && (startIndex in firstTokenStart..lastTokenEnd)) {
                                                        setInfoPanelText(node.name + " ${node.tokens.first().id}")
                                                        break
                                                    }
                                                }

                                                is Grammar.TreeNode.SectionNode -> {
                                                    var text = "${node.name} -> "
                                                    for (collNode in node.collNodes) {
                                                        val firstTokenLineID = collNode.tokenNodes[0].tokens.first().lineLoc.lineID
                                                        val firstTokenStart = collNode.tokenNodes[0].tokens.first().lineLoc.startIndex
                                                        val lastTokenEnd = collNode.tokenNodes[collNode.tokenNodes.lastIndex].tokens.last().lineLoc.endIndex

                                                        if (firstTokenLineID == lineID && (startIndex in firstTokenStart..lastTokenEnd)) {
                                                            text += collNode.name + " -> "
                                                            val childs = mutableListOf<String>()
                                                            for (tokenNode in collNode.tokenNodes) {
                                                                val firstChildLineLoc = tokenNode.tokens.first().lineLoc
                                                                val childLineID = firstChildLineLoc.lineID
                                                                val firstChildStart = firstChildLineLoc.startIndex
                                                                val lastChildEnd = tokenNode.tokens.last().lineLoc.endIndex
                                                                if (startIndex in firstChildStart..lastChildEnd) {
                                                                    childs += tokenNode.name
                                                                }
                                                            }
                                                            setInfoPanelText(text + childs.joinToString(" , ") { it } + " ${collNode.tokenNodes[0].tokens.first().id}")
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                            setInfoPanelText("")
                                        }
                                    }
                                }
                            }

                            onChange = { event ->
                                setta_val(event.currentTarget.value)

                                addUndoState(event.currentTarget.value, false)

                                // Fire Change Events
                                val lines = event.currentTarget.value.split("\n")
                                if (vc_rows != null) {
                                    if (lines.size != vc_rows.size) {
                                        preHighlight(event.currentTarget.value)

                                    } else {
                                        val selStart = event.currentTarget.selectionStart ?: 0
                                        val selEnd =
                                            event.currentTarget.selectionEnd ?: (event.currentTarget.value.length - 1)
                                        val lineIDStart =
                                            event.currentTarget.value.substring(0, selStart).split("\n").size - 1
                                        val lineIDEnd =
                                            event.currentTarget.value.substring(0, selEnd).split("\n").size - 1
                                        val editedLines: MutableMap<Int, String> = mutableMapOf()
                                        for (lineID in lineIDStart..lineIDEnd) {
                                            editedLines.put(lineID, lines[lineID])
                                        }

                                        preHighlight(editedLines)

                                    }
                                } else {
                                    preHighlight(event.currentTarget.value)
                                }
                                //

                                appLogic.getArch().getState().edit()
                                setCheckState(appLogic.getArch().getState().getState())
                                checkCode(event.currentTarget.value, false)

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
                                        setta_val(event.currentTarget.value)

                                        val lines = event.currentTarget.value.split("\n")
                                        val lineIDStart =
                                            event.currentTarget.value.substring(0, start).split("\n").size - 1
                                        val lineIDEnd =
                                            event.currentTarget.value.substring(0, end).split("\n").size - 1
                                        val editedLines: MutableMap<Int, String> = mutableMapOf()
                                        for (lineID in lineIDStart..lineIDEnd) {
                                            editedLines.put(lineID, lines[lineID])
                                        }
                                        preHighlight(editedLines)

                                        checkCode(event.currentTarget.value, false)
                                    }
                                } else if (event.ctrlKey && event.key == "z") {
                                    undo()
                                } else if (event.key == "Enter") {
                                    preHighlight(event.currentTarget.value)
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
                                    preHighlight(formatted)
                                    checkCode(formatted, false)
                                }
                            }
                        }

                        pre {
                            className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING)
                            ariaHidden = true

                            code {
                                className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_LANGUAGE)
                                className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_CONTENT)
                                ref = codeAreaRef

                                vc_rows?.let {
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
        textareaRef.current?.let {
            checkCode(it.value, true)
        }
    }


    useEffect(update, transcriptView) {
        /* Component RELOAD */

        /* -- LOAD from localStorage -- */
        /* ta_val */
        setta_val(localStorage.getItem(StorageKey.TA_VALUE))
        textareaRef.current?.let {
            it.value = ta_val ?: ""
        }

        /* vc_rows */
        val vc_rows_length = localStorage.getItem(StorageKey.VC_ROW_Length)?.toInt() ?: 0
        if (vc_rows_length > 0) {
            val tempVcRows: MutableList<String> = mutableListOf()
            for (i in 0 until vc_rows_length) {
                val vcRowContent = localStorage.getItem(StorageKey.VC_ROW + i) ?: ""
                tempVcRows.add(i, vcRowContent)
            }
            setvc_rows(tempVcRows)
        }

        /* ta_val_ss */
        val ta_val_ss_length = localStorage.getItem(StorageKey.TA_VALUE_SaveState_Length)?.toInt() ?: 0
        if (ta_val_ss_length > 0) {
            val tempTaValSS: MutableList<String> = mutableListOf()
            for (i in 0 until ta_val_ss_length) {
                val taValSS = localStorage.getItem(StorageKey.TA_VALUE_SaveState + i) ?: ""
                tempTaValSS.add(i, taValSS)
            }
            setta_val_ss(tempTaValSS)
        }

    }

    useEffect(checkState) {
        when (checkState) {
            ArchConst.STATE_EXECUTABLE -> {
                btnSwitchRef.current?.let {
                    it.classList.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
                }
            }

            else -> {
                btnSwitchRef.current?.let {
                    it.classList.add(StyleConst.CLASS_ANIM_DEACTIVATED)
                }
            }
        }
    }

    useEffect(currExeLine) {

    }

    useEffect(transcriptView) {
        btnSwitchRef.current?.let {
            if (transcriptView) {
                it.classList.add(StyleConst.CLASS_EDITOR_CONTROL_ACTIVE)
            } else {
                it.classList.remove(StyleConst.CLASS_EDITOR_CONTROL_ACTIVE)
            }
        }
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
    }

    useEffect(vc_rows) {
        /* SAVE vc_rows change */
        if (vc_rows != null) {
            for (vcRowID in vc_rows.indices) {
                localStorage.setItem(StorageKey.VC_ROW + vcRowID, vc_rows[vcRowID])
            }
            localStorage.setItem(StorageKey.VC_ROW_Length, vc_rows.size.toString())
        }
    }

    useEffect(ta_val) {
        /* SAVE ta_val change */
        ta_val?.let {
            localStorage.setItem(StorageKey.TA_VALUE, it)
            checkCode(it, false)
        }
        textareaRef.current?.let {
            it.value = ta_val ?: ""

        }
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
    }

    useEffect(ta_val_ss) {
        /* SAVE ta_val_ss */
        if (ta_val_ss != null) {
            for (taValSSID in ta_val_ss.indices) {
                localStorage.setItem(StorageKey.TA_VALUE_SaveState + "$taValSSID", ta_val_ss[taValSSID])
            }
            localStorage.setItem(StorageKey.TA_VALUE_SaveState_Length, ta_val_ss.size.toString())

        }
        updateUndoButton()
    }

    useEffect(darkMode) {
        val div = editorContainerRef.current
        div?.let {
            if (darkMode) {
                div.classList.add(StyleConst.CLASS_EDITOR_DARKMODE)
            } else {
                div.classList.remove(StyleConst.CLASS_EDITOR_DARKMODE)
            }

        }
    }


}

object Formatter {
    val reformats = listOf<ReFormat>(
        ReFormat(Regex("""(\s{2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", ")

    )
}

data class ReFormat(val regex: Regex, val replace: String)