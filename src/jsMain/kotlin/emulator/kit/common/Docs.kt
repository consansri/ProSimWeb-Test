package emulator.kit.common

import react.FC
import react.Props
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.ul

/**
 * This class contains all documents which are partly supplied by specific architectures. There are two options to define a documentation file.
 * The first is by linking a source path to a specific html source file and the second is by directly defining a file as a React component, inwhich information can be generated directly from the implemented architecture.
 */
class Docs(vararg htmlFiles: HtmlFile) {
    var files = mutableListOf<HtmlFile>(
        HtmlFile.SourceFile(
            "User Manual",
            "../documents/user-manual.html"
        ),
        HtmlFile.DefinedFile("Version - ${Constants.version}", FC<Props> {
            h2 {
                +"Version - 0.1.5"
            }
            h3 {
                +"New"
            }
            ul {
                li {
                    +"KIT Feature System is fully integrated."
                }
                li {
                    +"""RV64 Extensions where added but aren't fully implemented (CSR and S can be enabled).
                        |Instructions and Registers usable without any restrictions (upcoming).
                    """.trimMargin()
                }
            }
            h3 {
                +"Changed"
            }
            ul {
                li {
                    +"KIT Privileges can be added to Registers."
                }
            }
            h3 {
                +"Fixed"
            }
            ul {
                li {
                    +"KIT FileHandler had some issues with undo redo state on page reload."
                }
                li {
                    +"REACT Editor had some code state indication issues when build is triggered through the button."
                }
            }
            h3 {
                +"Issues"
            }
            ul {
                li {
                    +"KIT RV64 Code examples are missing."
                }
                li {
                    +"REACT Editor Undo, Redo, Build and Clear Button shouldn't be used in trancript view. Will be hidden in next version!"
                }
            }

            h2 {
                +"Version - 0.1.4"
            }
            h3 {
                +"New"
            }
            ul {
                li {
                    +"""KIT & REACT The Calling Convention needs to be set for each Register.
                        |REACT now displays the calling convention in a separate column.
                    """.trimMargin()
                }
            }
            h3 {
                +"Changed"
            }
            ul {
                li {
                    +"KIT RV32 and RV64 under the hood the whole syntax implementation is dependent on a specific xlen (globally set parameter)."
                }
                li {
                    +"KIT types are either specified as signed or unsigned. (dec is signed while hex, bin, udec are unsigned)"
                }
                li {
                    +"KIT Compiler checking value sizes is now fully ported to the arch syntax implementation."
                }
            }
            h3 {
                +"Fixed"
            }
            ul {
                li {
                    pre {
                        code {
                            +"""
                            # KIT RV32 and RV64 where implemented with the wrong standard syntax.
                            old:
                                rd, offset(rs)  # old standard syntax
                            new:
                                rd, rs, offset  # new standard syntax
                                rd, offset(rs)  # still possible through pseudo instruction
                        """.trimIndent()
                        }
                    }
                }
                li {
                    pre {
                        code {
                            +"""
                            # KIT RV64 unlike in version 0.1.3 flexible li and la pseudo instructions are now implemented.
                            # The result of using li will be shown in the following example.
                            
                            # signed
                            li  t0, -3                  #  1 - 32 Bit -> lui, ori
                            
                            # unsigned
                            li  t0, 0xCAFEAFF           #  1 - 28 Bit -> lui, ori
                            li  t0, 0xCAFEAFFEDE        # 29 - 40 Bit -> lui, ori, slli, ori
                            li  t0, 0xCAFEAFFEDEADB     # 41 - 52 Bit -> lui, ori, slli, ori, slli, ori
                            
                            # signed & unsigned
                            li  t0, 0xCAFEAFFEDEADBEEF  # 53 - 64 Bit -> lui, ori, slli, ori, slli, ori, slli, ori
                            li  t0, 3432234234          # 33 - 64 Bit -> lui, ori, slli, ori, slli, ori, slli, ori
                            
                            # address labels (possible improvement)
                            la  t0, [alabel]            # always 64 Bit addresses -> lui, ori, slli, ori, slli, ori, slli, ori
                            
                        """.trimIndent()
                        }
                    }
                }
            }
            h3 {
                +"Issues"
            }
            ul {
                li {
                    +"KIT RV64 Code examples are missing."
                }
            }

            h2 {
                +"Version - 0.1.3"
            }
            h3 {
                +"New"
            }
            ul {
                li {
                    +"""REACT got a completely new component update system based on useStates.
                        |Manually refreshing components isn't needed anymore.""".trimMargin()
                }
                li {
                    +"""KIT RV64I is now in it's preview state"""
                }
                li {
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

            h3 {
                +"Changed"
            }
            ul {
                li {
                    +"""KIT Memory Component now returns a List of Memory Instances which already contain the row address and offset.
                        |This change happened to implement a more efficient way to rerender the memory without always recalculating the indexes of each instance.""".trimMargin()
                }
            }
            h3 {
                +"Fixed"
            }
            ul {
                li {
                    +"""RV64 & RV32: Resolved wrong jumps from faulty call and tail immediate calculations!"""
                }
            }
            h3 {
                +"Issues"
            }
            ul {
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