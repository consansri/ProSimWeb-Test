package views.components

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.span
import react.useState

external interface TooltipProps : Props {
    val text: String
}

val Tooltip = FC<TooltipProps> { props ->

    val text by useState(props.text)

    span {

    }

}