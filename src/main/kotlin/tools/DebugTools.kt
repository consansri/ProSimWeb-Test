package tools

import extendable.components.connected.IConsole
import extendable.components.types.BinaryTools
import extendable.components.types.ByteValue
import extendable.components.types.DecTools

object DebugTools {

    val divisionLoopLimit: Long? = 255

    const val showTypeConversionInfo = false
    const val showOperatorCalculations = false
    const val showBinaryToolsCalculations = false
    const val showBinaryToolsCalculationDetails = false
    const val showDecToolsCalculations = false
    const val showDecToolsCalculationDetails = false


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
        val bin1 = "111101"
        val bin2 = "011"

        iConsole.log("BinaryTools.sub(): ${bin1} - ${bin2} = ${BinaryTools.sub(bin1, bin2)}")
        iConsole.log("BinaryTools.div(): ${bin1} - ${bin2} = ${BinaryTools.divide(bin1, bin2)}")
        iConsole.log("BinaryTools.inv(): ${bin1} = ${BinaryTools.inv(bin1)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.inv(bin1), ByteValue.Size.Byte())).getDecStr()}")
        iConsole.log(
            "BinaryTools.addWithCarry(): ${bin1} + ${bin2} = ${BinaryTools.addWithCarry(bin1, bin2)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.addWithCarry(bin1, bin2).result, ByteValue.Size.Byte())).getDecStr()} carry: ${
                BinaryTools.addWithCarry(
                    bin1,
                    bin2
                ).carry
            }"
        )
        iConsole.log("BinaryTools.multiply(): ${bin1} * ${bin2} = ${BinaryTools.multiply(bin1, bin2)} | ${ByteValue.Type.Conversion.getDec(ByteValue.Type.Binary(BinaryTools.multiply(bin1, bin2), ByteValue.Size.Byte())).getDecStr()} ")
    }

}