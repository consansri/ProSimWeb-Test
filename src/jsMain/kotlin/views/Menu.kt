package views

import AppLogic
import StyleConst
import csstype.*
import emotion.react.css
import extendable.components.connected.FileBuilder
import extendable.components.connected.FileHandler
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.File
import org.w3c.files.FileReader
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import tools.DebugTools


external interface MenuProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit
}

val Menu = FC<MenuProps>() { props ->

    val data by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val (navHidden, setNavHidden) = useState(true)
    val (archsHidden, setArchsHidden) = useState(true)

    val (importHidden, setImportHidden) = useState(true)

    val (exportHidden, setExportHidden) = useState(true)
    val (selFormat, setSelFormat) = useState<FileBuilder.ExportFormat>(FileBuilder.ExportFormat.entries.first())

    val (selAddrW, setSelAddrW) = useState<Int>(8)
    val (selDataW, setSelDataW) = useState<Int>(256)

    val navRef = useRef<HTMLElement>()
    val archsRef = useRef<HTMLDivElement>()
    val importRef = useRef<HTMLInputElement>()


    fun showNavbar(state: Boolean) {
        navRef.current?.let {
            if (state) {
                it.classList.add("responsive_nav")
            } else {
                it.classList.remove("responsive_nav")
            }
            setNavHidden(!state)
        }
    }

    fun showArchs(state: Boolean) {
        archsRef.current?.let {
            if (state) {
                it.classList.add("nav-dropdown-open")
            } else {
                it.classList.remove("nav-dropdown-open")
            }
            setArchsHidden(!state)
        }
    }

    fun importFile(file: dynamic) {
        val reader = FileReader()
        reader.readAsText(file as Blob, "UTF-8")

        reader.onloadend = {
            console.log("read ${reader.result}")
            data.getArch().getFileHandler().import(FileHandler.File(file.name as String, reader.result as String))
        }
    }

    header {
        h3 {
            +"ProSimWeb"
        }

        nav {
            ref = navRef
            a {
                href = "#home"
                onClick = {
                    console.log("#home clicked")
                }

                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.home

                }
            }

            a {
                href = "#"
                onClick = {
                    showArchs(true)
                }
                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.processor
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Upload"
                    src = StyleConst.Icons.import

                }

                onClick = {
                    setImportHidden(!importHidden)
                }


            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Download"
                    src = StyleConst.Icons.export
                }

                onClick = {
                    setExportHidden(!exportHidden)
                }
            }

            button {
                className = ClassName("nav-btn nav-close-btn")

                onClick = {
                    showNavbar(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"

                }
            }
        }

        button {

            className = ClassName("nav-btn")

            onClick = {
                showNavbar(true)
            }

            img {
                className = ClassName("nav-img")
                src = "icons/bars.svg"

            }
        }

        div {
            className = ClassName("nav-dropdown")
            ref = archsRef

            for (id in data.getArchList().indices) {
                a {
                    href = "#${data.getArchList()[id].getName()}"

                    onClick = { event ->
                        showArchs(false)
                        val newData = data
                        newData.selID = id
                        localStorage.setItem(StorageKey.ARCH_TYPE, "$id")
                        console.log("Load " + data.getArch().getName())
                        event.currentTarget.classList.toggle("nav-arch-active")
                        //updateParent(newData)
                        document.location?.reload()
                    }

                    +data.getArchList()[id].getName()
                }
            }

            a {
                onClick = {
                    showArchs(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"

                }
            }
        }

        if (!exportHidden) {
            div {
                css {
                    position = Position.fixed
                    bottom = 0.px
                    left = 0.px
                    width = 100.vw
                    zIndex = integer(1000)
                    padding = 1.rem
                    backgroundColor = Color("#5767aa")

                    display = Display.flex
                    justifyContent = JustifyContent.center
                    gap = 2.rem
                    alignItems = AlignItems.center
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/cancel.svg"
                    }
                    onClick = {
                        setExportHidden(true)
                    }
                }

                select {

                    defaultValue = selFormat.name

                    option {
                        disabled = true
                        value = ""
                        +"Choose Export Format"
                    }

                    for (format in FileBuilder.ExportFormat.entries) {
                        option {
                            value = format.name
                            +format.name
                        }
                    }

                    onChange = {
                        for (format in FileBuilder.ExportFormat.entries) {
                            if (format.name == it.currentTarget.value) {
                                setSelFormat(format)
                                break
                            }
                        }
                    }
                }

                when (selFormat) {
                    FileBuilder.ExportFormat.VHDL -> {
                        div {

                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }

                            label {
                                htmlFor = "vhdlAddrInput"
                                +"Address Width [Bits]"
                            }

                            input {
                                id = "vhdlAddrInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selAddrW.toString()

                                onChange = {
                                    setSelAddrW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        div {

                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }
                            label {
                                htmlFor = "vhdlDataInput"
                                +"Data Width [Bits]"
                            }

                            input {
                                id = "vhdlDataInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selDataW.toString()

                                onChange = {
                                    setSelDataW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        a {
                            img {
                                className = ClassName("nav-img")
                                src = "icons/download.svg"
                            }

                            onClick = {
                                val blob = data.getArch().getFormattedFile(selFormat, FileBuilder.Setting.DataWidth(selDataW), FileBuilder.Setting.AddressWidth(selAddrW))
                                val anchor = document.createElement("a") as HTMLAnchorElement
                                document.body?.appendChild(anchor)
                                anchor.style.display = "none"
                                anchor.href = URL.createObjectURL(blob)
                                anchor.download = data.getArch().getFileHandler().getCurrNameWithoutType() + selFormat.ending
                                anchor.click()

                                setExportHidden(true)
                            }
                        }
                    }

                    FileBuilder.ExportFormat.MIF -> {
                        div {

                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }

                            label {
                                htmlFor = "vhdlAddrInput"
                                +"Address Width [Bits]"
                            }

                            input {
                                id = "vhdlAddrInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selAddrW.toString()

                                onChange = {
                                    setSelAddrW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        div {

                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }
                            label {
                                htmlFor = "vhdlDataInput"
                                +"Data Width [Bits]"
                            }

                            input {
                                id = "vhdlDataInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selDataW.toString()

                                onChange = {
                                    setSelDataW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        a {
                            img {
                                className = ClassName("nav-img")
                                src = "icons/download.svg"
                            }

                            onClick = {
                                val blob = data.getArch().getFormattedFile(selFormat,  FileBuilder.Setting.DataWidth(selDataW), FileBuilder.Setting.AddressWidth(selAddrW))

                                val anchor = document.createElement("a") as HTMLAnchorElement
                                document.body?.appendChild(anchor)
                                anchor.style.display = "none"
                                anchor.href = URL.createObjectURL(blob)
                                anchor.download = data.getArch().getFileHandler().getCurrNameWithoutType() + selFormat.ending
                                anchor.click()

                                setExportHidden(true)
                            }
                        }
                    }

                    FileBuilder.ExportFormat.HEXDUMP -> {
                        div {
                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }

                            label {
                                htmlFor = "vhdlAddrInput"
                                +"Address Width [Bits]"
                            }

                            input {
                                id = "vhdlAddrInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selAddrW.toString()

                                onChange = {
                                    setSelAddrW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        div {

                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                justifyContent = JustifyContent.center
                                alignItems = AlignItems.center
                            }
                            label {
                                htmlFor = "vhdlDataInput"
                                +"Data Width [Bits]"
                            }

                            input {
                                id = "vhdlDataInput"
                                type = InputType.number
                                min = 1.0
                                max = 2048.0
                                defaultValue = selDataW.toString()

                                onChange = {
                                    setSelDataW(it.currentTarget.valueAsNumber.toInt())
                                }
                            }
                        }

                        a {
                            img {
                                className = ClassName("nav-img")
                                src = "icons/download.svg"
                            }

                            onClick = {
                                val blob = data.getArch().getFormattedFile(selFormat, FileBuilder.Setting.DataWidth(selDataW), FileBuilder.Setting.AddressWidth(selAddrW))
                                val anchor = document.createElement("a") as HTMLAnchorElement
                                document.body?.appendChild(anchor)
                                anchor.style.display = "none"
                                anchor.href = URL.createObjectURL(blob)
                                anchor.download = data.getArch().getFileHandler().getCurrNameWithoutType() + selFormat.ending
                                anchor.click()

                                setExportHidden(true)
                            }
                        }
                    }
                }

            }
        }


        if (!importHidden) {
            div {
                css {
                    position = Position.fixed
                    bottom = 0.px
                    left = 0.px
                    width = 100.vw
                    zIndex = integer(1000)
                    padding = 1.rem
                    backgroundColor = Color("#5767aa")

                    display = Display.flex
                    justifyContent = JustifyContent.center
                    gap = 2.rem
                    alignContent = AlignContent.spaceEvenly
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/cancel.svg"
                    }
                    onClick = {
                        setImportHidden(true)
                    }
                }

                input {
                    ref = importRef
                    type = InputType.file
                    multiple = true
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/upload.svg"
                    }
                    onClick = {
                        val files = importRef.current?.files?.asList() ?: emptyList<File>()

                        if (!files.isEmpty()) {
                            for (file in files) {
                                importFile(file)
                            }
                        }
                        props.updateParent()
                        setImportHidden(true)
                    }
                }

            }
        }
    }

    useEffect(selFormat, selAddrW, selDataW) {
        // generate downloadable file

    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) Menu")
        }
    }

}