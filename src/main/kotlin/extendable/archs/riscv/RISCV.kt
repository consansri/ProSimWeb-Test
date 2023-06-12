package extendable.archs.riscv

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*
import kotlin.math.pow

object RISCV {

    // PROCESSOR

    val REGISTER_WIDTH = 32
    val REGISTER_PC_WIDTH = 32

    val INSTRUCTION_WIDTH = 32

    val MEMORY_WIDTH = 8
    val MEMORY_ADDRESS_WIDTH = 32

    // OpMnemonic Labels
    const val OPLBL_SPLIT = "_"
    const val OPLBL_NOVAL = "[noval]"
    const val OPLBL_FUNCT7 = "[funct7]"
    const val OPLBL_OPCODE = "[opcode]"
    const val OPLBL_FUNCT3 = "[funct3]"
    const val OPLBL_IMM = "[imm]"
    const val OPLBL_RS1 = "[rs1]"
    const val OPLBL_RS2 = "[rs2]"
    const val OPLBL_RD = "[rd]"

    // CONFIG
    val config = Config(
        "RISC-V",
        arrayOf(
            Register(Address(0, 32), "zero", 0, "hardwired zero", REGISTER_WIDTH),
            Register(Address(ArchConst.ADDRESS_NOVALUE, 32), "pc", 0, "program counter", REGISTER_PC_WIDTH),
            Register(Address(1, 32), "ra", 0, "return address", REGISTER_WIDTH),
            Register(Address(2, 32), "sp", 0, "stack pointer", REGISTER_WIDTH),
            Register(Address(3, 32), "gp", 0, "global pointer", REGISTER_WIDTH),
            Register(Address(4, 32), "tp", 0, "thread pointer", REGISTER_WIDTH),
            Register(Address(5, 32), "t0", 0, "temporary register 0", REGISTER_WIDTH),
            Register(Address(6, 32), "t1", 0, "temporary register 1", REGISTER_WIDTH),
            Register(Address(7, 32), "t2", 0, "temporary register 2", REGISTER_WIDTH),
            Register(Address(8, 32), "s0 / fp", 0, "saved register 0 / frame pointer", REGISTER_WIDTH),
            Register(Address(9, 32), "s1", 0, "saved register 1", REGISTER_WIDTH),
            Register(Address(10, 32), "a0", 0, "function argument 0 / return value 0", REGISTER_WIDTH),
            Register(Address(11, 32), "a1", 0, "function argument 1 / return value 1", REGISTER_WIDTH),
            Register(Address(12, 32), "a2", 0, "function argument 2", REGISTER_WIDTH),
            Register(Address(13, 32), "a3", 0, "function argument 3", REGISTER_WIDTH),
            Register(Address(14, 32), "a4", 0, "function argument 4", REGISTER_WIDTH),
            Register(Address(15, 32), "a5", 0, "function argument 5", REGISTER_WIDTH),
            Register(Address(16, 32), "a6", 0, "function argument 6", REGISTER_WIDTH),
            Register(Address(17, 32), "a7", 0, "function argument 7", REGISTER_WIDTH),
            Register(Address(18, 32), "s2", 0, "saved register 2", REGISTER_WIDTH),
            Register(Address(19, 32), "s3", 0, "saved register 3", REGISTER_WIDTH),
            Register(Address(20, 32), "s4", 0, "saved register 4", REGISTER_WIDTH),
            Register(Address(21, 32), "s5", 0, "saved register 5", REGISTER_WIDTH),
            Register(Address(22, 32), "s6", 0, "saved register 6", REGISTER_WIDTH),
            Register(Address(23, 32), "s7", 0, "saved register 7", REGISTER_WIDTH),
            Register(Address(24, 32), "s8", 0, "saved register 8", REGISTER_WIDTH),
            Register(Address(25, 32), "s9", 0, "saved register 9", REGISTER_WIDTH),
            Register(Address(26, 32), "s10", 0, "saved register 10", REGISTER_WIDTH),
            Register(Address(27, 32), "s11", 0, "saved register 11", REGISTER_WIDTH),
            Register(Address(28, 32), "t3", 0, "temporary register 3", REGISTER_WIDTH),
            Register(Address(29, 32), "t4", 0, "temporary register 4", REGISTER_WIDTH),
            Register(Address(30, 32), "t5", 0, "temporary register 5", REGISTER_WIDTH),
            Register(Address(31, 32), "t6", 0, "temporary register 6", REGISTER_WIDTH)

        ),
        listOf(
            Instruction(
                "LUI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b0110111", listOf(OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← imm",
                "Load Upper Immediate",
                ::lui
            ),
            Instruction(
                "AUIPC",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b0010111", listOf(OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← pc + offset",
                "Add Upper Immediate to PC",
                ::auipc
            ),
            Instruction(
                "JAL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b1101111", listOf(OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← pc + length(inst), pc ← pc + offset",
                "Jump and Link",
                ::jal
            ),
            Instruction(
                "JALR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b000_00000_1100111", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Jump and Link Register",
                ::jalr
            ),
            Instruction(
                "ECALL",
                listOf(),
                OpCode("0b0000000_00000_00000_000_00000_1110011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Systemaufruf verarbeiten",
                ::ecall
            ),
            Instruction(
                "EBREAK",
                listOf(),
                OpCode("0b0000000_00001_00000_000_00000_1110011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Unterbrechung verarbeiten",
                ::ebreak
            ),

            Instruction(
                "BEQ",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b000_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 = rs2 then pc ← pc + offset",
                "Branch Equal",
                ::beq
            ),
            Instruction(
                "BNE",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b001_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 ≠ rs2 then pc ← pc + offset",
                "Branch Not Equal",
                ::bne
            ),
            Instruction(
                "BLT",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b100_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than",
                ::blt
            ),
            Instruction(
                "BGE",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b101_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal",
                ::bge
            ),
            Instruction(
                "BLTU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b110_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than Unsigned",
                ::bltu
            ),
            Instruction(
                "BGEU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b111_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal Unsigned",
                ::bgeu
            ),
            Instruction(
                "LB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b000_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← s8[rs1 + offset]",
                "Load Byte",
                ::lb
            ),
            Instruction(
                "LH",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b001_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← s16[rs1 + offset]",
                "Load Half",
                ::lh
            ),
            Instruction(
                "LW",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b010_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← s32[rs1 + offset]",
                "Load Word",
                ::lw
            ),
            Instruction(
                "LBU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b100_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← u8[rs1 + offset]",
                "Load Byte Unsigned",
                ::lbu
            ),
            Instruction(
                "LHU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b101_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← u16[rs1 + offset]",
                "Load Half Unsigned",
                ::lhu
            ),
            Instruction(
                "SB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b000_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "u8[rs1 + offset] ← rs2",
                "Store Byte",
                ::sb
            ),
            Instruction(
                "SH",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b001_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "u16[rs1 + offset] ← rs2",
                "Store Half",
                ::sh
            ),
            Instruction(
                "SW",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b010_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPLBL_SPLIT),
                "u32[rs1 + offset] ← rs2",
                "Store Word",
                ::sw
            ),
            Instruction(
                "ADDI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b000_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← rs1 + sx(imm)",
                "Add Immediate",
                ::addi
            ),
            Instruction(
                "SLTI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b010_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) < sx(imm)",
                "Set Less Than Immediate",
                ::slti
            ),
            Instruction(
                "SLTIU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b011_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) < ux(imm)",
                "Set Less Than Immediate Unsigned",
                ::sltiu
            ),
            Instruction(
                "XORI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b100_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ⊕ ux(imm)",
                "Xor Immediate",
                ::xori
            ),
            Instruction(
                "ORI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b110_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ∨ ux(imm)",
                "Or Immediate",
                ::ori
            ),
            Instruction(
                "ANDI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_IMMEDIATE),
                OpCode("0b111_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ∧ ux(imm)",
                "And Immediate",
                ::andi
            ),
            Instruction(
                "SLLI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("0b0000000_00000_00000_001_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) « ux(imm)",
                "Shift Left Logical Immediate",
                ::slli
            ),
            Instruction(
                "SRLI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("0b0000000_00000_00000_101_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) » ux(imm)",
                "Shift Right Logical Immediate",
                ::srli
            ),
            Instruction(
                "SRAI",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_SHIFT),
                OpCode("0b0100000_00000_00000_101_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) » ux(imm)",
                "Shift Right Arithmetic Immediate",
                ::srai
            ),
            Instruction(
                "ADD",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_000_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) + sx(rs2)",
                "Add",
                ::add
            ),
            Instruction(
                "SUB",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0100000_00000_00000_000_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) - sx(rs2)",
                "Substract",
                ::sub
            ),
            Instruction(
                "SLL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_001_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) « rs2",
                "Shift Left Logical",
                ::sll
            ),
            Instruction(
                "SLT",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_010_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) < sx(rs2)",
                "Set Less Than",
                ::slt
            ),
            Instruction(
                "SLTU",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_011_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) < ux(rs2)",
                "Set Less Than Unsigned",
                ::sltu
            ),
            Instruction(
                "XOR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_100_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ⊕ ux(rs2)",
                "Xor",
                ::xor
            ),
            Instruction(
                "SRL",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_101_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) » rs2",
                "Shift Right Logical",
                ::srl
            ),
            Instruction(
                "SRA",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0100000_00000_00000_101_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← sx(rs1) » rs2",
                "Shift Right Arithmetic",
                ::sra
            ),
            Instruction(
                "OR",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_110_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ∨ ux(rs2)",
                "Or",
                ::or
            ),
            Instruction(
                "AND",
                listOf(ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER, ArchConst.EXTYPE_REGISTER),
                OpCode("0b0000000_00000_00000_111_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPLBL_SPLIT),
                "rd ← ux(rs1) ∧ ux(rs2)",
                "And",
                ::and
            )
        ),
        Memory(MEMORY_ADDRESS_WIDTH, 16),
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

    fun ecall(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

    fun ebreak(extensions: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
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
                is TypeBIN -> {}
                is TypeDEC -> {}
                is TypeHEX -> {}
                is TypeLABEL -> {}
            }
        }

        for (reg in registers) {
            if (reg.name == "ra") {
                reg.incValue()
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