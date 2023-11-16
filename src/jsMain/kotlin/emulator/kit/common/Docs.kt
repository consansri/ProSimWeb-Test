package emulator.kit.common

import react.FC
import react.Props
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.ul

/**
 * This class contains all documents which are partly supplied by specific architectures. There are two options to define a documentation file.
 * The first is by linking a source path to a specific html source file and the second is by directly defining a file as an react component, inwhich information can be generated directly from the implemented architecture.
 */
class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile.SourceFile(
            "User Manual",
            "../documents/user-manual.html"
        ),
        HtmlFile.DefinedFile("Version - ${Constants.version}", FC<Props> {
            h2 {
                +"Version - 0.1.3"
            }
            h3{
                +"New"
            }
            ul{
                li{
                    +"""REACT got a completely new component update system based on useStates.
                        |Manually refreshing components isn't needed anymore.""".trimMargin()
                }
                li{
                    +"""KIT RV64I is now in it's preview state"""
                }
                li{
                    +"""GLOBAL Switched to Kotlin 1.9.20 where Kotlin Multiplatform becomes stable"""
                }
                li {
                    +"""REACT Menu can now be collapsed and elapsed"""
                }
                li {
                    +"""KIT now supports ISA extension features"""
                }
                li {
                    +"""REACT Console can hide or show logs (controlled with button)"""
                }
            }

            h3{
                +"Changed"
            }
            ul{
                li{
                    +"""KIT Memory Component now returns a List of Memory Instances which already contain the row address and offset.
                        |This change happened to implement a more efficient way to rerender the memory without always recalculating the indexes of each instance.""".trimMargin()
                }
            }
            h3{
                +"Fixed"
            }
            ul{
                li {
                    +"""RV64 & RV32: Resolved wrong jumps from faulty call and tail immediate calculations!"""
                }
            }
            h3{
                +"Issues"
            }
            ul{
                li {
                    +"""KIT Caused by the new Memory and Update System, the assembly process is now a little slower. 
                        |This is not directly caused by inefficient code instead it is caused by JavaScript which slows down with each loop run.""".trimMargin()
                }
            }
        })
    )

    init {
        files.addAll(htmlFiles)
    }

    constructor() : this(*emptyArray<HtmlFile>())

    sealed class HtmlFile(val title: String) {

        class SourceFile(title: String, val src: String) : HtmlFile(title)
        class DefinedFile(title: String, val fc: FC<Props>) : HtmlFile(title)

    }
}