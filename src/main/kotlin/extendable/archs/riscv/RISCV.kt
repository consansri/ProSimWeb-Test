package extendable.archs.riscv

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR

    val REGISTER_WIDTH = 32
    val REGISTER_PC_WIDTH = 32

    val INSTRUCTION_WIDTH = 32

    val MEMORY_WIDTH = 8
    val MEMORY_ADDRESS_WIDTH = 32

    val config = Config(
        "RISC-V",
        arrayOf(
            Register(0, "zero", 0, "hardwired zero", REGISTER_WIDTH),
            Register(1, "ra", 0, "return address", REGISTER_WIDTH),
            Register(2, "sp", 0, "stack pointer", REGISTER_WIDTH),
            Register(3, "gp", 0, "global pointer", REGISTER_WIDTH),
            Register(4, "tp", 0, "thread pointer", REGISTER_WIDTH),
            Register(5, "t0", 0, "temporary register 0", REGISTER_WIDTH),
            Register(6, "t1", 0, "temporary register 1", REGISTER_WIDTH),
            Register(7, "t2", 0, "temporary register 2", REGISTER_WIDTH),
            Register(8, "s0 / fp", 0, "saved register 0 / frame pointer", REGISTER_WIDTH),
            Register(9, "s1", 0, "saved register 1", REGISTER_WIDTH),
            Register(10, "a0", 0, "function argument 0 / return value 0", REGISTER_WIDTH),
            Register(11, "a1", 0, "function argument 1 / return value 1", REGISTER_WIDTH),
            Register(12, "a2", 0, "function argument 2", REGISTER_WIDTH),
            Register(13, "a3", 0, "function argument 3", REGISTER_WIDTH),
            Register(14, "a4", 0, "function argument 4", REGISTER_WIDTH),
            Register(15, "a5", 0, "function argument 5", REGISTER_WIDTH),
            Register(16, "a6", 0, "function argument 6", REGISTER_WIDTH),
            Register(17, "a7", 0, "function argument 7", REGISTER_WIDTH),
            Register(18, "s2", 0, "saved register 2", REGISTER_WIDTH),
            Register(19, "s3", 0, "saved register 3", REGISTER_WIDTH),
            Register(20, "s4", 0, "saved register 4", REGISTER_WIDTH),
            Register(21, "s5", 0, "saved register 5", REGISTER_WIDTH),
            Register(22, "s6", 0, "saved register 6", REGISTER_WIDTH),
            Register(23, "s7", 0, "saved register 7", REGISTER_WIDTH),
            Register(24, "s8", 0, "saved register 8", REGISTER_WIDTH),
            Register(25, "s9", 0, "saved register 9", REGISTER_WIDTH),
            Register(26, "s10", 0, "saved register 10", REGISTER_WIDTH),
            Register(27, "s11", 0, "saved register 11", REGISTER_WIDTH),
            Register(28, "t3", 0, "temporary register 3", REGISTER_WIDTH),
            Register(29, "t4", 0, "temporary register 4", REGISTER_WIDTH),
            Register(30, "t5", 0, "temporary register 5", REGISTER_WIDTH),
            Register(31, "t6", 0, "temporary register 6", REGISTER_WIDTH),
            Register(ArchConst.REGISTER_NOVALUE, "pc", 0, "program counter", REGISTER_PC_WIDTH)
        ),
        listOf(
            Instruction(
                "LUI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0000001001"),
                "rd ← imm",
                "Load Upper Immediate",
                ::lui
            ),
            Instruction(
                "AUIPC",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0000001001"),
                "rd ← pc + offset",
                "Add Upper Immediate to PC",
                ::auipc
            ),
            Instruction(
                "JAL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0000001001"),
                "rd ← pc + length(inst), pc ← pc + offset",
                "Jump and Link",
                ::jal
            ),
            Instruction(
                "JALR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0000001001"),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Jump and Link Register",
                ::jalr
            ),
            Instruction(
                "BEQ",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("00101001"),
                "if rs1 = rs2 then pc ← pc + offset",
                "Branch Equal",
                ::beq
            ),
            Instruction(
                "BNE",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("01001001"),
                "if rs1 ≠ rs2 then pc ← pc + offset",
                "Branch Not Equal",
                ::bne
            ),
            Instruction(
                "BLT",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("01001001"),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than",
                ::blt
            ),
            Instruction(
                "BGE",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("01001001"),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal",
                ::bge
            ),
            Instruction(
                "BLTU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("01001001"),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than Unsigned",
                ::bltu
            ),
            Instruction(
                "BGEU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("01001001"),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal Unsigned",
                ::bgeu
            ),
            Instruction(
                "LB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← s8[rs1 + offset]",
                "Load Byte",
                ::lb
            ),
            Instruction(
                "LH",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← s16[rs1 + offset]",
                "Load Half",
                ::lh
            ),
            Instruction(
                "LW",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← s32[rs1 + offset]",
                "Load Word",
                ::lw
            ),
            Instruction(
                "LBU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← u8[rs1 + offset]",
                "Load Byte Unsigned",
                ::lbu
            ),
            Instruction(
                "LHU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← u16[rs1 + offset]",
                "Load Half Unsigned",
                ::lhu
            ),
            Instruction(
                "SB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "u8[rs1 + offset] ← rs2",
                "Store Byte",
                ::sb
            ),
            Instruction(
                "SH",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "u16[rs1 + offset] ← rs2",
                "Store Half",
                ::sh
            ),
            Instruction(
                "SW",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "u32[rs1 + offset] ← rs2",
                "Store Word",
                ::sw
            ),
            Instruction(
                "ADDI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← rs1 + sx(imm)",
                "Add Immediate",
                ::addi
            ),
            Instruction(
                "SLTI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← sx(rs1) < sx(imm)",
                "Set Less Than Immediate",
                ::slti
            ),
            Instruction(
                "SLTIU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← ux(rs1) < ux(imm)",
                "Set Less Than Immediate Unsigned",
                ::sltiu
            ),
            Instruction(
                "XORI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← ux(rs1) ⊕ ux(imm)",
                "Xor Immediate",
                ::xori
            ),
            Instruction(
                "ORI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← ux(rs1) ∨ ux(imm)",
                "Or Immediate",
                ::ori
            ),
            Instruction(
                "ANDI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("010100100"),
                "rd ← ux(rs1) ∧ ux(imm)",
                "And Immediate",
                ::andi
            ),
            Instruction(
                "SLLI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("010100100"),
                "rd ← ux(rs1) « ux(imm)",
                "Shift Left Logical Immediate",
                ::slli
            ),
            Instruction(
                "SRLI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("010100100"),
                "rd ← ux(rs1) » ux(imm)",
                "Shift Right Logical Immediate",
                ::srli
            ),
            Instruction(
                "SRAI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("010100100"),
                "rd ← sx(rs1) » ux(imm)",
                "Shift Right Arithmetic Immediate",
                ::srai
            ),
            Instruction(
                "ADD",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← sx(rs1) + sx(rs2)",
                "Add",
                ::add
            ),
            Instruction(
                "SUB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← sx(rs1) - sx(rs2)",
                "Substract",
                ::sub
            ),
            Instruction(
                "SLL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) « rs2",
                "Shift Left Logical",
                ::sll
            ),
            Instruction(
                "SLT",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← sx(rs1) < sx(rs2)",
                "Set Less Than",
                ::slt
            ),
            Instruction(
                "SLTU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) < ux(rs2)",
                "Set Less Than Unsigned",
                ::sltu
            ),
            Instruction(
                "XOR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) ⊕ ux(rs2)",
                "Xor",
                ::xor
            ),
            Instruction(
                "SRL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) » rs2",
                "Shift Right Logical",
                ::srl
            ),
            Instruction(
                "SRA",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← sx(rs1) » rs2",
                "Shift Right Arithmetic",
                ::sra
            ),
            Instruction(
                "OR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) ∨ ux(rs2)",
                "Or",
                ::or
            ),
            Instruction(
                "AND",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("010100100"),
                "rd ← ux(rs1) ∧ ux(rs2)",
                "And",
                ::and
            ),
            Instruction(
                "FENCE",
                listOf(ArchConst.EXTYPE_IMMEDIATE),
                OpCode("00101010"),
                "",
                "Fence",
                ::fence
            ),
            Instruction(
                "FENCE.I",
                listOf(),
                OpCode("00101010"),
                "",
                "Fence Instruction",
                ::fencei
            )


        ),
        Memory(MEMORY_ADDRESS_WIDTH, 4),
        Transcript(arrayOf("Address", "Line", "Code", "Labels", "Instruction"))
    )

    // INSTRUCTION LOGIC

    fun lui(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun auipc(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun jal(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun jalr(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun beq(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun bne(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun blt(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun bge(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun bltu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun bgeu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun lb(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun lh(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun lw(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun lbu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun lhu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sb(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sh(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sw(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun addi(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun slti(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sltiu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun xori(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun ori(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun andi(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun slli(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun srli(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun srai(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun add(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {

        for (ex in extensions) {
            when (ex) {
                is TypeAddr -> {

                }

                is TypeCSR -> {

                }

                is TypeImm -> {

                }

                is TypeJAddr -> {

                }

                is TypeReg -> {

                }

                is TypeShift -> {

                }
            }
        }

        for (reg in registers) {
            if (reg.name == "ra") {
                reg.value += 1
            }

        }
        return true
    }

    fun sub(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sll(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun slt(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sltu(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun xor(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun srl(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun sra(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun or(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun and(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun fence(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun fencei(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }


    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}