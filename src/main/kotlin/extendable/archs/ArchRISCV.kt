package extendable.cisc

import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVFlags
import extendable.components.connected.Instruction
import extendable.components.types.BinaryTools
import extendable.components.types.ByteValue
import extendable.components.types.DecTools

class ArchRISCV() : Architecture(RISCV.config) {

    override fun exeContinuous() {
        super.exeContinuous()
        val reg = getRegisterContainer().getRegister("a0")

        for (i in 0..200) {
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
                                "RegexTemplate: ${ins.regex.pattern}"

                    )
                }

                ins.regex.findAll(remainingLine).forEach { match ->
                    val wrappedComment = highlight(match.value, RISCVFlags.instr)
                    wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                    remainingLine = remainingLine.replace(match.value, "")
                    getConsole().log("found instruction: ${ins.name}")
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

            val var1 = "12"
            val var2 = "1"

            val var3 = "12"
            val var4 = "-3"

            /*getConsole().log("DecTools.addUnsigned(): ${var1} + ${var2} = ${DecTools.addUnsigned(var1, var2)}")
            getConsole().log("DecTools.subUnsigned(): ${var1} - ${var2} = ${DecTools.subUnsigned(var1, var2)}")
            getConsole().log("DecTools.multiplyUnsigned(): ${var1} * ${var3} = ${DecTools.mutliplyUnsigned(var1, var3)}")
            getConsole().log("DecTools.add(): ${var3} + ${var4} = ${DecTools.add(var3, var4)}")
            getConsole().log("DecTools.abs(): ${var3} ? ${DecTools.abs(var3)}")
            getConsole().log("DecTools.abs(): ${var4} ? ${DecTools.abs(var4)}")
            getConsole().log("DecTools.isGreaterEqualThan(): ${var1} >= ${var2} ? ${DecTools.isGreaterEqualThan(var1, var2)}")
            getConsole().log("DecTools.isGreaterThan(): ${var3} > ${var4} ? ${DecTools.isGreaterThan(var3, var4)}")
            getConsole().log("DecTools.isEqual(): ${var3} = ${var4} ? ${DecTools.isEqual(var3, var4)}")
            getConsole().log("DecTools.negotiate(): ${var4} -> ${DecTools.negotiate(var4)}")
            getConsole().log("DecTools.powUnsigned(): ${var1} ^ ${var2} = ${DecTools.powUnsigned(var1, var2)}")
            getConsole().log("DecTools.divideUnsigned(): ${var1} / ${var2} = ${DecTools.divideUnsigned(var1, var2)}")
            getConsole().log("DecTools.divide(): ${var3} / ${var4} = ${DecTools.divide(var3, var4)}")*/

            val bin1 = "00001111"
            val bin2 = "00000101"

            /*getConsole().log("BinaryTools.inv(): ${bin1} = ${BinaryTools.inv(bin1)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.inv(bin1), ByteValue.Size.Byte())).getDecStr()}")
            getConsole().log("BinaryTools.addWithCarry(): ${bin1} + ${bin2} = ${BinaryTools.addWithCarry(bin1, bin2)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.addWithCarry(bin1, bin2).result, ByteValue.Size.Byte())).getDecStr()} carry: ${BinaryTools.addWithCarry(bin1, bin2).carry}")
            getConsole().log("BinaryTools.multiply(): ${bin1} * ${bin2} = ${BinaryTools.multiply(bin1, bin2)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.multiply(bin1, bin2), ByteValue.Size.Byte())).getDecStr()} ")*/

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