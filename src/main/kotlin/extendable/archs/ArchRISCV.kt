package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVFlags
import extendable.components.types.TypeDEC
import extendable.components.types.TypeLABEL

class ArchRISCV() : Architecture(RISCV.config) {

    override fun exeContinuous() {
        super.exeContinuous()
        getRegister().get(1).incValue()
        for (i in 0..200) {
            getDataMemory().save(i.toDouble(), i)
        }
        for(ins in getInstructions()){
            if(ins.name == "ADD"){
                getConsole().log("execute ADD")
                ins.execute(listOf(TypeDEC(2), TypeLABEL("s1")), getDataMemory(), getRegister(), getFlagsConditions())
            }
        }
    }

    override fun exeMultiStep(steps: Int) {
        super.exeMultiStep(steps)
        getDataMemory().save(0.0, steps)
    }

    override fun exeClear() {
        super.exeClear()

    }

    override fun hlAndCompile(code: String, startAtLine: Int): Pair<String, Boolean> {
        val absoluteValuesRegex = Regex("#(-?\\d+)")
        val addressesRegex = Regex("(&[0-9a-fA-F]+)")
        val lineRegex = Regex("\\b([a-zA-Z]+)\\b")

        val highlightedCode = StringBuilder()

        val insMap: MutableMap<String, Int> = mutableMapOf()
        for (ins in getInstructions()) {
            insMap[ins.name] = ins.exFormats.size
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

        return Pair(highlightedCode.toString(), false)
    }
}