package views

import AppLogic
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.ul

external interface FooterViewProps : Props{

}

val FooterView = FC<FooterViewProps>{

    div{

        ul{
            li{

            }
            li{

            }
            li{

            }
        }
        ul{
            li{

            }
            li{

            }
            li{

            }
        }
        p{
            +"Copyright &copy; 2023 Universität Stuttgart IKR"
        }


    }


}