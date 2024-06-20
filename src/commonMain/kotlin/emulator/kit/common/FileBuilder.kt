package emulator.kit.common

import Constants
import emulator.kit.Architecture
import emulator.kit.assembler.AsmFile
import emulator.kit.common.FileBuilder.ExportFormat
import emulator.kit.common.FileBuilder.Setting
import emulator.kit.common.FileBuilder.buildFileContentLines
import emulator.kit.nativeError
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import emulator.core.Value
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * The [FileBuilder] can [buildFileContentLines] [ExportFormat]'s which then will be returned as a List<String>. In addition to that the [FileBuilder] can receive [Setting]'s which allow to control the fileoutput in certain aspects.
 *
 *
 */
object FileBuilder {
    fun buildFileContentLines(architecture: Architecture, format: ExportFormat, currentFile: AsmFile, others: List<AsmFile>, vararg settings: Setting): List<String> {
        when (format) {
            ExportFormat.VHDL, ExportFormat.MIF, ExportFormat.HEXDUMP -> {
                val content = mutableListOf("Empty...")

                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                var addrWidth = 32
                var dataWidth = 256

                if (settings.isNotEmpty()) {
                    settings.forEach {
                        when (it) {
                            is Setting.AddressWidth -> {
                                addrWidth = it.bits
                            }

                            is Setting.DataWidth -> {
                                dataWidth = it.bits
                            }
                        }
                    }
                }
                val depth = 2.0.pow(addrWidth) * dataWidth
                val memInstances = architecture.memory.memList.sortedBy { it.address.getRawHexStr() }.toMutableList()

                nativeLog("FileBuilder: AddrWidth: $addrWidth, DataWidth: $dataWidth from ${settings.joinToString(",") { it.toString() }}")

                val vhdlItems = mutableListOf<VHDLItem>()
                var itemsPerRow = Value.Dec((dataWidth / 8).toString()).getRawDecStr().toLongOrNull()
                if (itemsPerRow == null) {
                    itemsPerRow = 1
                }

                for (instance in memInstances) {
                    val rowAddr = instance.address.getRawHexStr().toLong(16) / itemsPerRow
                    val id = (instance.address % Value.Dec((dataWidth / 8).toString())).toHex().getRawHexStr().toIntOrNull(16)
                    if (id != null) {
                        vhdlItems.add(VHDLItem(rowAddr, id, instance.variable.get().toHex().getRawHexStr()))
                    } else {
                        architecture.console.error("FileBuilder: problems by calculating instance id from address ${rowAddr}!")
                        return content
                    }
                }

                val rowMap = mutableMapOf<Long, String>()

                while (true) {
                    val first = vhdlItems.firstOrNull()
                    if (first != null) {
                        var oldValue = rowMap[first.rowID]
                        if (oldValue != null) {
                            val oldValueList = oldValue.chunked(2).toMutableList()
                            if (first.id >= oldValueList.size) {
                                nativeWarn("out of range for id: ${first.id} content: ${first.hexContent} in $oldValueList")
                            }
                            oldValueList[first.id] = first.hexContent
                            oldValue = oldValueList.joinToString("") { it.lowercase() }
                        } else {
                            val oldValueList = "0".repeat(dataWidth / 4).chunked(2).toMutableList()
                            oldValueList[first.id] = first.hexContent
                            oldValue = oldValueList.joinToString("") { it.lowercase() }
                        }
                        rowMap[first.rowID] = oldValue
                        vhdlItems.removeFirst()
                    } else {
                        break
                    }
                }

                try {
                    try {
                        when (format) {
                            ExportFormat.VHDL -> {
                                content.clear()
                                // Auto Generated
                                content.add(
                                    "\n-- \n-- Generated by ${Constants.NAME} ${architecture.description.name} \n-- on ${now}" +
                                            "\n-- " +
                                            "\n-- Input file:" +
                                            "\n-- \t${currentFile.mainRelativeName}" +
                                            "\n--" +
                                            "\n-- Output file:" +
                                            "\n-- \t${currentFile.mainRelativeName.removeFileEnding() + format.ending}" +
                                            "\n-- "
                                )

                                // File Content
                                content.add(
                                    "\n" +
                                            "\nlibrary ieee;" +
                                            "\nuse ieee.std_logic_1164.all;" +
                                            "\n" +
                                            "\npackage mem_contents is" +
                                            "\n\tconstant ADDR_WIDTH : positive := $addrWidth;" +
                                            "\n\tconstant DATA_WIDTH : positive := $dataWidth;" +
                                            "\n\ttype mem_content_type is array (0 to 2**ADDR_WIDTH - 1) of std_logic_vector(DATA_WIDTH - 1 downto 0);" +
                                            "\n\tconstant mem_content : mem_content_type := (${rowMap.toList().joinToString(",") { "\n\t\t${it.first} => X\"${it.second.lowercase()}\"" }},\n\t\tothers => X\"${"0".repeat(dataWidth / 4)}\");" +
                                            "\n\t" +
                                            "\n\tconstant mem_used : positive := ${rowMap.size - 1};" +
                                            "\nend mem_contents;" +
                                            "\n"
                                )
                            }

                            ExportFormat.MIF -> {
                                content.clear()

                                // Auto Generated
                                content.add(
                                    "\n" +
                                            "\n-- Altera Memory Initialization File" +
                                            "\n-- created on ${now}" +
                                            "\n-- by ${Constants.NAME} ${architecture.description.name}"
                                )

                                val contentString = rowMap.map {
                                    "${it.key.toString(16).padStart(addrWidth / 4, '0')} : ${it.value.lowercase()};"
                                }.joinToString("\n") { it }

                                /*for (line in 0..<2.0.pow(addrWidth).roundToLong()) {
                                    val rowContent = rowMap[line]
                                    contentString += if (rowContent != null) {
                                        "\n ${line.toString(16).padStart(addrWidth / 4, '0')} : ${rowContent.lowercase()};"
                                    } else {
                                        "\n ${line.toString(16).padStart(addrWidth / 4, '0')} : ${"0".repeat(dataWidth / 4)};"
                                    }
                                }*/

                                // CONTENT
                                content.add(
                                    "\n" +
                                            "\nWIDTH = $dataWidth;" +
                                            "\nDEPTH = ${depth};" +
                                            "\n" +
                                            "\nADDRESS_RADIX = HEX;" +
                                            "\nDATA_RADIX = HEX;" +
                                            "\n" +
                                            "\nCONTENT" +
                                            "\nBEGIN" +
                                            "\n" +
                                            "\n$contentString" +
                                            "\n" +
                                            "\nEND;"
                                )
                            }

                            ExportFormat.HEXDUMP -> {
                                content.clear()

                                var contentString = ""
                                val remainingRowsWithContent = rowMap.map { it.key }.toMutableList()

                                for (line in 0..<2.0.pow(addrWidth).roundToLong()) {
                                    val rowContent = rowMap[line]
                                    if (remainingRowsWithContent.isNotEmpty()) {
                                        contentString += if (rowContent != null) {
                                            remainingRowsWithContent.remove(line)
                                            "${rowContent.lowercase()}\n"
                                        } else {
                                            "${"0".repeat(dataWidth / 4)}\n"
                                        }
                                    } else {
                                        break
                                    }
                                }

                                content.add(contentString)
                            }

                            else -> {}
                        }
                    } catch (ne: NumberFormatException) {
                        nativeError(ne.toString())
                    }

                } catch (e: IndexOutOfBoundsException) {
                    nativeError(e.toString())
                }

                return content
            }

            ExportFormat.CURRENT_FILE -> {
                return currentFile.content.split("\n")
            }

            ExportFormat.TRANSCRIPT -> {
                val result = architecture.compile(currentFile, others, build = false)
                return result.generateTS().split("\n")
            }
        }
    }

    private fun String.removeFileEnding(): String {
        val lastDotIndex = this.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            this.substring(0, lastDotIndex)
        } else {
            this
        }
    }

    enum class ExportFormat(val uiName: String, val ending: String) {
        VHDL("VHDL", "_pkg.vhd"),
        MIF("MIF (Memory Initialization File)", ".mif"),
        HEXDUMP("HEXDUMP", ".txt"),
        TRANSCRIPT("TRANSCRIPT", ".transcript"),
        CURRENT_FILE("current file", "")
    }

    sealed class Setting(val name: String) {
        class AddressWidth(val bits: Int) : Setting("Address Width"){
            override fun toString(): String {
                return "$name:$bits"
            }
        }
        class DataWidth(val bits: Int) : Setting("Data Width"){
            override fun toString(): String {
                return "$name:$bits"
            }
        }
    }

    data class VHDLItem(val rowID: Long, val id: Int, val hexContent: String)
}