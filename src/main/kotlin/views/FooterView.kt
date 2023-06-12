package views

import AppLogic
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.ul

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

        ul{
            css {
                listStyle = null
            }

            li{

            }
            li{

            }
            li{

            }
        }
        ul{
            css {
                listStyleType = null
            }
            li{

            }
            li{

            }
            li{

            }
        }
        h3{
            +"ProSimWeb"
        }
        p{
            +"Copyright &copy; 2023 Universität Stuttgart IKR"
        }


    }


}