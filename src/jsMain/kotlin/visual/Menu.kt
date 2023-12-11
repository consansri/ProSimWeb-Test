package visual

import StyleAttr
import emotion.react.css
import emulator.Link
import emulator.kit.common.FileBuilder
import emulator.kit.common.FileHandler
import js.core.asList

import react.*
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
import debug.DebugTools
import emulator.kit.Architecture
import web.buffer.Blob
import web.cssom.*
import web.storage.localStorage
import web.timers.*
import web.html.*
import web.file.*
import web.dom.*
import web.location.location
import web.url.URL

external interface MenuProps : Props {
    var archState: StateInstance<Architecture>
    var fileChangeEvent: StateInstance<Boolean>
}

val Menu = FC<MenuProps>() { props ->

    val (arch, setArch) = props.archState
    val (navHidden, setNavHidden) = useState(true)
    val (archsHidden, setArchsHidden) = useState(true)

    val (importHidden, setImportHidden) = useState(true)

    val (exportHidden, setExportHidden) = useState(true)
    val (selFormat, setSelFormat) = useState<FileBuilder.ExportFormat>(FileBuilder.ExportFormat.entries.first())

    val (selAddrW, setSelAddrW) = useState<Int>(arch.getMemory().getAddressSize().bitWidth)
    val (selDataW, setSelDataW) = useState<Int>(arch.getMemory().getWordSize().bitWidth)

    val navRef = useRef<HTMLElement>()
    val archsRef = useRef<HTMLDivElement>()
    val importRef = useRef<HTMLInputElement>()

    val downloadAsyncRef = useRef<Timeout>()

    fun showNavbar(state: Boolean) {
        navRef.current?.let {
            if (state) {
                it.classList.add(StyleAttr.Header.CLASS_MOBILE_OPEN)
            } else {
                it.classList.remove(StyleAttr.Header.CLASS_MOBILE_OPEN)
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
            arch.getFileHandler().import(FileHandler.File(file.name as String, reader.result as String))
            props.fileChangeEvent.component2().invoke(!props.fileChangeEvent.component1())
        }
    }

    header {
        css {
            backgroundColor = StyleAttr.Header.BgColor.get()
            color = StyleAttr.Header.FgColor.get()

            img {
                filter = StyleAttr.Header.IconFilter.get()
            }
            a {
                color = StyleAttr.Header.FgColor.get()
            }
            nav {
                backgroundColor = StyleAttr.Header.BgColor.get()
            }
        }

        h3 {
            +Constants.name
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
                    src = StyleAttr.Icons.home
                }
            }

            a {
                href = "#"
                onClick = {
                    showArchs(true)
                }
                img {
                    className = ClassName("nav-img")
                    src = StyleAttr.Icons.processor
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Upload"
                    src = StyleAttr.Icons.import
                }

                onClick = {
                    setImportHidden(!importHidden)
                    setExportHidden(true)
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Download"
                    src = StyleAttr.Icons.export
                }

                onClick = {
                    setExportHidden(!exportHidden)
                    setImportHidden(true)
                }
            }

            button {
                className = ClassName("nav-btn nav-close-btn")
                title = "close nav"
                onClick = {
                    showNavbar(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = StyleAttr.Icons.cancel

                }
            }
        }

        button {
            className = ClassName("nav-btn")
            title = "open nav"

            onClick = {
                showNavbar(true)
            }

            img {
                className = ClassName("nav-img")
                src = StyleAttr.Icons.bars

            }
        }

        div {
            ref = archsRef
            css(ClassName(StyleAttr.Header.CLASS_DROPDOWN)) {
                if (archsHidden) {
                    visibility = Visibility.hidden
                    transform = translatey(-100.vh)
                } else {
                    visibility = Visibility.visible
                    transform = translatey(0.vh)
                }
            }

            for (archLink in Link.entries) {
                a {
                    css {
                        color = important(StyleAttr.Header.FgColorSec.get())
                    }
                    onClick = { event ->
                        showArchs(false)
                        setArch(archLink.architecture)
                        localStorage.setItem(StorageKey.ARCH_TYPE, Link.entries.indexOf(archLink).toString())

                        event.currentTarget.classList.toggle("nav-arch-active")
                    }

                    +archLink.architecture.getDescription().fullName
                }
            }

            a {
                onClick = {
                    showArchs(false)
                }

                img {
                    css {
                        filter = important(invert(100.pct))
                    }
                    src = StyleAttr.Icons.cancel
                }
            }
        }

        if (!exportHidden) {
            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

                img {
                    src = StyleAttr.Icons.cancel
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
                            +format.uiName
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

                if (selFormat != FileBuilder.ExportFormat.CURRENT_FILE) {
                    div {
                        className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
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
                        className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
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
                }

                a {
                    css {
                        color = StyleAttr.Header.FgColorSec.get()
                    }
                    +"Export"

                    onClick = {
                        downloadAsyncRef.current?.let {
                            clearInterval(it)
                        }

                        downloadAsyncRef.current = setTimeout({
                            val blob = arch.getFormattedFile(selFormat, FileBuilder.Setting.DataWidth(selDataW), FileBuilder.Setting.AddressWidth(selAddrW))
                            val anchor = document.createElement("a") as HTMLAnchorElement
                            anchor.href = URL.createObjectURL(blob)
                            anchor.style.display = "none"
                            document.body.appendChild(anchor)
                            if (selFormat.ending.isNotEmpty()) {
                                anchor.download = arch.getFileHandler().getCurrNameWithoutType() + selFormat.ending
                            } else {
                                anchor.download = arch.getFileHandler().getCurrent().getName()
                            }
                            anchor.click()
                        }, 10)

                        setTimeout({
                            downloadAsyncRef.current?.let {
                                clearInterval(it)
                                console.warn("Download File Generation took to long!")
                            }
                        }, 3000)

                        setExportHidden(true)
                    }
                }

            }
        }

        if (!importHidden) {
            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

                img {
                    src = StyleAttr.Icons.cancel

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
                    css {
                        color = StyleAttr.Header.FgColorSec.get()
                    }
                    +"Import"
                    onClick = {
                        val files = importRef.current?.files?.asList() ?: emptyList<File>()

                        if (!files.isEmpty()) {
                            for (file in files) {
                                importFile(file)
                            }
                        }
                        setImportHidden(true)
                    }
                }

            }
        }
    }


}