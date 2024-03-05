package visual

import StyleAttr
import emotion.react.css
import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.common.Docs
import emulator.kit.common.FileHandler
import emulator.kit.common.Memory
import react.FC
import react.Props
import react.StateInstance
import react.dom.html.ReactHTML
import web.cssom.Color
import web.cssom.TextAlign
import web.window.window

object StyleExt {

    fun Memory.InstanceType.get(mode: StyleAttr.Mode): Color {
        return when (mode) {
            StyleAttr.Mode.LIGHT -> Color("#${this.light.toString(16)}")
            StyleAttr.Mode.DARK -> Color("#${this.dark?.toString(16) ?: this.light.toString(16)}")
        }
    }

    fun Compiler.CodeStyle.get(mode: StyleAttr.Mode): Color {
        return when (mode) {
            StyleAttr.Mode.LIGHT -> Color("#${this.lightHexColor.toString(16)}")
            StyleAttr.Mode.DARK -> Color("#${this.darkHexColor?.toString(16) ?: this.lightHexColor.toString(16)}")
        }
    }

    fun List<Compiler.Token>.getVCRows(): List<String> {
        return this.joinToString("") {
            val severity = it.getSeverity()?.type
            val codeStyle = it.getCodeStyle()
            if (severity != null) {
                if (codeStyle == null) highlight(it.content, it.id, severity.name) else highlight(it.content, it.id, severity.name, codeStyle.name)
            } else {
                if (codeStyle == null) it.content else highlight(it.content, it.id, codeStyle.name)
            }
        }.split("\n")
    }

    /**
     * Tool
     * for surrounding a input with a certain highlighting html tag
     */
    fun highlight(input: String, id: Int? = null, vararg classes: String): String {
        val tag = "span"
        return "<$tag class='${classes.joinToString(" ") { it }}' ${id?.let { "id='$id'" }}>$input</$tag>"
    }

    fun Docs.DocComponent.render(arch: Architecture, fileChangeEvent: StateInstance<Boolean>): FC<Props> {
        val component = this
        return  FC {
            when (component) {
                is Docs.DocComponent.Code -> {
                    ReactHTML.pre {
                        ReactHTML.code {
                            +component.content

                            onClick = {event ->
                                if (arch.getFileHandler().getAllFiles().none { it.getName() == "example" }) {
                                    arch.getFileHandler().import(FileHandler.File("example", component.content))
                                    window.scrollTo(0, 0)
                                    arch.getConsole().info("Successfully imported 'example'!")
                                    fileChangeEvent.component2().invoke(!fileChangeEvent.component1())
                                } else {
                                    val currentTarget = event.currentTarget
                                    currentTarget.classList.add(StyleAttr.ANIM_SHAKERED)
                                    web.timers.setTimeout({
                                        currentTarget.classList.remove(StyleAttr.ANIM_SHAKERED)
                                    }, 100)
                                    arch.getConsole().warn("Documentation couldn't import code example cause filename 'example' already exists!")
                                }
                            }
                        }
                    }
                }

                is Docs.DocComponent.Chapter -> {
                    ReactHTML.h2 {
                        +component.chapterTitle
                    }
                    component.chapterContent.forEach {
                        val fc = it.render(arch, fileChangeEvent)
                        fc {

                        }
                    }
                }

                is Docs.DocComponent.Table -> {
                    ReactHTML.table {
                        ReactHTML.thead {
                            ReactHTML.tr {
                                component.header.forEach { headString ->
                                    ReactHTML.th {
                                        +headString
                                    }
                                }
                            }
                        }
                        ReactHTML.tbody {
                            component.contentRows.forEach { row ->
                                ReactHTML.tr {
                                    row.forEach { rowEntry ->
                                        ReactHTML.td {
                                            css {
                                                textAlign = TextAlign.left
                                            }
                                            val fc = rowEntry.render(arch, fileChangeEvent)
                                            fc{

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is Docs.DocComponent.Text -> {
                    ReactHTML.p {
                        +component.content
                    }
                }

                is Docs.DocComponent.UnlinkedList -> {
                    ReactHTML.ul {
                        component.entrys.forEach { entry ->
                            ReactHTML.li {
                                val fc = entry.render(arch, fileChangeEvent)
                                fc{

                                }
                            }
                        }
                    }
                }

                is Docs.DocComponent.Section -> {
                    ReactHTML.h3 {
                        +component.sectionTitle
                    }
                    component.sectionContent.forEach {
                        val fc = it.render(arch, fileChangeEvent)
                        fc {

                        }
                    }
                }
            }
        }
    }


}