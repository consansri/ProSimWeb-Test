package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable.Value.*
import emulator.archs.t6502.T6502Syntax.AModes.*
import emulator.archs.t6502.T6502.BYTE_SIZE


/**
 * T6502 Syntax
 *
 */
class T6502Syntax : Syntax() {
    override val applyStandardHLForRest: Boolean = false

    override fun clear() {
        // nothing to do here
    }

    override fun check(arch: Architecture, compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {
        TODO("Not yet implemented")
    }

    data object NAMES {
        const val E_INSTR = "e_instr"
    }

    enum class AModes {
        A, // Accumulator: A
        IMP, // Implied: i
        IMM, // Immediate: #
        ABS, // Absolute: a
        REL, // Relative: r
        ABS_INDIRECT, // Absolute Indirect: (a)
        ZP, // Zero Page: zp
        A_AND_X, // Absolute Indexed with X: a,x
        A_AND_Y, // Absolute Indexed with Y: a,y
        ZP_AND_X, // Zero Page Indexed with X: zp,x
        ZP_AND_Y, // Zero Page Indexed with Y: zp,y
        ZP_AND_X_I, // Zero Page Indexed Indirect: (zp,x)
        IZP_AND_Y, // Zero Page Indirect Indexed with Y: (zp),y
    }

    enum class INSTR_TYPE(val opCode: Map<AModes, Hex> = mapOf()) {
        // load, store, interregister transfer
        LDA(
            mapOf(
                ABS to Hex("AD", BYTE_SIZE),
                A_AND_X to Hex("BD", BYTE_SIZE),
                A_AND_Y to Hex("B9", BYTE_SIZE),
                IMM to Hex("A9", BYTE_SIZE),
                ZP to Hex("A5", BYTE_SIZE),
                ZP_AND_X_I to Hex("A1", BYTE_SIZE),
                ZP_AND_X to Hex("B5", BYTE_SIZE),
                IZP_AND_Y to Hex("B1", BYTE_SIZE)
            )
        ),
        LDX(
            mapOf(
                ABS to Hex("AE", BYTE_SIZE),
                A_AND_Y to Hex("BE", BYTE_SIZE),
                IMM to Hex("A2", BYTE_SIZE),
                ZP to Hex("A6", BYTE_SIZE),
                ZP_AND_Y to Hex("B6", BYTE_SIZE)
            )
        ),
        LDY(
            mapOf(
                ABS to Hex("AC", BYTE_SIZE),
                A_AND_X to Hex("BC", BYTE_SIZE),
                IMM to Hex("A0", BYTE_SIZE),
                ZP to Hex("A4", BYTE_SIZE),
                ZP_AND_X to Hex("B4", BYTE_SIZE)
            )
        ),
        STA(
            mapOf(
                ABS to Hex("8D", BYTE_SIZE),
                A_AND_X to Hex("9D", BYTE_SIZE),
                A_AND_Y to Hex("99", BYTE_SIZE),
                ZP to Hex("85", BYTE_SIZE),
                ZP_AND_X_I to Hex("81", BYTE_SIZE),
                ZP_AND_X to Hex("95", BYTE_SIZE),
                IZP_AND_Y to Hex("91", BYTE_SIZE)
            )
        ),
        STX(
            mapOf(
                ABS to Hex("8E", BYTE_SIZE),
                ZP to Hex("86", BYTE_SIZE),
                ZP_AND_Y to Hex("96", BYTE_SIZE)
            )
        ),
        STY(
            mapOf(
                ABS to Hex("8C", BYTE_SIZE),
                ZP to Hex("84", BYTE_SIZE),
                ZP_AND_X to Hex("94", BYTE_SIZE)
            )
        ),
        TAX(mapOf(IMP to Hex("AA", BYTE_SIZE))),
        TAY(mapOf(IMP to Hex("A8", BYTE_SIZE))),
        TSX(mapOf(IMP to Hex("BA", BYTE_SIZE))),
        TXA(mapOf(IMP to Hex("8A", BYTE_SIZE))),
        TXS(mapOf(IMP to Hex("9A", BYTE_SIZE))),
        TYA(mapOf(IMP to Hex("98", BYTE_SIZE))),

        // stack
        PHA,
        PHP,
        PLA,
        PLP,

        // decrements, increments
        DEC(
            mapOf(
                ABS to Hex("CE", BYTE_SIZE),
                A_AND_X to Hex("DE", BYTE_SIZE),
                ZP to Hex("C6", BYTE_SIZE),
                ZP_AND_X to Hex("D6", BYTE_SIZE)
            )
        ),
        DEX(mapOf(IMP to Hex("CA", BYTE_SIZE))),
        DEY(mapOf(IMP to Hex("88", BYTE_SIZE))),
        INC(
            mapOf(
                ABS to Hex("EE", BYTE_SIZE),
                A_AND_X to Hex("FE", BYTE_SIZE),
                ZP to Hex("E6", BYTE_SIZE),
                ZP_AND_X to Hex("F6", BYTE_SIZE)
            )
        ),
        INX(mapOf(IMP to Hex("E8", BYTE_SIZE))),
        INY(mapOf(IMP to Hex("C8", BYTE_SIZE))),

        // arithmetic operations
        ADC(
            mapOf(
                ABS to Hex("6D", BYTE_SIZE),
                A_AND_X to Hex("7D", BYTE_SIZE),
                A_AND_Y to Hex("79", BYTE_SIZE),
                IMM to Hex("69", BYTE_SIZE),
                ZP to Hex("65", BYTE_SIZE),
                ZP_AND_X_I to Hex("61", BYTE_SIZE),
                ZP_AND_X to Hex("75", BYTE_SIZE),
                IZP_AND_Y to Hex("71", BYTE_SIZE)
            )
        ),
        SBC(
            mapOf(
                ABS to Hex("ED", BYTE_SIZE),
                A_AND_X to Hex("FD", BYTE_SIZE),
                A_AND_Y to Hex("F9", BYTE_SIZE),
                IMM to Hex("E9", BYTE_SIZE),
                ZP to Hex("E5", BYTE_SIZE),
                ZP_AND_X_I to Hex("E1", BYTE_SIZE),
                ZP_AND_X to Hex("F5", BYTE_SIZE),
                IZP_AND_Y to Hex("F1", BYTE_SIZE)
            )
        ),

        // logical operations
        AND,
        EOR,
        ORA,

        // shift & rotate
        ASL(
            mapOf(
                ABS to Hex("0E", BYTE_SIZE),
                A_AND_X to Hex("1E", BYTE_SIZE),
                A to Hex("0A", BYTE_SIZE),
                ZP to Hex("06", BYTE_SIZE),
                ZP_AND_X to Hex("16", BYTE_SIZE)
            )
        ),
        LSR(
            mapOf(
                ABS to Hex("4E", BYTE_SIZE),
                A_AND_X to Hex("5E", BYTE_SIZE),
                A to Hex("4A", BYTE_SIZE),
                ZP to Hex("46", BYTE_SIZE),
                ZP_AND_X to Hex("56", BYTE_SIZE)
            )
        ),
        ROL(
            mapOf(
                ABS to Hex("2E", BYTE_SIZE),
                A_AND_X to Hex("3E", BYTE_SIZE),
                A to Hex("2A", BYTE_SIZE),
                ZP to Hex("26", BYTE_SIZE),
                ZP_AND_X to Hex("36", BYTE_SIZE)
            )
        ),
        ROR(
            mapOf(
                ABS to Hex("6E", BYTE_SIZE),
                A_AND_X to Hex("7E", BYTE_SIZE),
                A to Hex("6A", BYTE_SIZE),
                ZP to Hex("66", BYTE_SIZE),
                ZP_AND_X to Hex("76", BYTE_SIZE)
            )
        ),

        // flag
        CLC,
        CLD,
        CLI,
        CLV,
        SEC,
        SED,
        SEI,

        // comparison
        CMP,
        CPX,
        CPY,

        // conditional branches
        BCC,
        BCS,
        BEQ,
        BMI,
        BNE,
        BPL,
        BVC,
        BVS,

        // jumps, subroutines
        JMP,
        JSR,
        RTS,

        // interrupts
        BRK,
        RTI,

        // other
        BIT,
        NOP;

        open fun execute(arch: Architecture) {
            arch.getConsole().log("executing ${this.name}...")
        }
    }

    class E_INSTR(name: Compiler.Token.Word) : TreeNode.ElementNode(highlighting = ConnectedHL(T6502Flags.instr), name = NAMES.E_INSTR, name) {


    }

}