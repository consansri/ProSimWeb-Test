package emulator.kit.common

import Constants
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.common.Docs.DocComponent.*

/**
 * This class contains all documents which are partly supplied by specific architectures. There are two options to define a documentation file.
 * The first is by linking a source path to a specific html source file and the second is by directly defining a file as a React component, inwhich information can be generated directly from the implemented architecture.
 */
class Docs(val usingStandard: Boolean, vararg docFiles: DocFile) {
    var files: MutableList<DocFile> = (mutableListOf(
        DocFile.SourceFile(
            "User Manual",
            "/documents/user-manual.html"
        ),
        DocFile.DefinedFile(
            "Version - ${Constants.VERSION}",
            Chapter(
                "Version - 0.1.10",
                Section(
                    "New",
                    UnlinkedList(
                        Text("KIT: File Imports import Macro and Equ definitions beside the sections."),
                        Code(
                            """
                                // Added another popular commenting syntax!
                                // Those are not replacing the arch specific Syntax!
                                
                                // Single Line Comment
                                
                                /*
                                    Multi
                                    Line
                                    Comment                                    
                                */
                            """.trimIndent()
                        ),
                        Code(
                            """
                                // if you want to add raw data as a hex dump you now can just use the following data directive
                                // this will extract all hex chars [a-fA-F0-9] and will just store them into the memory
                                .data
                                    .hexstring ${"\"\"\""}
                                        CAFEAFFE
                                        DEADBEEF
                                        01234567
                                        89ABCDEF                                        
                                    ${"\"\"\""}
                            """.trimIndent()
                        ),
                        Code(
                            """
                                // now supporting nested expression of literals!
                                .data
	                                .word (((4 / 2) + 33) << (1 * 2))
                            """.trimIndent()
                        )
                    )
                ),
                Section(
                    "Changed",
                    UnlinkedList(
                        Text("GLOBAL Upgraded to Kotlin 1.9.23"),
                    )
                ),
                Section(
                    "Fixed",
                    UnlinkedList(
                        Text("KIT .asciz and .string now store in the right direction and append a zero at the end."),
                        Text("KIT RV64 and RV32 MULH and RV64 MULW had a faulty sign extension."),
                    )
                ),
                Section(
                    "Issues",
                    UnlinkedList(
                        Text("KIT Expression dot before line rule is missing on expressions without brackets!")
                    )
                )
            ),
            Chapter(
                "Version - 0.1.9",
                Section(
                    "New",
                    UnlinkedList(
                        Text("REACT Editor supports multiline indentation!"),
                        Text("KIT 6502 Technology in Preview!"),
                        Text("KIT IKR Mini in Preview!"),
                        Text("REACT Editor shortcut CTRL + S to build the project!"),
                        Text("REACT Switchable register view (either 2 independent views or one view with descriptions)!"),
                        Text(
                            "KIT There now is a standardized Assembler and Syntax implementation, which can be used. (currently used by RV32, RV64, T6502 and IKR Mini)\nIt provides features like imports, equs, macros, multiple sections (data, rodata, bss), labels, pc setter and global start definitions.\nThis simplifies the integration of new architectures and makes all features easily accessible for every architecture."
                        ),
                    )
                ),
                Section(
                    "Changed",
                    UnlinkedList(
                        Text("KIT moved from javascript target to common target which allows a parallel development of the wasm target, with a new ui."),
                        Text("REACT Editor improved current line information for macros and pseudo instructions."),
                        Text("REACT Editor code reformat is a little enhanced."),
                        Text("KIT Automatic syntax checks replace old pre highlighting."),
                        Text("GLOBAL Upgraded to Kotlin 1.9.22"),
                        Text("KIT Rewrote RISC-V parser and assembler."),
                        Text("KIT Direct syntax analysis on every change."),
                        Text("KIT Compiler is more efficient and Lexer uses updated Tokens."),
                    )
                )
            )

            /*FC<Props> {
            h2 { +"Version - 0.1.9" }
            h3 { +"New" }
            ul {
                li { +"REACT Editor supports multiline indentation!" }
                li { +"KIT 6502 Technology in Preview!" }
                li { +"KIT IKR Mini in Preview!" }
                li { +"REACT Editor shortcut CTRL + S to build the project!" }
                li { +"REACT Switchable register view (either 2 independent views or one view with descriptions)!" }
                li {
                    +"""KIT There now is a standardized Assembler and Syntax implementation, which can be used. (currently used by RV32, RV64, T6502 and IKR Mini)
                    |It provides features like imports, equs, macros, multiple sections (data, rodata, bss), labels, pc setter and global start definitions.
                    |This simplifies the integration of new architectures and makes all features easily accessible for every architecture.
                """.trimMargin()
                }
            }
            h3 { +"Changed" }
            ul {
                li { +"REACT Editor improved current line information for macros and pseudo instructions" }
                li { +"REACT Editor code reformat is a little enhanced." }
                li { +"KIT Automatic syntax checks replace old pre highlighting." }
                li { +"GLOBAL Upgraded to Kotlin 1.9.22" }
                li { +"KIT Rewrote RISC-V parser and assembler." }
                li { +"KIT Direct syntax analysis on every change." }
                li { +"KIT Compiler is more efficient and Lexer uses updated Tokens." }
            }

            h2 { +"Version - 0.1.8" }
            h3 { +"New" }
            ul {
                li { +"KIT RV32 & RV64 Syntax inline instructions with labels now possible!" }
                li { +"KIT RV32 & RV64 M Extension added!" }
            }
            h3 { +"Changed" }
            ul {
                li { +"KIT RV32 CSR Extension now also implemented for RV32!" }
                li { +"KIT RV32 reimplemented all rv32 instructions." }
                li { +"KIT Memory sections now can be changed via Architecture Settings!" }
                li {
                    +"""REACT More relevant settings will be stored and reloaded in and from local storage!
                    |General: Theme, MMIO
                    |Specific: Architecture Features, Architecture Settings (e.g. risc-v data, rodata and bss section)
                """.trimMargin()
                }
            }
            h3 { +"Fixed" }
            ul {
                li { +"KIT RV32 & RV64 ADDI, ORI, ... decimal values wrong interpretation." }
                li { +"Wrong paths for docs." }
            }

            h2 { +"Version - 0.1.7" }
            h3 { +"Changed" }
            ul {
                li { +"REACT exchanged edit icon." }
                li { +"KIT Memory cleares first on reset." }
            }
            h3 { +"Fixed" }
            ul {
                li { +"REACT Fixed: MMIO values weren't visualy updating on change from program." }
            }


            h2 { +"Version - 0.1.6" }
            h3 { +"New" }
            ul {
                li { +"KIT RV64 Code examples now added." }
            }
            h3 { +"Changed" }
            ul {
                li { +"REACT Design changed for a more minimalistic style which allows a better focus on the actual content." }
            }
            h3 { +"Fixed" }
            ul {
                li { +"REACT Editor Undo, Redo, Build and Clear Button are hidden in transcript view!" }
                li { +"KIT RV32 & RV64 .data and .rodata updated syntax changes. (examples below)" }
                li {
                    pre {
                        code {
                            +"""
                    .data

                    # string arrays always where possible
                    myStringArray:	.string	"i always existed :|"

                    # all other arrays are now possibly too :)
                    myByteArray:	.byte 	0xC0, 0xFF, 0xEE, 0xBA, 0xBE

                    # and standalone data emitting directives are now usable
                    myTable:	.half	0x0001, 0x0002, 0x0003, 0x0004
                                    .half 	0x0005, 0x0006, 0x0007, 0x0008
                                    #...
                """.trimIndent()
                        }
                    }
                }
            }

            h2 { +"Version - 0.1.5" }
            h3 { +"New" }
            ul {
                li { +"KIT Feature System is fully integrated." }
                li {
                    +"""RV64 Extensions where added but aren't fully implemented (CSR and S can be enabled).
                        |Instructions and Registers usable without any restrictions (upcoming).
                    """.trimMargin()
                }
            }
            h3 { +"Changed" }
            ul {
                li { +"KIT Privileges can be added to Registers." }
            }
            h3 { +"Fixed" }
            ul {
                li { +"KIT FileHandler had some issues with undo redo state on page reload." }
                li { +"REACT Editor had some code state indication issues when build is triggered through the button." }
            }

            h2 { +"Version - 0.1.4" }
            h3 { +"New" }
            ul {
                li {
                    +"""KIT & REACT The Calling Convention needs to be set for each Register.
                        |REACT now displays the calling convention in a separate column.
                    """.trimMargin()
                }
            }
            h3 { +"Changed" }
            ul {
                li { +"KIT RV32 and RV64 under the hood the whole syntax implementation is dependent on a specific xlen (globally set parameter)." }
                li { +"KIT types are either specified as signed or unsigned. (dec is signed while hex, bin, udec are unsigned)" }
                li { +"KIT Compiler checking value sizes is now fully ported to the arch syntax implementation." }
            }
            h3 { +"Fixed" }
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
            h3 { +"Issues" }

            h2 { +"Version - 0.1.3" }
            h3 { +"New" }
            ul {
                li {
                    +"""REACT got a completely new component update system based on useStates.
                        |Manually refreshing components isn't needed anymore.""".trimMargin()
                }
                li { +"""KIT RV64I is now in it's preview state""" }
                li { +"""GLOBAL Switched to Kotlin 1.9.20 where Kotlin Multiplatform becomes stable""" }
                li { +"""REACT Menu can now be collapsed and elapsed""" }
                li { +"""KIT now supports ISA extension features""" }
                li { +"""REACT Console can hide or show logs (controlled with button)""" }
            }

            h3 { +"Changed" }
            ul {
                li {
                    +"""KIT Memory Component now returns a List of Memory Instances which already contain the row address and offset.
                        |This change happened to implement a more efficient way to rerender the memory without always recalculating the indexes of each instance.""".trimMargin()
                }
                li {
                    +"""KIT Caused by the new Memory and Update System, the assembly process is now a little slower.
                        |This is not directly caused by inefficient code instead it is caused by JavaScript which slows down with each loop run.""".trimMargin()
                }
            }
            h3 { +"Fixed" }
            ul {
                li { +"""RV64 & RV32: Resolved wrong jumps from faulty call and tail immediate calculations!""" }
            }
        }*/
        )
    ) + if (usingStandard) DocFile.DefinedFile(
        "Standard Syntax",
        Chapter(
            "Directives",
            *StandardSyntax.DirMajType.entries.map { maj -> Section(maj.docName, UnlinkedList(*StandardSyntax.DirType.entries.filter { it.dirMajType == maj }.map { Text(".${it.dirname}") }.toTypedArray())) }.toTypedArray()
        ),
        Chapter(
            "Example",
            Code(
                """
                    #import "anotherfile.s"

                    /*
                        Important!
                         -> The symbol which indicates a comment is set by arch
                         -> The prefix of bin,hex,dec and udec is set by arch
                         -> Instruction Syntax is completely handled by arch
                    */

                    .equ SOME_VALUE0, "This is a constant which will be written on address 0x0F04"

                    .macro MACRO_NAME attr1, attr2
                    	# some content insert attributes with leading '\'
                    	# ex. "\attr1"
                    .endm

                    *=0xA000
                    label_on_0xA000:
                    .data
                    			.byte 	(((4 / 2) + 33) << 1)
                    			.half 	(0xA000 >> 12)
                    			.word 	(-10 * 0xFFFFFFFF)
                    			.dword	0x0123456789ABCDEF
                    			.asciz	'#'
                    string: 	.string SOME_VALUE0
                    
                    rawdata:    .hexstring \"\"\"
                                        CAFEAFFE
                                        DEADBEEF
                                        01234567
                                        89ABCDEF                                        
                                    \"\"\"

                    *=0xA100
                    byte_array:	.half 0, 1, 2, 3, (0b1 << 2), 5, (0xC / 2), 7,
                    			8, 9, 10, 11, 12, 13, 14, 15,
                    			16, 17, 18, (20 - 1), 20, 21, (20 + 2), 23

                    *=0xB000
                    .rodata
                    	.string "This can't be overwritten!"

                    *=0xC000
                    .bss
                    	.byte
                    	.half
                    	.half
                    	.word

                    .text

                """.trimIndent()
            )
        )
    ) else null).filterNotNull().toMutableList()

    init {
        files.addAll(docFiles.toList())
    }

    sealed class DocFile(val title: String) {
        class SourceFile(title: String, val src: String) : DocFile(title)
        class DefinedFile(title: String, vararg val chapters: Chapter) : DocFile(title)
    }

    sealed class DocComponent {
        class Table(val header: List<String>, vararg val contentRows: List<DocComponent>) : DocComponent()
        class Text(val content: String) : DocComponent()
        class Code(val content: String) : DocComponent()
        class Chapter(val chapterTitle: String, vararg val chapterContent: DocComponent) : DocComponent()
        class Section(val sectionTitle: String, vararg val sectionContent: DocComponent) : DocComponent()
        class UnlinkedList(vararg val entrys: DocComponent) : DocComponent()
    }

}