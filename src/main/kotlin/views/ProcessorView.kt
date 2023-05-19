package views

import AppData
import csstype.ClassName
import csstype.Display
import org.w3c.dom.HTMLAnchorElement
import react.*
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div

external interface ProcessorViewProps : Props {
    var appData: AppData
    var updateParent: (newData: AppData) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: Boolean
}

// CSS CLASSES
val CLASS_PROC_MAIN_CONTAINER = "processor"


val ProcessorView = FC<ProcessorViewProps> { props ->

    val data by useState(props.appData)
    val (change, setChange) = useState(props.update)

    val titleRef = useRef<HTMLAnchorElement>()

    useEffect(change) {
        console.log("ProcessorView updated")
    }



    div {
        className = ClassName("exeControlDiv")

        div {
            a {
                ref = titleRef
                +data.getArch().name
            }
        }
    }

    div {
        className = ClassName("processorDiv")

//    ReactHTML.button {
//        +"refreshParentExample"
//
//        onClick = {
//            data.testBoolean = !data.testBoolean
//            props.updateParent(data)
//            setChange(!change)
//        }
//    }

    }

    div {
        className = ClassName("memoryDiv")

    }


}