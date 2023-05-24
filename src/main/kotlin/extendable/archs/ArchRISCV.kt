package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCVFlags
import extendable.components.DataMemory
import extendable.components.Instruction
import extendable.components.Transcript
import extendable.components.Register

class ArchRISCV : Architecture {

    constructor() : super(
        "RISC-V",
        arrayOf(
            Register(0, "zero", 0, "hardwired zero"),
            Register(1, "ra", 0, "return address"),
            Register(2, "sp", 0, "stack pointer"),
            Register(3, "gp", 0, "global pointer"),
            Register(4, "tp", 0, "thread pointer"),
            Register(5, "t0", 0, "temporary register 0"),
            Register(6, "t1", 0, "temporary register 1"),
            Register(7, "t2", 0, "temporary register 2"),
            Register(8, "s0 / fp", 0, "saved register 0 / frame pointer"),
            Register(9, "s1", 0, "saved register 1"),
            Register(10, "a0", 0, "function argument 0 / return value 0"),
            Register(11, "a1", 0, "function argument 1 / return value 1"),
            Register(12, "a2", 0, "function argument 2"),
            Register(13, "a3", 0, "function argument 3"),
            Register(14, "a4", 0, "function argument 4"),
            Register(15, "a5", 0, "function argument 5"),
            Register(16, "a6", 0, "function argument 6"),
            Register(17, "a7", 0, "function argument 7"),
            Register(18, "s2", 0, "saved register 2"),
            Register(19, "s3", 0, "saved register 3"),
            Register(20, "s4", 0, "saved register 4"),
            Register(21, "s5", 0, "saved register 5"),
            Register(22, "s6", 0, "saved register 6"),
            Register(23, "s7", 0, "saved register 7"),
            Register(24, "s8", 0, "saved register 8"),
            Register(25, "s9", 0, "saved register 9"),
            Register(26, "s10", 0, "saved register 10"),
            Register(27, "s11", 0, "saved register 11"),
            Register(28, "t3", 0, "temporary register 3"),
            Register(29, "t4", 0, "temporary register 4"),
            Register(30, "t5", 0, "temporary register 5"),
            Register(31, "t6", 0, "temporary register 6")

        ),
        listOf(
            Instruction("ADD", 3),
            Instruction("SUB", 3),
            Instruction("ADDI", 3),
            Instruction("SLT", 3),
            Instruction("SLTI", 3),
            Instruction("SLTU", 3),
            Instruction("SLTIU", 3),
            Instruction("LUI", 2),
            Instruction("AUIP", 2)
        ),
        DataMemory(32, 4),
        Transcript(4, 32, 32)
    ) {

    }

    override fun exeContinuous() {
        super.exeContinuous()
        for (i in 0..200) {
            getDataMemory().save(i.toDouble(), i)
        }
        getRegister().get(1).value = getDataMemory().load(100.0) ?: 0
    }

    override fun exeMultiStep(steps: Int) {
        super.exeMultiStep(steps)
        getDataMemory().save(0.0, steps)
    }

    override fun exeClear() {
        super.exeClear()

    }

    override fun highlightArchSyntax(code: String): String {
        val absoluteValuesRegex = Regex("#(-?\\d+)")
        val addressesRegex = Regex("(&[0-9a-fA-F]+)")
        val lineRegex = Regex("\\b([a-zA-Z]+)\\b")

        val highlightedCode = StringBuilder()

        val insMap: MutableMap<String, Int> = mutableMapOf()
        for (ins in getInstructions()) {
            insMap[ins.name] = ins.extensionCount
        }


        code.split("\n").forEach { line ->
            val matchResult = lineRegex.findAll(line)
            var currentIndex = 0
            var isValidLine = true

            matchResult.forEach { match ->
                val startIndex = match.range.first
                val endIndex = match.range.last + 1

                val prefix = line.substring(currentIndex, startIndex)
                val word = line.substring(startIndex, endIndex)

                highlightedCode.append(prefix)

                when {
                    insMap.containsKey(word.uppercase()) -> {
                        val expectedOperandCount = insMap[word.uppercase()] ?: 0
                        val operands = line.substring(endIndex).trim().split(",").map { it.trim() }
                        val operandCount = operands.size

                        if (operandCount != expectedOperandCount) {
                            highlightedCode.append(highlight(word, RISCVFlags.errorFlag))
                            isValidLine = false
                        } else {
                            highlightedCode.append(highlight(word, RISCVFlags.instrFlag))
                        }
                    }

                    absoluteValuesRegex.matches(word) -> {
                        highlightedCode.append(highlight(word, RISCVFlags.valueFlag))
                    }

                    addressesRegex.matches(word) -> {
                        highlightedCode.append(highlight(word, RISCVFlags.addressFlag))
                    }

                    else -> {
                        highlightedCode.append(highlight(word, RISCVFlags.errorFlag))
                    }
                }

                currentIndex = endIndex
            }

            if (!isValidLine) {
                highlightedCode.insert(0, highlightBeginTag(RISCVFlags.errorFlag))
                highlightedCode.append(highlightEndTag())
            }

            highlightedCode.append(line.substring(currentIndex))
            highlightedCode.append("\n")
        }

        return highlightedCode.toString()
    }


}