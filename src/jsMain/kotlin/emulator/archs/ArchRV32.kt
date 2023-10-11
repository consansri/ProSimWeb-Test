package emulator.cisc

import emulator.kit.Architecture
import emulator.archs.riscv32.RV32
import emulator.archs.riscv32.RV32BinMapper
import emulator.archs.riscv32.RV32Flags
import emulator.archs.riscv32.RV32Syntax.*
import emulator.kit.types.HTMLTools
import emulator.kit.types.StringTools
import emulator.kit.types.Variable
import kotlin.time.measureTime

/**
 * This class connects the used custom and kit components, for a fully functional emulator and assembly process.
 */
class ArchRV32() : Architecture(RV32.config, RV32.asmConfig) {

    val instrnames = R_INSTR.InstrType.entries.map { Regex("""(?<=\s|^)(${Regex.escape(it.id)})(?=\s|$)""", RegexOption.IGNORE_CASE) }
    val registers = getRegContainer().getAllRegs().map { it.getRegexList() }.flatMap { it }
    val labels = listOf(Regex("""(?<=\s|^)(.+:)(?=\s|$)"""))
    val directivenames = listOf(".text", ".data", ".rodata", ".bss", ".globl", ".global", ".macro", ".endm", ".equ", ".byte", ".half", ".word", ".dword", ".asciz", ".string", ".2byte", ".4byte", ".8byte", ".attribute", ".option")
    val directive = directivenames.map { Regex("""(?<=\s|^)(${Regex.escape(it)})(?=\s|$)""") }
    val consts = listOf(Regex("""(?<=\s|^)(0x[0-9A-Fa-f]+)"""), Regex("""(?<=\s|^)(0b[01]+)"""), Regex("""(?<=\s|^)((-)?[0-9]+)"""), Regex("""(?<=\s|^)(u[0-9]+)"""), Regex("""('.')"""), Regex("\".+\""))
    val mapper = RV32BinMapper()

