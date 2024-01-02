package visual

import Constants
import StyleAttr
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.p
import web.cssom.*


external interface FooterViewProps : Props

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
            a{
                href = "mailto:constantin.birkert@web.de?subject=ProSimWeb: BugReport"
                img{
                    css {
                        width = 4.rem
                        height = 4.rem
                        cursor = Cursor.pointer
                        filter = invert(90.pct)
                    }

                    src = StyleAttr.Icons.report_bug
                }
            }
        }
        h3{
            className = ClassName(StyleAttr.CLASS_LOGO)
            +Constants.NAME
        }
        p{
            +"${Constants.NAME} version ${Constants.VERSION}"
        }

        p{
            +"Copyright © ${Constants.YEAR} ${Constants.ORG}"
        }
        p{
            +"Developed by ${Constants.DEV}"
        }
    }

}