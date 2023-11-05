package visual

import Constants
import debug.DebugTools
import emotion.react.css
import emulator.kit.Settings
import react.FC
import react.Props
import react.StateInstance
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.p
import react.useEffect
import web.cssom.*


external interface FooterViewProps : Props{

}

val FooterView = FC<FooterViewProps>{ props ->

    div{
        css {
            backgroundColor = Color("#905356")
            textAlign = TextAlign.center
            color = Color("#FFFFFF")
            padding = 1.0.rem
            textDecoration = null
        }

        p{
            img{
                css {
                    width = 4.rem
                    height = 4.rem
                    cursor = Cursor.pointer
                    filter = invert(90.pct)
                }

                onClick = {event ->

                }

                src = StyleAttr.Icons.report_bug
            }
        }
        h3{
            className = ClassName(StyleAttr.CLASS_LOGO)
            +Constants.name
        }
        p{
            +"${Constants.name} version ${Constants.version}"
        }
        p{
            +"Copyright © ${Constants.year} ${Constants.org}"
        }
    }

}