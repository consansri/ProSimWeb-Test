package emulator.archs.ikrrisc2

import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*
import emulator.kit.common.RegContainer.Register
import emulator.kit.common.RegContainer.RegisterFile

object IKRRisc2 {

    val BYTE_WIDTH = Bit8()
    val WORD_WIDTH = Bit32()
    val REG_SIZE = Bit5()
    const val REG_INIT = "0"
    
    const val standardRegFileName = "main"
    val standardRegFile = RegisterFile(
        standardRegFileName, arrayOf(
            Register(Bin("00000", REG_SIZE), listOf("r0"), listOf(), Variable(REG_INIT,WORD_WIDTH),"", hardwire = true),
            Register(Bin("00001", REG_SIZE), listOf("r1"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00010", REG_SIZE), listOf("r2"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00011", REG_SIZE), listOf("r3"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00100", REG_SIZE), listOf("r4"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00101", REG_SIZE), listOf("r5"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00110", REG_SIZE), listOf("r6"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("00111", REG_SIZE), listOf("r7"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01000", REG_SIZE), listOf("r8"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01001", REG_SIZE), listOf("r9"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01010", REG_SIZE), listOf("r10"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01011", REG_SIZE), listOf("r11"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01100", REG_SIZE), listOf("r12"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01101", REG_SIZE), listOf("r13"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01110", REG_SIZE), listOf("r14"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("01111", REG_SIZE), listOf("r15"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10000", REG_SIZE), listOf("r16"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10001", REG_SIZE), listOf("r17"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10010", REG_SIZE), listOf("r18"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10011", REG_SIZE), listOf("r19"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10100", REG_SIZE), listOf("r20"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10101", REG_SIZE), listOf("r21"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10110", REG_SIZE), listOf("r22"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("10111", REG_SIZE), listOf("r23"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11000", REG_SIZE), listOf("r24"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11001", REG_SIZE), listOf("r25"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11010", REG_SIZE), listOf("r26"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11011", REG_SIZE), listOf("r27"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11100", REG_SIZE), listOf("r28"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11101", REG_SIZE), listOf("r29"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11110", REG_SIZE), listOf("r30"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
            Register(Bin("11111", REG_SIZE), listOf("r31"), listOf(), Variable(REG_INIT,WORD_WIDTH),""),
        )
    )


    val config = Config(
        Config.Description("IKR RISC-II", "IKR RISC-II", Docs(usingProSimAS = true)),
        fileEnding = "s",
        RegContainer(
            listOf(standardRegFile),
            pcSize = WORD_WIDTH,
            standardRegFileName
        ),
        Memory(WORD_WIDTH,"0", BYTE_WIDTH, endianess = Memory.Endianess.BigEndian)
    )

    val asmConfig = AsmConfig(IKRRisc2Assembler())


}