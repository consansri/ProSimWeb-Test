package extendable.archs.riscv

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.Instruction
import extendable.components.connected.Memory
import extendable.components.connected.Register
import extendable.components.connected.Transcript
import extendable.components.types.OpCode

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
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_IMM}",
                OpCode("0000001001"),
                "rd ← imm",
                "Load Upper Immediate",
                ::lui
            ),
            Instruction(
                "AUIPC",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("0000001001"),
                "rd ← pc + offset",
                "Add Upper Immediate to PC",
                ::auipc
            ),
            Instruction(
                "JAL",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("0000001001"),
                "rd ← pc + length(inst), pc ← pc + offset",
                "Jump and Link",
                ::jal
            ),
            Instruction(
                "JALR",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("0000001001"),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Jump and Link Register",
                ::jalr
            ),
            Instruction(
                "BEQ",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("00101001"),
                "if rs1 = rs2 then pc ← pc + offset",
                "Branch Equal",
                ::beq
            ),
            Instruction(
                "BNE",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("01001001"),
                "if rs1 ≠ rs2 then pc ← pc + offset",
                "Branch Not Equal",
                ::bne
            ),
            Instruction(
                "BLT",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("01001001"),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than",
                ::blt
            ),
            Instruction(
                "BGE",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("01001001"),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal",
                ::bge
            ),
            Instruction(
                "BLTU",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("01001001"),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than Unsigned",
                ::bltu
            ),
            Instruction(
                "BGEU",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_OFFSETABS}",
                OpCode("01001001"),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal Unsigned",
                ::bgeu
            ),
            Instruction(
                "LB",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "rd ← s8[rs1 + offset]",
                "Load Byte",
                ::lb
            ),
            Instruction(
                "LH",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "rd ← s16[rs1 + offset]",
                "Load Half",
                ::lh
            ),
            Instruction(
                "LW",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "rd ← s32[rs1 + offset]",
                "Load Word",
                ::lw
            ),
            Instruction(
                "LBU",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "rd ← u8[rs1 + offset]",
                "Load Byte Unsigned",
                ::lbu
            ),
            Instruction(
                "LHU",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "rd ← u16[rs1 + offset]",
                "Load Half Unsigned",
                ::lhu
            ),
            Instruction(
                "SB",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "u8[rs1 + offset] ← rs2",
                "Store Byte",
                ::sb
            ),
            Instruction(
                "SH",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "u16[rs1 + offset] ← rs2",
                "Store Half",
                ::sh
            ),
            Instruction(
                "SW",
                2,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_OFFSETREL}",
                OpCode("010100100"),
                "u32[rs1 + offset] ← rs2",
                "Store Word",
                ::sw
            ),
            Instruction(
                "ADDI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← rs1 + sx(imm)",
                "Add Immediate",
                ::addi
            ),
            Instruction(
                "SLTI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← sx(rs1) < sx(imm)",
                "Set Less Than Immediate",
                ::slti
            ),
            Instruction(
                "SLTIU",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) < ux(imm)",
                "Set Less Than Immediate Unsigned",
                ::sltiu
            ),
            Instruction(
                "XORI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) ⊕ ux(imm)",
                "Xor Immediate",
                ::xori
            ),
            Instruction(
                "ORI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) ∨ ux(imm)",
                "Or Immediate",
                ::ori
            ),
            Instruction(
                "ANDI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) ∧ ux(imm)",
                "And Immediate",
                ::andi
            ),
            Instruction(
                "SLLI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) « ux(imm)",
                "Shift Left Logical Immediate",
                ::slli
            ),
            Instruction(
                "SRLI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← ux(rs1) » ux(imm)",
                "Shift Right Logical Immediate",
                ::srli
            ),
            Instruction(
                "SRAI",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_IMM}",
                OpCode("010100100"),
                "rd ← sx(rs1) » ux(imm)",
                "Shift Right Arithmetic Immediate",
                ::srai
            ),
            Instruction(
                "ADD",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← sx(rs1) + sx(rs2)",
                "Add",
                ::add
            ),
            Instruction(
                "SUB",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← sx(rs1) - sx(rs2)",
                "Substract",
                ::sub
            ),
            Instruction(
                "SLL",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) « rs2",
                "Shift Left Logical",
                ::sll
            ),
            Instruction(
                "SLT",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← sx(rs1) < sx(rs2)",
                "Set Less Than",
                ::slt
            ),
            Instruction(
                "SLTU",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) < ux(rs2)",
                "Set Less Than Unsigned",
                ::sltu
            ),
            Instruction(
                "XOR",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) ⊕ ux(rs2)",
                "Xor",
                ::xor
            ),
            Instruction(
                "SRL",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) » rs2",
                "Shift Right Logical",
                ::srl
            ),
            Instruction(
                "SRA",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← sx(rs1) » rs2",
                "Shift Right Arithmetic",
                ::sra
            ),
            Instruction(
                "OR",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) ∨ ux(rs2)",
                "Or",
                ::or
            ),
            Instruction(
                "AND",
                3,
                "${ArchConst.INSTYPE_INS} ${ArchConst.INSTYPE_REGDEST}, ${ArchConst.INSTYPE_REGSRC}, ${ArchConst.INSTYPE_REGSRC}",
                OpCode("010100100"),
                "rd ← ux(rs1) ∧ ux(rs2)",
                "And",
                ::and
            ),
            Instruction(
                "FENCE",
                3,
                "${ArchConst.INSTYPE_INS} [pred], [succ]",
                OpCode("00101010"),
                "",
                "Fence",
                ::fence
            ),
            Instruction(
                "FENCE.I",
                3,
                "${ArchConst.INSTYPE_INS} ",
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

    fun lui(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun auipc(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun jal(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun jalr(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun beq(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun bne(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun blt(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun bge(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun bltu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun bgeu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun lb(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun lh(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun lw(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun lbu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun lhu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sb(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sh(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sw(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun addi(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun slti(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sltiu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun xori(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun ori(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun andi(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun slli(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun srli(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun srai(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun add(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sub(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sll(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun slt(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sltu(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun xor(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun srl(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun sra(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun or(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun and(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun fence(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }

    fun fencei(extensions: List<String>, mem: Memory, registers: Array<Register>): Boolean {
        return false
    }


    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}