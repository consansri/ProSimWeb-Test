package extendable.cisc

import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVFlags
import extendable.components.connected.Assembly
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
                ins.execute(true, "", listOf(Instruction.Ext.Imm(ByteValue.Type.Dec("2", ByteValue.Size.Byte())), Instruction.Ext.Reg("ra")), getMemory(), getRegisterContainer(), getFlagsConditions())
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
         *   1. Find Assembly Tokens
         *   2. Highlight Assembly Tokens
         */

        val tokenList = mutableListOf<Assembly.Token>()

        val highlightedCode = StringBuilder()

        val lines = code.split(*ArchConst.LINEBREAKS.toTypedArray())

        lines.forEach { line ->
            val lineID = lines.indexOf(line)
            val lineNum = lineID + 1

            // If this Buffer is empty everything should be found and made sense if not this line has an error
            var remainingLine = line
            var wrappedLine = line

            /* Comment */
            Assembly.Regex.COMMENT.findAll(remainingLine).forEach { match ->

                tokenList += Assembly.Token.Comment(Assembly.LineLoc(lineID, match.range.first, match.range.last), match.value)

                val wrappedComment = highlight(match.value, RISCVFlags.comment)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("line $lineNum: found comment")
            }

            /* Directive */
            Assembly.Regex.DIRECTIVE.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.directive)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("line $lineNum: found directive")
            }

            /* Labels */
            Assembly.Regex.LABEL.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.label)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("line $lineNum: found label")
            }

            /* Instructions */
            for (ins in getInstructions()) {


                if (remainingLine.uppercase().contains(ins.name)) {
                    getConsole().info(
                        "line $lineNum: found Instruction ${ins.name}\n " +
                                "Example: ${ins.example()}\n" +
                                "RegexTemplate: ${ins.nameRegex.pattern} ${ins.paramRegexList}"

                    )
                }

                ins.nameRegex.findAll(remainingLine).forEach { match ->
                    var valid = true
                    var startIndex: Int = match.range.start
                    var endIndex: Int = remainingLine.lastIndex

                    var foundParams = ""

                    var paramBuffer = remainingLine
                    var paramMap = mutableMapOf<String, String>()

                    for (extension in ins.exFormats) {
                        val index = ins.exFormats.indexOf(extension)

                        val paramResult = ArchConst.extMap.get(extension)?.find(paramBuffer)
                        paramResult?.let {
                            endIndex = it.range.last
                            foundParams += "${it.value}"

                            val wrappedParam = highlight(it.value, RISCVFlags.name)
                            paramMap += it.value to wrappedParam

                            paramBuffer = paramBuffer.replace(it.value, "")
                        }

                        val splitResult = ins.splitRegex.find(paramBuffer)
                        splitResult?.let {
                            foundParams += ins.paramSplit
                            paramBuffer = paramBuffer.replace(it.value, "")
                        }

                        if (
                            paramResult == null ||
                            (splitResult == null && index < ins.exFormats.size - 1) ||
                            (splitResult != null && index == ins.exFormats.size - 1)
                        ) {
                            // NO VALID INSTRUCTION
                            valid = false
                            break
                        }
                    }

                    if (valid) {
                        val wholeInstr = remainingLine.substring(startIndex, endIndex)
                        var wrappedParams = wholeInstr
                        for (entry in paramMap) {
                            wrappedParams = wrappedParams.replace(entry.key, entry.value)
                        }
                        val wrappedIns = highlight(wrappedParams, RISCVFlags.instr)
                        wrappedLine = wrappedLine.replace(wholeInstr, wrappedIns)
                        remainingLine = remainingLine.replace(wholeInstr, "")
                        getConsole().log("line $lineNum: found valid instruction: $wholeInstr")
                    } else {
                        val wholeInstr = remainingLine.substring(startIndex, endIndex)
                        var wrappedParams = wholeInstr
                        for (entry in paramMap) {
                            wrappedParams = wrappedParams.replace(entry.key, entry.value)
                        }
                        val wrappedIns = highlight(wrappedParams, RISCVFlags.error)
                        wrappedLine = wrappedLine.replace(wholeInstr, wrappedIns)
                        remainingLine = remainingLine.replace(wholeInstr, "")
                        getConsole().error("line $lineNum: found invalid instruction: $wholeInstr")
                    }


                }


            }

            /* Variables */


            /* Data Area */


            /* Macros */
            Assembly.Regex.MACRO.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.macro)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("line $lineNum: found macro")
            }

            /* Includes */
            Assembly.Regex.INCLUDE.findAll(remainingLine).forEach { match ->
                val wrappedComment = highlight(match.value, RISCVFlags.include)
                wrappedLine = wrappedLine.replace(match.value, wrappedComment)
                remainingLine = remainingLine.replace(match.value, "")
                getConsole().log("line $lineNum: found include")
            }

            /* Error Handling */
            if (remainingLine.trim().isNotEmpty()) {
                buildable = false
                wrappedLine = highlight(wrappedLine, RISCVFlags.error)
                getConsole().error("line $lineNum: found error ${remainingLine}")
            }

            highlightedCode.append(wrappedLine)
            highlightedCode.append("\n")
        }

        //  HL Assembly Tokens
        for (tokenID in tokenList.indices) {
            val token = tokenList[tokenID]
            val line = lines[token.lineLoc.lineID]
            val dryContent = line.substring(token.lineLoc.startIndex, token.lineLoc.endIndex)

            when (token) {
                is Assembly.Token.Comment -> {

                }

                is Assembly.Token.Directive -> {

                }

                is Assembly.Token.Inst -> {

                }

                is Assembly.Token.Label -> {

                }

                is Assembly.Token.DataArea -> {

                }

                is Assembly.Token.Macro -> {

                }

                is Assembly.Token.Var -> {

                }
            }


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