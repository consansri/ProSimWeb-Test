package tools

import emulator.kit.common.IConsole
import emulator.kit.types.BinaryTools
import emulator.kit.types.Variable
import emulator.kit.types.DecTools

object DebugTools {


    // ARCH

    //      RISCV
    const val RISCV_showAsmInfo = false
    const val RISCV_showBinMapperInfo = false
    const val RISCV_showOpCodeInfo = false
    const val RISCV_showGrammarScanTiers = false

    //      RISCII

    //      MINI

    //      CISC


    // ARCH COMPONENTS
    //      Main
    const val ARCH_showCheckCodeEvents = false
    const val ARCH_showFileHandlerInfo = false

    //      Compiler
    const val ARCH_showCompilerInfo = false
    const val ARCH_showAsmInfo = false

    //      Memory
    const val ARCH_showMemoryInfo = false

    //      ByteValue
    val ARCH_BVDivisionLoopLimit: Long? = null

    const val ARCH_showBVCheckWarnings = false
    const val ARCH_showBVTypeConversionInfo = false
    const val ARCH_showBVOperatorCalculations = false
    const val ARCH_showBVBinaryToolsCalculations = false
    const val ARCH_showBVBinaryToolsCalculationDetails = false
    const val ARCH_showBVDecToolsCalculations = false
    const val ARCH_showBVDecToolsCalculationDetails = false

    fun testDecTools(iConsole: IConsole) {
        val dec1 = "12"
        val dec2 = "-3"

        iConsole.log("DecTools.add(): ${dec1} + ${dec2} = ${DecTools.add(dec1, dec2)}")
        iConsole.log("DecTools.sub(): ${dec1} - ${dec2} = ${DecTools.sub(dec1, dec2)}")
        iConsole.log("DecTools.multiply(): ${dec1} * ${dec2} = ${DecTools.multiply(dec1, dec2)}")
        iConsole.log("DecTools.pow(): ${dec1} ^ ${dec2} = ${DecTools.pow(dec1, dec2)}")
        iConsole.log("DecTools.divide(): ${dec1} / ${dec2} = ${DecTools.divide(dec1, dec2)}")

        iConsole.log("DecTools.abs(): ${dec1} -> ${DecTools.abs(dec1)}")
        iConsole.log("DecTools.negotiate(): ${dec1} -> ${DecTools.negotiate(dec1)}")

        iConsole.log("DecTools.isGreaterEqualThan(): ${dec1} >= ${dec2} ? ${DecTools.isGreaterEqualThan(dec1, dec2)}")
        iConsole.log("DecTools.isGreaterThan(): ${dec1} > ${dec2} ? ${DecTools.isGreaterThan(dec1, dec2)}")
        iConsole.log("DecTools.isEqual(): ${dec1} = ${dec2} ? ${DecTools.isEqual(dec1, dec2)}")
    }

    fun testBinTools(iConsole: IConsole) {
        val bin1 = "11001010111111101010000000000000"
        val bin2 = "11111111111111111111111111111110"

        iConsole.log("BinaryTools.sub(): ${bin1} - ${bin2} = ${BinaryTools.sub(bin1, bin2)}")
        iConsole.log("BinaryTools.div(): ${bin1} - ${bin2} = ${BinaryTools.divide(bin1, bin2)}")
        iConsole.log("BinaryTools.inv(): ${bin1} = ${BinaryTools.inv(bin1)} | ${Variable.Value.Conversion.getDec(Variable.Value.Bin(BinaryTools.inv(bin1), Variable.Size.Bit8())).getDecStr()}")
        iConsole.log(
            "BinaryTools.addWithCarry(): ${bin1} + ${bin2} = ${BinaryTools.addWithCarry(bin1, bin2)} | ${Variable.Value.Conversion.getDec(Variable.Value.Bin(BinaryTools.addWithCarry(bin1, bin2).result, Variable.Size.Bit8())).getDecStr()} carry: ${
                BinaryTools.addWithCarry(
                    bin1,
                    bin2
                ).carry
            }"
        )
        iConsole.log("BinaryTools.multiply(): ${bin1} * ${bin2} = ${BinaryTools.multiply(bin1, bin2)} | ${Variable.Value.Conversion.getDec(Variable.Value.Bin(BinaryTools.multiply(bin1, bin2), Variable.Size.Bit8())).getDecStr()} ")
    }


    // React Components
    const val REACT_showUpdateInfo = false
    const val REACT_deactivateAutoRefreshs = false


}