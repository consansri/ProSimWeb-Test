package views

import AppLogic
import emotion.react.css
import extendable.ArchConst
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.ul
import web.cssom.*


external interface FooterViewProps : Props{

}

val FooterView = FC<FooterViewProps>{

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
                }

                onClick = {event ->


                }

                src = "icons/system-outline-21-bug.gif"
            }
        }
        h3{
            className = ClassName(StyleConst.CLASS_LOGO)
            +ArchConst.PROSIMNAME
        }
        p{
            +"Copyright © 2023 Universität Stuttgart IKR"
        }


    }


}