package visual.virtual

import StyleAttr
import emotion.react.css
import emulator.kit.nativeLog
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useRef
import react.useState
import web.cssom.*
import web.html.HTMLDivElement
import kotlin.math.roundToInt

external interface ScrollbarProps : Props {
    var visibleRows: Int
    var rowCount: Int
    var startIndex: Int
    var onScroll: (index: Int) -> Unit
}

val Scrollbar = FC<ScrollbarProps> { props ->

    val scrollContainerRef = useRef<HTMLDivElement>()

    val (drag, setDrag) = useState<Boolean>(false)

    val maxScroll = props.rowCount - props.visibleRows
    val step = 1.0 / props.rowCount

    // Function to calculate the height of the draggable scrollbar element
    fun calculateScrollbarHeight(): Length {
        val totalHeight = scrollContainerRef.current?.clientHeight ?: 0
        return (totalHeight.toDouble() / props.rowCount * props.visibleRows).pct
    }

    // Function to handle scrollbar drag
    fun handleScrollbarDrag(deltaY: Double) {
        val newScrollPosition = (props.startIndex + deltaY).coerceIn(0.0, maxScroll.toDouble()).toInt()
        props.onScroll(newScrollPosition)
    }

    div {
        ref = scrollContainerRef
        css {
            display = Display.block
            position = Position.relative
            backgroundColor = StyleAttr.Main.BgColor.get()
            width = 12.px
            overflowY = Overflow.hidden
        }

        onClick = {
            val posY = it.clientY - it.currentTarget.getBoundingClientRect().top
            val percentage = posY / it.currentTarget.clientHeight
            val index = when{
                percentage < 0.01 -> 0
                percentage > 0.99 -> maxScroll
                else -> (maxScroll * percentage).roundToInt()
            }
            nativeLog("Drag: $index -> ${percentage}%")
            props.onScroll(index)
        }

        onMouseDown = {
            setDrag(true)
        }

        onMouseMoveCapture = {
            if (drag) {
                val posY = it.clientY - it.currentTarget.getBoundingClientRect().top
                val percentage = posY / it.currentTarget.clientHeight
                val index = when{
                    percentage < 0.01 -> 0
                    percentage > 0.99 -> maxScroll
                    else -> (maxScroll * percentage).roundToInt()
                }
                nativeLog("Drag: $index -> ${percentage}%")
                props.onScroll(index)
            }
        }

        onMouseLeave = {
            setDrag(false)
        }

        onMouseUpCapture = {
            setDrag(false)
        }

        // Draggable scrollbar element
        div {
            css {
                position = Position.absolute
                top = (step * props.startIndex * 100).pct
                right = 0.px
                width = 12.px // Adjust the width as needed
                height = calculateScrollbarHeight()
                backgroundColor = Color("lightgrey")
                cursor = Cursor.pointer
            }

            /*onDrag = {
                // Drag logic
                it.preventDefault()
                handleScrollbarDrag(it.movementY)
            }*/
        }
    }

}