    override fun exeContinuous() {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0
            val measuredTime = measureTime {
                super.exeContinuous()

                var value = getMemory().load(getRegContainer().pc.get(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                while (result != null && instrCount < 1000) {
                    instrCount++
                    result.type.execute(this, result.binMap)

                    // Load next Instruction
                    value = getMemory().load(getRegContainer().pc.get(), 4)
                    result = mapper.getInstrFromBinary(value.toBin())
                }
            }
            getConsole().log("--continuous finishing... \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
        }
    }

    override fun exeSingleStep() {
        if (!this.getAssembly().isBuildable()) return

        val measuredTime = measureTime {
            super.exeSingleStep() // clears console

            // load binary from memory
            val loadedValue = getMemory().load(getRegContainer().pc.get(), 4)
            // identify instr and opcode usages (build binary map)
            val instrResult = mapper.getInstrFromBinary(loadedValue.toBin())
            // execute instr
            instrResult?.type?.execute(this, instrResult.binMap)
        }

        getConsole().log("--single_step finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeMultiStep(steps: Int) {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeMultiStep(steps)

                var value = getMemory().load(getRegContainer().pc.get(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                for (step in 0 until steps) {
                    if (result != null) {
                        instrCount++
                        result.type.execute(this, result.binMap)

                        // Load next Instruction
                        value = getMemory().load(getRegContainer().pc.get(), 4)
                        result = mapper.getInstrFromBinary(value.toBin())
                    } else {
                        break
                    }
                }
            }

            getConsole().log("--multi_step finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeSkipSubroutine() {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeSkipSubroutine()

                var value = getMemory().load(getRegContainer().pc.get(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())
                if (result != null) {
                    if (result.type == R_INSTR.InstrType.JAL || result.type == R_INSTR.InstrType.JALR) {
                        val returnAddress = getRegContainer().pc.get() + Variable.Value.Hex("4", Variable.Size.Bit32())
                        while (getRegContainer().pc.get() != returnAddress) {
                            if (result != null) {
                                result.type.execute(this, result.binMap)
                                instrCount++
                            } else {
                                break
                            }
                            value = getMemory().load(getRegContainer().pc.get(), 4)
                            result = mapper.getInstrFromBinary(value.toBin())
                        }

                    } else {
                        result.type.execute(this, result.binMap)
                        instrCount++
                    }
                }
            }

            getConsole().log("--exe_skip_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeReturnFromSubroutine() {
        if (this.getAssembly().isBuildable()) {
            super.exeReturnFromSubroutine()
            var instrCount = 0
            val measuredTime = measureTime {

                var value = getMemory().load(getRegContainer().pc.get(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                val returnTypeList = listOf(R_INSTR.InstrType.JALR, R_INSTR.InstrType.JAL)

                while (result != null && !returnTypeList.contains(result.type)) {
                    result.type.execute(this, result.binMap)

                    value = getMemory().load(getRegContainer().pc.get(), 4)
                    result = mapper.getInstrFromBinary(value.toBin())
                    instrCount++
                }
            }

            getConsole().log("--exe_return_from_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }

    }

    override fun exeUntilLine(lineID: Int) {
        if (this.getAssembly().isBuildable()) {
            super.exeUntilLine(lineID)
            var instrCount = 0

            val lineAddressMap = getAssembly().getAssemblyMap().lineAddressMap.map { it.value to it.key }.filter { it.first.file == getFileHandler().getCurrent() }
            var closestID: Int? = null
            for (entry in lineAddressMap) {
                if (entry.first.lineID >= lineID) {
                    if (closestID != null) {
                        if (entry.first.lineID < closestID) {
                            closestID = entry.first.lineID
                        }
                    } else {
                        closestID = entry.first.lineID
                    }
                }
            }
            val destAddrString = lineAddressMap.associate { it.first.lineID to it.second }.get(closestID) ?: ""
            if (destAddrString.isNotEmpty() && closestID != null) {
                val destAddr = Variable.Value.Hex(destAddrString)
                getConsole().info("--exe_until_line executing until line ${closestID + 1} or address ${destAddr.getHexStr()}")
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val value = getMemory().load(getRegContainer().pc.get(), 4)
                        val result = mapper.getInstrFromBinary(value.toBin())
                        if (destAddr == getRegContainer().pc.get()) {
                            break
                        }
                        if (result != null) {
                            result.type.execute(this, result.binMap)
                            instrCount++
                        } else {
                            break
                        }
                    }
                }

                getConsole().log("--exe_until_line finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            } else {
                getConsole().info("--exe_continuous")
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val value = getMemory().load(getRegContainer().pc.get(), 4)
                        val result = mapper.getInstrFromBinary(value.toBin())
                        if (result != null) {
                            result.type.execute(this, result.binMap)
                            instrCount++
                        } else {
                            break
                        }
                    }
                }

                getConsole().log("--exe_continuous finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            }
        }
    }

    override fun exeUntilAddress(address: Variable.Value.Hex) {
        super.exeUntilAddress(address)
        var instrCount = 0

        getConsole().info("--exe_until_line executing until address ${address.getHexStr()}")
        val measuredTime = measureTime {
            while (instrCount < 1000) {
                val value = getMemory().load(getRegContainer().pc.get(), 4)
                val result = mapper.getInstrFromBinary(value.toBin())
                if (address == getRegContainer().pc.get()) {
                    break
                }
                if (result != null) {
                    result.type.execute(this, result.binMap)
                    instrCount++
                } else {
                    break
                }
            }
        }
        getConsole().log("--exe_until_address finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeClear() {
        val measuredTime = measureTime {
            super.exeClear()
        }
        getConsole().log("--clear finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeReset() {
        val measuredTime = measureTime {
            super.exeReset()
        }
        getConsole().log("--reset finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun getPreHighlighting(text: String): String {
        val lines = HTMLTools.encodeHTML(text).split("\n").toMutableList()
        for (lineID in lines.indices) {
            // REMOVE COMMENTS
            val splitted = StringTools.splitStringAtFirstOccurrence(lines[lineID], '#')
            val comment = highlight(splitted.second, title = "comment", flag = RV32Flags.comment)

            // PREHIGHLIGHT ANYTHING ELSE
            var preline: String = hlText(splitted.first, instrnames, "instr", RV32Flags.instruction)
            preline = hlText(preline, labels, "lbl", RV32Flags.label)
            preline = hlText(preline, directive, "dir", RV32Flags.directive)
            preline = hlText(preline, registers, "reg", RV32Flags.register)
            preline = hlText(preline, consts, "const", RV32Flags.constant)

            lines[lineID] = preline + comment
        }

        return lines.joinToString("\n") { it }
    }

}