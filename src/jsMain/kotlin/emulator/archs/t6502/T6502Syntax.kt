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
        DEC,
        DEX,
        DEY,
        INC,
        INX,
        INY,

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
        ASL,
        LSR,
        ROL,
        ROR,

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

        open fun execute(arch: Architecture){
            arch.getConsole().log("executing ${this.name}...")
        }
    }

    class E_INSTR(name: Compiler.Token.Word) : TreeNode.ElementNode(highlighting = ConnectedHL(T6502Flags.instr), name = NAMES.E_INSTR, name) {


    }

}