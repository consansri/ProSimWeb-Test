package visual.virtual

import StyleAttr
import emotion.react.css
import kotlinx.browser.document
import org.w3c.dom.events.Event
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useRef
import react.useState
import web.cssom.*
import web.html.HTMLDivElement
import web.timers.Timeout

external interface VirtualTableProps : Props {
    var headers: Array<String>
    var visibleRows: Int
    var rowCount: Int
    var colCount: Int
    var cellContent: (rowIndex: Int) -> Array<String>
}

val VirtualTable = FC<VirtualTableProps> { props ->

    val containerRef = useRef<HTMLDivElement>()
    val (startIndex, setStartIndex) = useState<Int>(0)
    val wheelTimeout = useRef<Timeout>()

    val scrollRange: IntRange = IntRange(0, (props.rowCount / props.visibleRows) - 1)

    val handleScroll = { deltaY: Double ->
        if (deltaY > 0) {
            // Scroll Dow
            if (startIndex + props.visibleRows < props.rowCount - props.visibleRows) {
                setStartIndex(startIndex + props.visibleRows)
            }
        } else {
            // Scroll Up
            if (startIndex - props.visibleRows >= 0) {
                setStartIndex(startIndex - props.visibleRows)
            }
        }
    }

    val preventWheel = { e: Event ->
        e.preventDefault()
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

            onScrollCapture = {
                it.preventDefault()
            }

            onMouseEnter = {
                document.body?.addEventListener("onscroll", preventWheel)
            }

            onMouseLeave = {
                document.body?.removeEventListener("onscroll", preventWheel)
            }

            onWheelCapture = {
                //it.preventDefault()
                val delta = it.deltaY
                handleScroll(delta)
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

                props.cellContent(startIndex + rowID).forEachIndexed { i, value ->
                    div {
                        key = "col-${rowID}-${i}"
                        +value
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
}


