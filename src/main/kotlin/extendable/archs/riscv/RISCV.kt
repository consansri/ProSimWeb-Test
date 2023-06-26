package extendable.archs.riscv

import extendable.ArchConst
import extendable.Architecture
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR
    val REGISTER_PC_WIDTH = 32

    val INSTRUCTION_WIDTH = 32

    val MEMORY_WORD_BYTES = 4
    val MEMORY_ADDRESS_WIDTH = 32
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_BYTES = 4

    // REGEX
    const val PARAM_SPLITTER = ","


    // INS REGEX

    // OpMnemonic Labels
    const val OPSPLIT = "_"
    val OPLBL_NOVAL = OpCode.OpLabel("[noval]", null, true)
    val OPLBL_FUNCT7 = OpCode.OpLabel("[funct7]", null, true)
    val OPLBL_OPCODE = OpCode.OpLabel("[opcode]", null, true)
    val OPLBL_FUNCT3 = OpCode.OpLabel("[funct3]", null, true)
    val OPLBL_IMM = OpCode.OpLabel("[imm]", Instruction.EXT.IMM, false)
    val OPLBL_RS1 = OpCode.OpLabel("[rs1]", Instruction.EXT.REG, false)
    val OPLBL_RS2 = OpCode.OpLabel("[rs2]", Instruction.EXT.REG, false)
    val OPLBL_RD = OpCode.OpLabel("[rd]", Instruction.EXT.REG, false)

    // CONFIG
    val config = Config(
        """RISC-V""",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(Address(ArchConst.ADDRESS_NOVALUE, 32), "pc", ByteValue(REG_INIT, REG_BYTES), "program counter"))
                ),

                RegisterContainer.RegisterFile(
                    RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(
                        RegisterContainer.Register(Address(0, 32), "zero", ByteValue(REG_INIT, REG_BYTES), "hardwired zero"),
                        RegisterContainer.Register(Address(1, 32), "ra", ByteValue(REG_INIT, REG_BYTES), "return address"),
                        RegisterContainer.Register(Address(2, 32), "sp", ByteValue(REG_INIT, REG_BYTES), "stack pointer"),
                        RegisterContainer.Register(Address(3, 32), "gp", ByteValue(REG_INIT, REG_BYTES), "global pointer"),
                        RegisterContainer.Register(Address(4, 32), "tp", ByteValue(REG_INIT, REG_BYTES), "thread pointer"),
                        RegisterContainer.Register(Address(5, 32), "t0", ByteValue(REG_INIT, REG_BYTES), "temporary register 0"),
                        RegisterContainer.Register(Address(6, 32), "t1", ByteValue(REG_INIT, REG_BYTES), "temporary register 1"),
                        RegisterContainer.Register(Address(7, 32), "t2", ByteValue(REG_INIT, REG_BYTES), "temporary register 2"),
                        RegisterContainer.Register(Address(8, 32), "s0 / fp", ByteValue(REG_INIT, REG_BYTES), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(Address(9, 32), "s1", ByteValue(REG_INIT, REG_BYTES), "saved register 1"),
                        RegisterContainer.Register(Address(10, 32), "a0", ByteValue(REG_INIT, REG_BYTES), "function argument 0 / return value 0"),
                        RegisterContainer.Register(Address(11, 32), "a1", ByteValue(REG_INIT, REG_BYTES), "function argument 1 / return value 1"),
                        RegisterContainer.Register(Address(12, 32), "a2", ByteValue(REG_INIT, REG_BYTES), "function argument 2"),
                        RegisterContainer.Register(Address(13, 32), "a3", ByteValue(REG_INIT, REG_BYTES), "function argument 3"),
                        RegisterContainer.Register(Address(14, 32), "a4", ByteValue(REG_INIT, REG_BYTES), "function argument 4"),
                        RegisterContainer.Register(Address(15, 32), "a5", ByteValue(REG_INIT, REG_BYTES), "function argument 5"),
                        RegisterContainer.Register(Address(16, 32), "a6", ByteValue(REG_INIT, REG_BYTES), "function argument 6"),
                        RegisterContainer.Register(Address(17, 32), "a7", ByteValue(REG_INIT, REG_BYTES), "function argument 7"),
                        RegisterContainer.Register(Address(18, 32), "s2", ByteValue(REG_INIT, REG_BYTES), "saved register 2"),
                        RegisterContainer.Register(Address(19, 32), "s3", ByteValue(REG_INIT, REG_BYTES), "saved register 3"),
                        RegisterContainer.Register(Address(20, 32), "s4", ByteValue(REG_INIT, REG_BYTES), "saved register 4"),
                        RegisterContainer.Register(Address(21, 32), "s5", ByteValue(REG_INIT, REG_BYTES), "saved register 5"),
                        RegisterContainer.Register(Address(22, 32), "s6", ByteValue(REG_INIT, REG_BYTES), "saved register 6"),
                        RegisterContainer.Register(Address(23, 32), "s7", ByteValue(REG_INIT, REG_BYTES), "saved register 7"),
                        RegisterContainer.Register(Address(24, 32), "s8", ByteValue(REG_INIT, REG_BYTES), "saved register 8"),
                        RegisterContainer.Register(Address(25, 32), "s9", ByteValue(REG_INIT, REG_BYTES), "saved register 9"),
                        RegisterContainer.Register(Address(26, 32), "s10", ByteValue(REG_INIT, REG_BYTES), "saved register 10"),
                        RegisterContainer.Register(Address(27, 32), "s11", ByteValue(REG_INIT, REG_BYTES), "saved register 11"),
                        RegisterContainer.Register(Address(28, 32), "t3", ByteValue(REG_INIT, REG_BYTES), "temporary register 3"),
                        RegisterContainer.Register(Address(29, 32), "t4", ByteValue(REG_INIT, REG_BYTES), "temporary register 4"),
                        RegisterContainer.Register(Address(30, 32), "t5", ByteValue(REG_INIT, REG_BYTES), "temporary register 5"),
                        RegisterContainer.Register(Address(31, 32), "t6", ByteValue(REG_INIT, REG_BYTES), "temporary register 6")
                    )
                )
            )
        ),
        listOf(
            Instruction(
                "LUI",
                listOf(Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b0110111", listOf(OPLBL_OPCODE), OPSPLIT),
                "rd ← imm",
                "Load Upper Immediate",
                PARAM_SPLITTER,
                ::lui
            ),
            Instruction(
                "AUIPC",
                listOf(Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b0010111", listOf(OPLBL_OPCODE), OPSPLIT),
                "rd ← pc + offset",
                "Add Upper Immediate to PC",
                PARAM_SPLITTER,
                ::auipc
            ),
            Instruction(
                "JAL",
                listOf(Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b1101111", listOf(OPLBL_OPCODE), OPSPLIT),
                "rd ← pc + length(inst), pc ← pc + offset",
                "Jump and Link",
                PARAM_SPLITTER,
                ::jal
            ),
            Instruction(
                "JALR",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b000_00000_1100111", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Jump and Link Register",
                PARAM_SPLITTER,
                ::jalr
            ),
            Instruction(
                "ECALL",
                listOf(),
                OpCode("0b0000000_00000_00000_000_00000_1110011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Systemaufruf verarbeiten",
                PARAM_SPLITTER,
                ::ecall
            ),
            Instruction(
                "EBREAK",
                listOf(),
                OpCode("0b0000000_00001_00000_000_00000_1110011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← pc + length(inst), pc ← (rs1 + offset) ∧ -2",
                "Unterbrechung verarbeiten",
                PARAM_SPLITTER,
                ::ebreak
            ),

            Instruction(
                "BEQ",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b000_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 = rs2 then pc ← pc + offset",
                "Branch Equal",
                PARAM_SPLITTER,
                ::beq
            ),
            Instruction(
                "BNE",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b001_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 ≠ rs2 then pc ← pc + offset",
                "Branch Not Equal",
                PARAM_SPLITTER,
                ::bne
            ),
            Instruction(
                "BLT",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b100_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than",
                PARAM_SPLITTER,
                ::blt
            ),
            Instruction(
                "BGE",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b101_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal",
                PARAM_SPLITTER,
                ::bge
            ),
            Instruction(
                "BLTU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b110_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 < rs2 then pc ← pc + offset",
                "Branch Less Than Unsigned",
                PARAM_SPLITTER,
                ::bltu
            ),
            Instruction(
                "BGEU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b111_00000_1100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "if rs1 ≥ rs2 then pc ← pc + offset",
                "Branch Greater than Equal Unsigned",
                PARAM_SPLITTER,
                ::bgeu
            ),
            Instruction(
                "LB",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b000_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← s8[rs1 + offset]",
                "Load Byte",
                PARAM_SPLITTER,
                ::lb
            ),
            Instruction(
                "LH",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b001_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← s16[rs1 + offset]",
                "Load Half",
                PARAM_SPLITTER,
                ::lh
            ),
            Instruction(
                "LW",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b010_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← s32[rs1 + offset]",
                "Load Word",
                PARAM_SPLITTER,
                ::lw
            ),
            Instruction(
                "LBU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b100_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← u8[rs1 + offset]",
                "Load Byte Unsigned",
                PARAM_SPLITTER,
                ::lbu
            ),
            Instruction(
                "LHU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b101_00000_0000011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← u16[rs1 + offset]",
                "Load Half Unsigned",
                PARAM_SPLITTER,
                ::lhu
            ),
            Instruction(
                "SB",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b000_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "u8[rs1 + offset] ← rs2",
                "Store Byte",
                PARAM_SPLITTER,
                ::sb
            ),
            Instruction(
                "SH",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b001_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "u16[rs1 + offset] ← rs2",
                "Store Half",
                PARAM_SPLITTER,
                ::sh
            ),
            Instruction(
                "SW",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b010_00000_0100011", listOf(OPLBL_FUNCT3, OPLBL_IMM, OPLBL_OPCODE), OPSPLIT),
                "u32[rs1 + offset] ← rs2",
                "Store Word",
                PARAM_SPLITTER,
                ::sw
            ),
            Instruction(
                "ADDI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b000_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← rs1 + sx(imm)",
                "Add Immediate",
                PARAM_SPLITTER,
                ::addi
            ),
            Instruction(
                "SLTI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b010_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) < sx(imm)",
                "Set Less Than Immediate",
                PARAM_SPLITTER,
                ::slti
            ),
            Instruction(
                "SLTIU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b011_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) < ux(imm)",
                "Set Less Than Immediate Unsigned",
                PARAM_SPLITTER,
                ::sltiu
            ),
            Instruction(
                "XORI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b100_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ⊕ ux(imm)",
                "Xor Immediate",
                PARAM_SPLITTER,
                ::xori
            ),
            Instruction(
                "ORI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b110_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ∨ ux(imm)",
                "Or Immediate",
                PARAM_SPLITTER,
                ::ori
            ),
            Instruction(
                "ANDI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.IMM),
                OpCode("0b111_00000_0010011", listOf(OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ∧ ux(imm)",
                "And Immediate",
                PARAM_SPLITTER,
                ::andi
            ),
            Instruction(
                "SLLI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.SHIFT),
                OpCode("0b0000000_00000_00000_001_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) « ux(imm)",
                "Shift Left Logical Immediate",
                PARAM_SPLITTER,
                ::slli
            ),
            Instruction(
                "SRLI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.SHIFT),
                OpCode("0b0000000_00000_00000_101_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) » ux(imm)",
                "Shift Right Logical Immediate",
                PARAM_SPLITTER,
                ::srli
            ),
            Instruction(
                "SRAI",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.SHIFT),
                OpCode("0b0100000_00000_00000_101_00000_0010011", listOf(OPLBL_IMM, OPLBL_NOVAL, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) » ux(imm)",
                "Shift Right Arithmetic Immediate",
                PARAM_SPLITTER,
                ::srai
            ),
            Instruction(
                "ADD",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_000_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) + sx(rs2)",
                "Add",
                PARAM_SPLITTER,
                ::add
            ),
            Instruction(
                "SUB",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0100000_00000_00000_000_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) - sx(rs2)",
                "Substract",
                PARAM_SPLITTER,
                ::sub
            ),
            Instruction(
                "SLL",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_001_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) « rs2",
                "Shift Left Logical",
                PARAM_SPLITTER,
                ::sll
            ),
            Instruction(
                "SLT",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_010_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) < sx(rs2)",
                "Set Less Than",
                PARAM_SPLITTER,
                ::slt
            ),
            Instruction(
                "SLTU",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_011_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) < ux(rs2)",
                "Set Less Than Unsigned",
                PARAM_SPLITTER,
                ::sltu
            ),
            Instruction(
                "XOR",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_100_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ⊕ ux(rs2)",
                "Xor",
                PARAM_SPLITTER,
                ::xor
            ),
            Instruction(
                "SRL",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_101_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) » rs2",
                "Shift Right Logical",
                PARAM_SPLITTER,
                ::srl
            ),
            Instruction(
                "SRA",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0100000_00000_00000_101_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← sx(rs1) » rs2",
                "Shift Right Arithmetic",
                PARAM_SPLITTER,
                ::sra
            ),
            Instruction(
                "OR",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_110_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ∨ ux(rs2)",
                "Or",
                PARAM_SPLITTER,
                ::or
            ),
            Instruction(
                "AND",
                listOf(Instruction.EXT.REG, Instruction.EXT.REG, Instruction.EXT.REG),
                OpCode("0b0000000_00000_00000_111_00000_0110011", listOf(OPLBL_FUNCT7, OPLBL_RS2, OPLBL_RS1, OPLBL_FUNCT3, OPLBL_RD, OPLBL_OPCODE), OPSPLIT),
                "rd ← ux(rs1) ∧ ux(rs2)",
                "And",
                PARAM_SPLITTER,
                ::and
            )
        ),
        Memory(MEMORY_ADDRESS_WIDTH, MEM_INIT, MEMORY_WORD_BYTES),
        Transcript(arrayOf("Address", "Line", "Code", "Labels", "Instruction"))
    )


    // INSTRUCTION LOGIC

    fun lui(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun auipc(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun jal(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun jalr(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun ecall(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun ebreak(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun beq(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun bne(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun blt(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun bge(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun bltu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun bgeu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun lb(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun lh(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun lw(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun lbu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun lhu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sb(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sh(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sw(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun addi(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun slti(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sltiu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun xori(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun ori(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun andi(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun slli(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun srli(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun srai(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun add(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {

        val reg = architecture.getRegisterContainer().getRegister("a1")?.byteValue
        reg?.let {
            reg.setBin(BinaryTools.add(reg.get().toBin().getRawBinaryStr(), "1"))
        }

        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sub(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sll(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun slt(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sltu(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun xor(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun srl(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun sra(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun or(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun and(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun fence(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

    fun fencei(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }


    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"

}