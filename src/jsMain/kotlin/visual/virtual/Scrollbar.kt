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

external interface ScrollbarProps : Props {
    var range: IntRange
    var value: Int
    var onScroll: (newValue: Int) -> Unit
}

val Scrollbar = FC<ScrollbarProps> { props ->

    val scrollContainerRef = useRef<HTMLDivElement>()

    val (drag, setDrag) = useState<Boolean>(false)

    val step = 1.0 / props.range.count()
    val stepInPCT = (step * 100).pct

    // Function to calculate the height of the draggable scrollbar element
    fun calculateScrollbarHeight(): Length {
        val totalHeight = scrollContainerRef.current?.clientHeight ?: 0
        val height = (totalHeight.toDouble() / props.range.count()).pct
        return height
    }

    div {
        ref = scrollContainerRef
        css {
            display = Display.block
            position = Position.relative
            width = 24.px
            overflowY = Overflow.hidden
            background = StyleAttr.Main.Processor.ScrollBarColor.get()
            borderRadius = StyleAttr.insideBorderRadius
        }

        onClick = {
            val posY = it.clientY - it.currentTarget.getBoundingClientRect().top
            val percentage = posY / it.currentTarget.clientHeight
            val index = props.range.first + (percentage * props.range.count()).toInt()
            props.onScroll(index)
        }

        onMouseDown = {
            setDrag(true)
        }

        onMouseMoveCapture = {
            if (drag) {
                val posY = it.clientY - it.currentTarget.getBoundingClientRect().top
                val percentage = posY / it.currentTarget.clientHeight
                val index = props.range.first + (percentage * props.range.count()).toInt()
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
                top = (props.value - props.range.first) * stepInPCT
                right = 0.px
                width = 24.px // Adjust the width as needed
                height = calculateScrollbarHeight()
                backgroundColor = StyleAttr.Main.Processor.TableFgColor.get()
                cursor = Cursor.pointer
            }

            title = "top: ${(props.value - props.range.first) * stepInPCT}"

            /*onDrag = {
                // Drag logic
                it.preventDefault()
                handleScrollbarDrag(it.movementY)
            }*/
        }
    }

}