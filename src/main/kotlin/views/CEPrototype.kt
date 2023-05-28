package views

import AppLogic
import StorageKey
import StyleConst
import csstype.*
import emotion.react.css
import kotlinx.browser.localStorage
import org.w3c.dom.*
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import react.dom.onChange
import kotlin.js.Date


external interface CEPrototypeProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}

class CodeLine(val id: Int, var content: String, var visible: Boolean)
class CursorPosition(var row: Int, var column: Int)


val CEPrototype = FC<CEPrototypeProps> { props ->

/* ----------------- STATIC VARIABLES ----------------- */

    val lineHeight = 21

    /* ----------------- TIMER VARIABLES ----------------- */

    var timer: Int = 0

    /* ----------------- REACT REFERENCES ----------------- */

    val btnClearRef = useRef<HTMLAnchorElement>()
    val btnUndoRef = useRef<HTMLAnchorElement>()

    /* ----------------- REACT STATES ----------------- */

    var data by useState(props.appLogic)
    var update = props.update

    /* ----------------- PERFORMANCE STATES ----------------- */

    var (visibleItems, setVisibleItems) = useState<MutableList<Element>>()

    /* ----------------- localStorage Sync Objects ----------------- */

    var (codeLines, setCodeLines) = useState<MutableList<CodeLine>>()
    var (cursorPos, setCursorPosition) = useState<CursorPosition>(CursorPosition(0, 0))

    var (vc_rows, setvc_rows) = useState<List<String>>()

    var (ta_val, setta_val) = useState<String>()
    var (ta_val_ss, setta_val_ss) = useState<MutableList<String>>()


    /* ----------------- UPDATE VISUAL COMPONENTS ----------------- */

    fun updateLineNumbers() {

    }

    fun updateTAResize() {

    }

    fun updateClearButton() {

    }

    /* ----------------- USEEFFECTS (Save and Reload from localStorage) ----------------- */

    useEffect(update) {
        /* Component RELOAD */

        /* -- LOAD from localStorage -- */
        /* codeLines */
        val codeLinesSize = localStorage.getItem(StorageKey.CODE_LINE_LENGTH)?.toInt() ?: 0
        val codeLinesTemp: MutableList<CodeLine> = mutableListOf()

        if (codeLinesSize >= 1) {
            for (codeLineID in 0 until codeLinesSize) {
                codeLinesTemp.add(
                    codeLineID,
                    CodeLine(codeLineID, localStorage.getItem(StorageKey.CODE_LINE + "$codeLineID") ?: "", true)
                )
            }
        } else {
            codeLinesTemp.add(0, CodeLine(0, "Assembly", true))
        }
        setCodeLines(codeLinesTemp)

        /* cursorPosition */
        val row = localStorage.getItem(StorageKey.CURSOR_POS_ROW)?.toInt() ?: 0
        val column = localStorage.getItem(StorageKey.CURSOR_POS_COLUMN)?.toInt() ?: 0
        setCursorPosition(CursorPosition(row, column))

        /* ta_val_ss */


    }

    useEffect(codeLines) {
        /* SAVE vc_rows change */
        if (codeLines != null) {
            for (codeLine in codeLines) {
                localStorage.setItem(StorageKey.CODE_LINE + "${codeLine.id}", codeLine.content)
            }
            localStorage.setItem(StorageKey.CODE_LINE_LENGTH, codeLines.size.toString())
        }


    }

    useEffect(cursorPos) {
        /* SAVE ta_val change */
        localStorage.setItem(StorageKey.CURSOR_POS_ROW, cursorPos.row.toString())
        localStorage.setItem(StorageKey.CURSOR_POS_COLUMN, cursorPos.column.toString())

    }

    useEffect(ta_val_ss) {
        /* SAVE ta_val_ss */

    }

    /* ----------------- EVENTS ----------------- */

    fun onContentChange(tachangedRows: Map<Int, String>) {

    }

    fun onLengthChange(taValue: String) {

    }

    /* ----------------- DOM ----------------- */
    ReactHTML.div {
        className = ClassName(StyleConst.CLASS_EDITOR)

        ReactHTML.div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTROLS)

            ReactHTML.a {
                id = "build"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ReactHTML.img {
                    src = "icons/cpu-charge.svg"
                }

            }

            ReactHTML.a {
                id = "undo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ref = btnUndoRef

                ReactHTML.img {
                    src = "icons/undo.svg"
                }

                onClick = {

                }
            }

            ReactHTML.a {
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ReactHTML.img {
                    src = "icons/exclamation-mark2.svg"
                }
            }

            ReactHTML.a {
                id = "editor-clear"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ref = btnClearRef
                ReactHTML.img {
                    src = "icons/clear.svg"
                }

                onClick = {

                }
            }
        }

        ReactHTML.div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTAINER)


            ReactHTML.div {
                className = ClassName(StyleConst.CLASS_CE_CODESCROLLDIV)

                onScroll = { event ->
                    val containerRect = event.currentTarget.getBoundingClientRect()
                    val children = event.currentTarget.children
                    val tempVisibleItems: MutableList<Element> = mutableListOf()

                    for (childID in 0 until children.length) {
                        val childRect = children.get(childID)?.getBoundingClientRect()
                        childRect?.let {
                            if (it.top >= containerRect.top && it.bottom <= containerRect.bottom) {
                                if (children[childID] != null) {
                                    children[childID]?.let { it1 -> tempVisibleItems.add(it1) }
                                }
                            }
                        }
                    }
                    setVisibleItems(tempVisibleItems)
                }

                /* SINGLE LINE RENDERING (EACH LINE SHOULD BE EDITABLE AND ONLY THE LINES SHOULD BE RENDERED WHICH ARE VISIBLE) */
                div {
                    className = ClassName(StyleConst.CLASS_CE_CODEDIV)

                    css {
                        display = Display.block
                        position = Position.relative
                        width = 100.pc

                    }

                    onKeyDown = { event ->
                        if (event.key == "Enter") {

                        }
                    }

                    if (codeLines != null) {
                        for (codeLine in codeLines) {
                            if (codeLine.visible) {
                                div {
                                    className = ClassName(StyleConst.CLASS_CE_CODELINE)
                                    key = "line ${codeLine.id}"

                                    css {
                                        position = Position.relative
                                        display = Display.block

                                        width = 100.pc
                                        height = 21.px
                                    }

                                    a {

                                        css {
                                            display = Display.block
                                            position = Position.absolute
                                            float = Float.left
                                            width = 30.px
                                            height = 100.pc
                                        }
                                        +" ${codeLine.id} "

                                    }
                                    div {

                                        css {
                                            position = Position.relative
                                            display = Display.block
                                            float = Float.left
                                            marginLeft = 30.px
                                            height = 21.px
                                            width = 100.pc
                                        }


                                        textarea {
                                            css {

                                            }
                                            contentEditable = true
                                            value = codeLine.content

                                            onChange = { event ->
                                                val tempCodeLines = codeLines
                                                tempCodeLines[codeLine.id].content = event.currentTarget.innerText
                                                setCodeLines(tempCodeLines)


                                            }

                                            onKeyDown = { event ->

                                                when (event.key) {
                                                    "Enter" -> {
                                                        event.preventDefault()

                                                    }


                                                }


                                            }

                                        }

                                        div {
                                            id = "lineVc${codeLine.id}"


                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}















