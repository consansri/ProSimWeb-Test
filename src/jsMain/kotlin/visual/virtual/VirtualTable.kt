package visual.virtual

import StyleAttr
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useRef
import react.useState
import web.cssom.*
import web.html.HTMLDivElement

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

    val handleScroll = { deltaY: Double ->
        if (deltaY > 0) {
            // Scroll Dow
            if (startIndex < props.rowCount - props.visibleRows - 1) {
                setStartIndex(startIndex + 1)
            }
        } else {
            // Scroll Up
            if (startIndex > 0) {
                setStartIndex(startIndex - 1)
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

            onWheelCapture = {
                //it.preventDefault()
                val delta = it.deltaY
                handleScroll(delta)
            }

            // Header
            repeat(props.colCount) { colID ->
                // Content Cell
                div {
                    css{
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
            this.startIndex = startIndex
            this.visibleRows = props.visibleRows
            this.rowCount = props.rowCount
            this.onScroll = {
                if (it >= props.rowCount - props.visibleRows) {
                    setStartIndex(props.rowCount - props.visibleRows)
                } else if (it < 0) {
                    setStartIndex(0)
                } else {
                    setStartIndex(it)
                }
            }
        }
    }
}


