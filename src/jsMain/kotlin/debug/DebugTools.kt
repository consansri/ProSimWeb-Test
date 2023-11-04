package debug

import emulator.kit.common.IConsole
import emulator.kit.types.BinaryTools
import emulator.kit.types.Variable
import emulator.kit.types.DecTools


/**
 * **Settings for Debugging**
 *
 * Mainly holding global booleans to control **console information** on runtime to debug the whole app.
 *
 * **Emulator**
 *
 * **RV32**
 *
 * [RV32_showAsmInfo]
 *
 * [RV32_showBinMapperInfo]
 *
 * [RV32_showOpCodeInfo]
 *
 * [RV32_showGrammarScanTiers]
 *
 * **RV64**
 *
 *
 * **KIT assembly**
 *
 *  [KIT_showCompilerInfo]
 *
 *  [KIT_showAsmInfo]
 *
 * **KIT common**
 *
 *  [KIT_showCheckCodeEvents]
 *
 *  [KIT_showFileHandlerInfo]
 *
 *  [KIT_showMemoryInfo]
 *
 * **KIT configs**
 *
 *
 *
 *
 * **KIT optional**
 *
 *
 * **KIT types**
 *
 *  [KIT_ValBinaryToolsDivisionLoopLimit]
 *
 *  [KIT_showValCheckWarnings]
 *
 *  [KIT_showValTypeConversionInfo]
 *
 *  [KIT_showValOperatorCalculations]
 *
 *  [KIT_showValBinaryToolsCalculations]
 *
 *  [KIT_showValBinaryToolsCalculationDetails]
 *
 *  [KIT_showValDecToolsCalculations]
 *
 *  [KIT_showValDecToolsCalculationDetails]
 *
 * **Visual**
 *
 *  [REACT_showUpdateInfo]
 *
 *  [REACT_deactivateAutoRefreshs]
 *
 */
object DebugTools {

    /*
     * EXTENDABLE
     */

    const val RV32_showAsmInfo = false
    const val RV32_showBinMapperInfo = false
    const val RV32_showOpCodeInfo = false
    const val RV32_showGrammarScanTiers = false

    /*
     * EXTENDABLE
     */

    const val RV64_showAsmInfo = false
    const val RV64_showBinMapperInfo = false
    const val RV64_showOpCodeInfo = false
    const val RV64_showGrammarScanTiers = false



    /*
     * KIT
     */

    const val KIT_showCheckCodeEvents = false
    const val KIT_showFileHandlerInfo = false
    const val KIT_showMemoryInfo = false

    const val KIT_showSyntaxInfo = false
    const val KIT_showCompilerInfo = false
    const val KIT_showAsmInfo = false


    val KIT_ValBinaryToolsDivisionLoopLimit: Long? = null
    const val KIT_showValCheckWarnings = false
    const val KIT_showValTypeConversionInfo = false
    const val KIT_showValOperatorCalculations = false
    const val KIT_showValBinaryToolsCalculations = false
    const val KIT_showValBinaryToolsCalculationDetails = false
    const val KIT_showValDecToolsCalculations = false
    const val KIT_showValDecToolsCalculationDetails = false

    /**
     * **KIT types**
     *
     * For testing multiple functions of the DecTools.
     */
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

    /**
     * **KIT types**
     *
     * For testing multiple functions of the BinaryTools.
     */
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

    /*
     * VISUAL
     */

    const val REACT_showUpdateInfo = true
    const val REACT_deactivateAutoRefreshs = false


}