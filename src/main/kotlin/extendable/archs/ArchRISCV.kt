package extendable.cisc

import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVFlags
import extendable.components.connected.Instruction
import extendable.components.types.ByteValue

class ArchRISCV() : Architecture(RISCV.config) {

    override fun exeContinuous() {
        super.exeContinuous()
        val reg = getRegisterContainer().getRegister("a0")

        reg?.let {
            reg.set(reg.get() + ByteValue.Type.Dec("1", reg.byteValue.size))
        }

        for (i in 0..100) {
            getMemory().saveDec(i.toDouble(), i.toString(10))
        }
        for (ins in getInstructions()) {
            if (ins.name == "ADD") {
                getConsole().log("execute ADD")
                ins.execute("", listOf(Instruction.Ext.Imm(2), Instruction.Ext.Reg("ra")), getMemory(), getRegisterContainer(), getFlagsConditions())
            }
        }
    }

    override fun exeMultiStep(steps: Int) {
        super.exeMultiStep(steps)
        getMemory().saveDec(0.0, steps.toString(10))
    }

    override fun exeClear() {
        super.exeClear()

    }

    override fun getPreHighlighting(line: String): String {


        return super.getPreHighlighting(line)
    }

    override fun hlAndCompile(code: String, startAtLine: Int): Pair<String, Boolean> {

        var buildable = true

        /* ----------------------- HIGHLIGHT and CHECK ------------------------- */
        /**
         *   Line by Line
         */


        val absoluteValuesRegex = Regex("#(-?\\d+)")
        val addressesRegex = Regex("(&[0-9a-fA-F]+)")
        val lineRegex = Regex("\\b([a-zA-Z]+)\\b")

        val highlightedCode = StringBuilder()

        val directive: MutableMap<String, Int> = mutableMapOf()


        val lines = code.split(*ArchConst.LINEBREAKS.toTypedArray())

        lines.forEach { line ->
            val lineNumber = lines.indexOf(line)

            // If this Buffer is empty everything should be found and made sense if not this line has an error
            var remainingLine = line
            var wrappedLine = line

            /* Comment */
            RISCV.REGEX_COMMENT.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.comment)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("found comment")
            }

            /* Directive */
            RISCV.REGEX_DIRECTIVE.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.directive)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("found directive")
            }

            /* Labels */
            RISCV.REGEX_LABEL.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.label)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("found label")
            }

            /* Instructions */
            for (ins in getInstructions()) {
                if (remainingLine.uppercase().contains(ins.name)) {
                    getConsole().info(
                        "Line $lineNumber: found Instruction ${ins.name}\n " +
                                "Example: ${ins.example()}" +
                                "RegexTemplate: ${ins.nameRegex.pattern}"

                    )


                }

                ins.nameRegex.findAll(remainingLine).forEach { match ->
                    val wrappedComment = highlight(match.value, RISCVFlags.instr)
                    wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                    remainingLine = remainingLine.replace(match.value, "")

                    var foundParams = ""

                    for(extension in ins.exFormats){
                        val paramResult = ins.paramRegexList.get(extension)?.find(remainingLine)
                        paramResult?.let {
                            foundParams += " ${it.value} ${it.groups}"

                            val wrappedParam = highlight(it.value, RISCVFlags.name)

                            wrappedLine = wrappedLine.replace(it.value, wrappedParam)
                            remainingLine = remainingLine.replace(it.value, "")

                        }
                    }
                    getConsole().log("found instruction: ${ins.name}$foundParams")
                }


            }

            /* Variables */


            /* Data Area */


            /* Macros */
            RISCV.REGEX_MACRO.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.macro)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("found macro")
            }

            /* Includes */
            RISCV.REGEX_INCLUDE.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.include)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("found include")
            }

            /* Error Handling */
            if (remainingLine.trim().isNotEmpty()) {
                buildable = false
                wrappedLine = highlight(wrappedLine, RISCVFlags.error)
                getConsole().error("found error ${remainingLine}")
            }

            highlightedCode.append(wrappedLine)
            highlightedCode.append("\n")
        }

        /* ----------------------- Generate Disassembled View and Write Binary to Memory ------------------------- */
        /**
         *   Line by Line
         */


        if (buildable) {

        }

        return Pair(highlightedCode.toString(), buildable)
    }
}