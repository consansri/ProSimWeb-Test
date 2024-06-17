package visual.virtual

import JSTools
import StyleAttr
import emotion.react.css
import emulator.kit.nativeLog
import react.*
import react.dom.html.ReactHTML.div
import web.cssom.*
import web.dom.document
import web.html.HTMLDivElement
import web.timers.Timeout

external interface VirtualTableProps : Props {
    var headers: Array<String>
    var visibleRows: Int
    var rowCount: Int
    var colCount: Int
    var rowContent: (rowIndex: Int) -> Array<Pair<String, Color?>>
}

val VirtualTable = FC<VirtualTableProps> { props ->

    val containerRef = useRef<HTMLDivElement>()
    val (startIndex, setStartIndex) = useState<Int>(0)
    val wheelTimeout = useRef<Timeout>()

    val scrollRange: IntRange = IntRange(0, (props.rowCount / props.visibleRows) - 1)

    val handleScroll = { deltaY: Double ->
        if (deltaY > 0) {
            // Scroll Dow
            if (startIndex + props.visibleRows < props.rowCount) {
                setStartIndex(startIndex + props.visibleRows)
            }
        } else {
            // Scroll Up
            if (startIndex - props.visibleRows >= 0) {
                setStartIndex(startIndex - props.visibleRows)
            }
        }
    }

    div {
        css {
            width = 100.pct
            height = 100.pct
            display = Display.flex
            position = Position.relative
            gap = StyleAttr.paddingSize
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.stretch
            alignItems = AlignItems.stretch
            padding = StyleAttr.paddingSize

            overflow = Overflow.hidden
            background = StyleAttr.Main.Processor.TableBgColor.get()
            borderRadius = StyleAttr.borderRadius
        }


        autoFocus = true

        div {
            ref = containerRef
            css {
                width = 100.pct
                height = 100.pct
                flexGrow = number(1.0)
                overflowX = Overflow.scroll
                overflowY = Overflow.hidden
                display = Display.grid

                gridTemplateColumns = repeat(props.colCount, 1.fr)
                gridTemplateRows = repeat(props.visibleRows + 1, 1.fr)

                div {
                    textAlign = TextAlign.center

                }
            }

            onMouseEnter = {
                val root = document.getElementById("root")
                root?.let {
                    JSTools.pauseScroll(it)
                }
            }

            onMouseLeave = {
                val root = document.getElementById("root")
                root?.let {
                    JSTools.unpauseScroll(it)
                }
            }

            onWheel = {
                it.preventDefault()
                it.stopPropagation()
                val delta = it.deltaY
                handleScroll(delta)
                nativeLog("$it, ${it.currentTarget}")
            }

            // Header
            repeat(props.colCount) { colID ->
                // Content Cell
                div {
                    css {
                        fontWeight = FontWeight.bold
                    }
                    key = "header-${colID}"
                    +props.headers.getOrNull(colID)
                }
            }

            // Content
            repeat(props.visibleRows) { rowID ->
                // Content Row

                props.rowContent(startIndex + rowID).forEachIndexed { i, value ->
                    div {
                        value.second?.let {
                            css {
                                color = important(it)
                            }
                        }
                        key = "col-${rowID}-${i}"
                        +value.first
                    }
                }
            }
        }

        Scrollbar {
            this.value = startIndex / props.visibleRows
            this.range = scrollRange
            this.onScroll = {
                setStartIndex(it * props.visibleRows)
            }
        }
    }

    useEffect {



    }
}


