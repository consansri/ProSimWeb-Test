package emulator.kit

import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*

object TestObj {

    fun calc() {

        // Beispiel 1
        val a = Hex("0xFE", Bit16())
        val b = Bin("0b11100000", Bit16())
        val c = Dec("-112", Bit16())
        val d = UDec("u40", Bit16())

        a + b - c * d // dec: 4958, hex: 0x135E

        // Beispiel 2
        val bin1 = Bin("10000110", Bit8())
        val bin2 = Bin("01100000", Bit8())

        bin1 + bin2           // bin: 0b11100110
        bin1 + (bin2 shl 1)   // bin: 0b01000110


        Bin("0b11011", Bit5())
        Bin("0b0111001", Bit7())
        Bin("0b010110011100", Bit20()).getUResized(Bit32()) shl 12
        Hex("0xDEADBEEFCAFEAFFEDEADBEEFCAFEAFFE", Bit128())


        Bin("0b11011001")
        Hex("0xD9")
        Dec("-39")
        UDec("u217")


        val imm20 = Bin("0b00111100010100101011", Bit20())
        val shiftedIMM32 = imm20.getUResized(Bit32()) shl 12

        var pc: Value = Hex("0", Bit32())
        pc += Hex("4")

    }

}