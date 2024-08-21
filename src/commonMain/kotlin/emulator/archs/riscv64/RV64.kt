package emulator.archs.riscv64

import cengine.util.integer.Size
import emulator.archs.ArchRV64
import emulator.archs.riscv64.CSRegister.Privilege
import emulator.kit.common.Docs
import emulator.kit.common.Docs.DocComponent.*
import emulator.kit.common.RegContainer
import emulator.kit.common.RegContainer.*
import emulator.kit.memory.*
import emulator.kit.memory.Cache.Setting
import emulator.kit.config.AsmConfig
import emulator.kit.config.Config
import emulator.kit.optional.Feature
import emulator.kit.optional.SetupSetting
import emulator.core.*
import cengine.util.integer.Size.*
import cengine.util.integer.Value.Bin
import cengine.util.integer.Value.Hex

data object RV64 {

    private const val REG_INIT: String = "0"

    val XLEN: Size = Bit64
    val WORD_WIDTH: Size = Bit32
    private val REG_VALUE_SIZE = XLEN
    private val REG_ADDRESS_SIZE = Bit5
    private val CSR_REG_ADDRESS_SIZE = Bit12
    private val MEM_VALUE_WIDTH = Bit8
    val MEM_ADDRESS_WIDTH = XLEN

    const val CSR_REGFILE_NAME = "csr"
    private const val MAIN_REGFILE_NAME = "common"

    /**
     * RV64 Extensions supplied through feature functionality
     */
    enum class EXTENSION(val initialValue: Boolean, val descr: String, val static: Boolean = true, val invisible: Boolean = false, val enables: List<EXTENSION> = emptyList()) {
        CSR(false, "Control and Status Register extension", false),
        A(false, "Atomic Extension"),
        B(false, "Tentatively reserved for Bit-Manipulation extension", invisible = true),
        C(false, "Compressed extension"),
        D(false, "Double-precision floating-point extension"),
        E(false, "RV32E base ISA"),
        F(false, "Single-precision floating-point extension"),
        G(false, "Reserved", invisible = true),
        H(false, "Hypervisor extension"),
        I(true, "RV32I/64I/128I base ISA"),
        J(false, "Tentatively reserved for Dynamically Translated Languages extension", invisible = true),
        K(false, "Reserved", invisible = true),
        L(false, "Reserved", invisible = true),
        M(false, "Integer Multiply/Divide extension", false),
        N(false, "Tentatively reserved for User-Level Interrupts extension", invisible = true),
        O(false, "Reserved", invisible = true),
        P(false, "Tentatively reserved for Packed-SIMD extension", invisible = true),
        Q(false, "Quad-precision floating-point extension"),
        R(false, "Reserved", invisible = true),
        S(false, "Supervisor mode implemented", false, enables = listOf(CSR)),
        T(false, "Reserved", invisible = true),
        U(false, "User mode implemented"),
        V(false, "Tentatively reserved for Vector extenstion", invisible = true),
        W(false, "Reserved", invisible = true),
        X(false, "Non-standard extensions present"),
        Y(false, "Reserved", invisible = true),
        Z(false, "Reserved", invisible = true),
    }

    /**
     * RV64 Generated Documenation
     */
    private val riscVDocs = Docs(
        usingProSimAS = true,
        Docs.DocFile.DefinedFile(
            "RV64 Implemented",
            Chapter(
                "Extensions",
                UnlinkedList(entrys = RV64.EXTENSION.entries.filter { !it.invisible }.map { feature -> Text("${feature.name} (${if (feature.static) "fixed" else "switchable"}): ${feature.descr}") }.toTypedArray())
            ),
            Chapter(
                "Instructions",
                Table(
                    listOf("instruction", "params", "opcode"),
                    contentRows = RV64Syntax.InstrType.entries.map { instr ->
                        listOf(Text(instr.id), Text(instr.paramType.exampleString),
                            Table(
                                header = instr.opCode?.maskLabels?.map { it.name } ?: listOf(),
                                contentRows = arrayOf(instr.opCode?.opMaskList?.map { Text(it) } ?: listOf())))
                    }.toTypedArray()
                )
            ),
            Chapter(
                "Registers",
                Text("address-width: ${REG_ADDRESS_SIZE.bitWidth}bit"),
                Text("value-width: ${REG_VALUE_SIZE.bitWidth}bit")
            ),
            Chapter(
                "Memory",
                Text("address-width: ${MEM_ADDRESS_WIDTH.bitWidth}bit"),
                Text("value-width: ${MEM_VALUE_WIDTH.bitWidth}bit")
            )
        )
    )

    val asmConfig = AsmConfig(
        RV64Assembler,
        features = EXTENSION.entries.map { Feature(it.ordinal, it.name, it.initialValue, it.static, it.invisible, it.descr, it.enables.map { ext -> ext.ordinal }) }
    )

    /**
     * Standard Registers
     */

    val standardRegFile = RegisterFile(
        MAIN_REGFILE_NAME, arrayOf(
            Register(Bin("00000", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), Variable(REG_INIT, REG_VALUE_SIZE), description = "hardwired zero", hardwire = true),
            Register(Bin("00001", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), Variable(REG_INIT, REG_VALUE_SIZE), "return address", CallingConvention.CALLER),
            Register(Bin("00010", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), Variable(REG_INIT, REG_VALUE_SIZE), "stack pointer", CallingConvention.CALLEE),
            Register(Bin("00011", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), Variable(REG_INIT, REG_VALUE_SIZE), description = "global pointer"),
            Register(Bin("00100", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), Variable(REG_INIT, REG_VALUE_SIZE), description = "thread pointer"),

            Register(Bin("00101", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 0", CallingConvention.CALLER),
            Register(Bin("00110", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 1", CallingConvention.CALLER),
            Register(Bin("00111", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 2", CallingConvention.CALLER),
            Register(Bin("11100", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 3", CallingConvention.CALLER),
            Register(Bin("11101", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 4", CallingConvention.CALLER),
            Register(Bin("11110", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 5", CallingConvention.CALLER),
            Register(Bin("11111", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), Variable(REG_INIT, REG_VALUE_SIZE), "temporary register 6", CallingConvention.CALLER),

            Register(Bin("01010", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 0 / return value 0", CallingConvention.CALLER),
            Register(Bin("01011", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 1 / return value 1", CallingConvention.CALLER),
            Register(Bin("01100", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 2", CallingConvention.CALLER),
            Register(Bin("01101", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 3", CallingConvention.CALLER),
            Register(Bin("01110", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 4", CallingConvention.CALLER),
            Register(Bin("01111", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 5", CallingConvention.CALLER),
            Register(Bin("10000", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 6", CallingConvention.CALLER),
            Register(Bin("10001", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), Variable(REG_INIT, REG_VALUE_SIZE), "function argument 7", CallingConvention.CALLER),

            Register(Bin("01000", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 0 / frame pointer", CallingConvention.CALLEE),
            Register(Bin("01001", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 1", CallingConvention.CALLEE),
            Register(Bin("10010", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 2", CallingConvention.CALLEE),
            Register(Bin("10011", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 3", CallingConvention.CALLEE),
            Register(Bin("10100", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 4", CallingConvention.CALLEE),
            Register(Bin("10101", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 5", CallingConvention.CALLEE),
            Register(Bin("10110", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 6", CallingConvention.CALLEE),
            Register(Bin("10111", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 7", CallingConvention.CALLEE),
            Register(Bin("11000", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 8", CallingConvention.CALLEE),
            Register(Bin("11001", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 9", CallingConvention.CALLEE),
            Register(Bin("11010", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 10", CallingConvention.CALLEE),
            Register(Bin("11011", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), Variable(REG_INIT, REG_VALUE_SIZE), "saved register 11", CallingConvention.CALLEE),

            )
    )

    /**
     * CSR Registers
     */
    private val csrUnprivileged = arrayOf(
        // Unprivileged Floating-Point CSRs
        CSRegister(Hex("001", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x001"), listOf("fflags"), Variable(REG_INIT, XLEN), "Floating-Point Accrued Exceptions.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("002", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x002"), listOf("frm"), Variable(REG_INIT, XLEN), "Floating-Point Dynamic Rounding Mode.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("003", CSR_REG_ADDRESS_SIZE), Privilege.URW, listOf("x003"), listOf("fcsr"), Variable(REG_INIT, XLEN), "Floating-Point Control and Status Register (frm + fflags).", listOf(EXTENSION.CSR.ordinal)),

        // Unprivileged Counter/Timers
        CSRegister(Hex("C00", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC00"), listOf("cycle"), Variable(REG_INIT, XLEN), "Cycle counter for RDCYCLE instruction.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C01", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC01"), listOf("time"), Variable(REG_INIT, XLEN), "Timer for RDTIME instruction.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C02", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC02"), listOf("instret"), Variable(REG_INIT, XLEN), "Instructions-retired counter for RDINSTRET instruction.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C03", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC03"), listOf("hpmcounter3"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C04", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC04"), listOf("hpmcounter4"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C05", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC05"), listOf("hpmcounter5"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C06", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC06"), listOf("hpmcounter6"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C07", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC07"), listOf("hpmcounter7"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C08", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC08"), listOf("hpmcounter8"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C09", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC09"), listOf("hpmcounter9"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0A", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0A"), listOf("hpmcounter10"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0B", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0B"), listOf("hpmcounter11"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0C", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0C"), listOf("hpmcounter12"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0D", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0D"), listOf("hpmcounter13"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0E", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0E"), listOf("hpmcounter14"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C0F", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC0F"), listOf("hpmcounter15"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C10", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC10"), listOf("hpmcounter16"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C11", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC11"), listOf("hpmcounter17"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C12", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC12"), listOf("hpmcounter18"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C13", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC13"), listOf("hpmcounter19"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C14", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC14"), listOf("hpmcounter20"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C15", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC15"), listOf("hpmcounter21"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C16", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC16"), listOf("hpmcounter22"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C17", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC17"), listOf("hpmcounter23"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C18", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC18"), listOf("hpmcounter24"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C19", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC19"), listOf("hpmcounter25"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1A", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1A"), listOf("hpmcounter26"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1B", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1B"), listOf("hpmcounter27"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1C", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1C"), listOf("hpmcounter28"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1D", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1D"), listOf("hpmcounter29"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1E", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1E"), listOf("hpmcounter30"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("C1F", CSR_REG_ADDRESS_SIZE), Privilege.URO, listOf("xC1F"), listOf("hpmcounter31"), Variable(REG_INIT, XLEN), "Performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)), // Needs to be extended for RV32I
    )
    private val csrDebug = arrayOf(
        // Debug Mode Registers
        CSRegister(Hex("7B0", CSR_REG_ADDRESS_SIZE), Privilege.DRW, listOf("x7B0"), listOf("dcsr"), Variable(REG_INIT, XLEN), "Debug control and status register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7B1", CSR_REG_ADDRESS_SIZE), Privilege.DRW, listOf("x7B1"), listOf("dpc"), Variable(REG_INIT, XLEN), "Debug PC.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7B2", CSR_REG_ADDRESS_SIZE), Privilege.DRW, listOf("x7B2"), listOf("dscratch0"), Variable(REG_INIT, XLEN), "Debug scratch register 0.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7B3", CSR_REG_ADDRESS_SIZE), Privilege.DRW, listOf("x7B3"), listOf("dscratch1"), Variable(REG_INIT, XLEN), "Debug scratch register 1.", listOf(EXTENSION.CSR.ordinal)),
    )
    private val csrMachine = arrayOf(
        // Machine-Level CSR
        // Machine Information Registers
        CSRegister(Hex("F11", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF11"), listOf("mvendorid"), Variable(REG_INIT, XLEN), "Vendor ID.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("F12", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF12"), listOf("marchid"), Variable(REG_INIT, XLEN), "Architecture ID.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("F13", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF13"), listOf("mimpid"), Variable(REG_INIT, XLEN), "Implementation ID.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("F14", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF14"), listOf("mhartid"), Variable(REG_INIT, XLEN), "Hardware thread ID.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("F15", CSR_REG_ADDRESS_SIZE), Privilege.MRO, listOf("xF15"), listOf("mconfigptr"), Variable(REG_INIT, XLEN), "Pointer to configuration data structure.", listOf(EXTENSION.CSR.ordinal)),
        // Machine Trap Setup
        CSRegister(Hex("300", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x300"), listOf("mstatus"), Variable(REG_INIT, XLEN), "Machine status register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("301", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x301"), listOf("misa"), Variable(REG_INIT, XLEN), "ISA and extensions.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("302", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x302"), listOf("medeleg"), Variable(REG_INIT, XLEN), "Machine exception delegation register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("303", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x303"), listOf("mideleg"), Variable(REG_INIT, XLEN), "Machine interrupt delegation register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("304", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x304"), listOf("mie"), Variable(REG_INIT, XLEN), "Machine interrupt-enable register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("305", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x305"), listOf("mtvec"), Variable(REG_INIT, XLEN), "Machine trap-handler base address.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("306", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x306"), listOf("mcounteren"), Variable(REG_INIT, XLEN), "Machine counter enable.", listOf(EXTENSION.CSR.ordinal)),
        // RV32 Only CSRegister(Hex("310", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x310"), listOf("mstatush"), Variable(REG_INIT, XLEN), "Additional machine status register. (RV32)"),
        // Machine Trap Handling
        CSRegister(Hex("340", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x340"), listOf("mscratch"), Variable(REG_INIT, XLEN), "Scratch register for machine trap handlers.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("341", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x341"), listOf("mepc"), Variable(REG_INIT, XLEN), "Machine exception program counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("342", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x342"), listOf("mcause"), Variable(REG_INIT, XLEN), "Machine trap cause.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("343", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x343"), listOf("mtval"), Variable(REG_INIT, XLEN), "Machine bad address or instruction.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("344", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x344"), listOf("mip"), Variable(REG_INIT, XLEN), "Machine interrupt pending.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("34A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x34A"), listOf("mtinst"), Variable(REG_INIT, XLEN), "Machine trap instruction (transformed).", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("34B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x34B"), listOf("mtval2"), Variable(REG_INIT, XLEN), "Machine bad guest physical address.", listOf(EXTENSION.CSR.ordinal)),
        // Machine Configuration
        CSRegister(Hex("30A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x30A"), listOf("menvcfg"), Variable(REG_INIT, XLEN), "Machine environment configuration register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("747", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x747"), listOf("mseccfg"), Variable(REG_INIT, XLEN), "Machine security configuration register.", listOf(EXTENSION.CSR.ordinal)),
        // Machine Memory Protection
        CSRegister(Hex("3A0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A0"), listOf("pmpcfg0"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)), // needs to be extended for RV32
        CSRegister(Hex("3A2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A2"), listOf("pmpcfg2"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3A4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A4"), listOf("pmpcfg4"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3A6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A6"), listOf("pmpcfg6"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3A8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3A8"), listOf("pmpcfg8"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3AA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AA"), listOf("pmpcfg10"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3AC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AC"), listOf("pmpcfg12"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3AE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3AE"), listOf("pmpcfg14"), Variable(REG_INIT, XLEN), "Physical memory protection configuration.", listOf(EXTENSION.CSR.ordinal)),

        CSRegister(Hex("3B0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B0"), listOf("pmpaddr0"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B1"), listOf("pmpaddr1"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B2"), listOf("pmpaddr2"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B3"), listOf("pmpaddr3"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B4"), listOf("pmpaddr4"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B5"), listOf("pmpaddr5"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B6"), listOf("pmpaddr6"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B7"), listOf("pmpaddr7"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B8"), listOf("pmpaddr8"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3B9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3B9"), listOf("pmpaddr9"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BA"), listOf("pmpaddr10"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BB"), listOf("pmpaddr11"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BC"), listOf("pmpaddr12"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BD"), listOf("pmpaddr13"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BE"), listOf("pmpaddr14"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3BF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3BF"), listOf("pmpaddr15"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),

        CSRegister(Hex("3C0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C0"), listOf("pmpaddr16"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C1"), listOf("pmpaddr17"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C2"), listOf("pmpaddr18"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C3"), listOf("pmpaddr19"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C4"), listOf("pmpaddr20"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C5"), listOf("pmpaddr21"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C6"), listOf("pmpaddr22"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C7"), listOf("pmpaddr23"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C8"), listOf("pmpaddr24"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3C9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3C9"), listOf("pmpaddr25"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CA"), listOf("pmpaddr26"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CB"), listOf("pmpaddr27"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CC"), listOf("pmpaddr28"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CD"), listOf("pmpaddr29"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CE"), listOf("pmpaddr30"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3CF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3CF"), listOf("pmpaddr31"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),

        CSRegister(Hex("3D0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D0"), listOf("pmpaddr32"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D1"), listOf("pmpaddr33"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D2"), listOf("pmpaddr34"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D3"), listOf("pmpaddr35"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D4"), listOf("pmpaddr36"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D5"), listOf("pmpaddr37"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D6"), listOf("pmpaddr38"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D7"), listOf("pmpaddr39"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D8"), listOf("pmpaddr40"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3D9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3D9"), listOf("pmpaddr41"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DA"), listOf("pmpaddr42"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DB"), listOf("pmpaddr43"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DC"), listOf("pmpaddr44"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DD", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DD"), listOf("pmpaddr45"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DE"), listOf("pmpaddr46"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3DF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3DF"), listOf("pmpaddr47"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),

        CSRegister(Hex("3E0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E0"), listOf("pmpaddr48"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E1"), listOf("pmpaddr49"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E2"), listOf("pmpaddr50"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E3"), listOf("pmpaddr51"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E4", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E4"), listOf("pmpaddr52"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E5", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E5"), listOf("pmpaddr53"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E6", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E6"), listOf("pmpaddr54"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E7", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E7"), listOf("pmpaddr55"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E8"), listOf("pmpaddr56"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3E9", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3E9"), listOf("pmpaddr57"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3EA", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EA"), listOf("pmpaddr58"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3EB", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EB"), listOf("pmpaddr59"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3EC", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EC"), listOf("pmpaddr60"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3ED", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3ED"), listOf("pmpaddr61"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3EE", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EE"), listOf("pmpaddr62"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("3EF", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x3EF"), listOf("pmpaddr63"), Variable(REG_INIT, XLEN), "Physical memory protection address register.", listOf(EXTENSION.CSR.ordinal)),
        // Machine Counter/Timers
        CSRegister(Hex("B00", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB00"), listOf("mcycle"), Variable(REG_INIT, XLEN), "Machine cycle counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B02", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB02"), listOf("minstret"), Variable(REG_INIT, XLEN), "Machine instructions-retired counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B03", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB03"), listOf("mhpmcounter3"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B04", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB04"), listOf("mhpmcounter4"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B05", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB05"), listOf("mhpmcounter5"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B06", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB06"), listOf("mhpmcounter6"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B07", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB07"), listOf("mhpmcounter7"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B08", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB08"), listOf("mhpmcounter8"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B09", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB09"), listOf("mhpmcounter9"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0A"), listOf("mhpmcounter10"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0B"), listOf("mhpmcounter11"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0C", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0C"), listOf("mhpmcounter12"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0D", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0D"), listOf("mhpmcounter13"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0E", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0E"), listOf("mhpmcounter14"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B0F", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB0F"), listOf("mhpmcounter15"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B10", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB10"), listOf("mhpmcounter16"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B11", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB11"), listOf("mhpmcounter17"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B12", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB12"), listOf("mhpmcounter18"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B13", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB13"), listOf("mhpmcounter19"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B14", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB14"), listOf("mhpmcounter20"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B15", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB15"), listOf("mhpmcounter21"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B16", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB16"), listOf("mhpmcounter22"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B17", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB17"), listOf("mhpmcounter23"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B18", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB18"), listOf("mhpmcounter24"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B19", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB19"), listOf("mhpmcounter25"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1A"), listOf("mhpmcounter26"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1B"), listOf("mhpmcounter27"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1C", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1C"), listOf("mhpmcounter28"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1D", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1D"), listOf("mhpmcounter29"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1E", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1E"), listOf("mhpmcounter30"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("B1F", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("xB1F"), listOf("mhpmcounter31"), Variable(REG_INIT, XLEN), "Machine performance-monitoring counter.", listOf(EXTENSION.CSR.ordinal)),// needs to be extended for RV32
        // Machine Counter Setup
        CSRegister(Hex("320", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x320"), listOf("mcountinhibit"), Variable(REG_INIT, XLEN), "Machine counter-inhibit register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("323", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x323"), listOf("mhpmevent3"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("324", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x324"), listOf("mhpmevent4"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("325", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x325"), listOf("mhpmevent5"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("326", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x326"), listOf("mhpmevent6"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("327", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x327"), listOf("mhpmevent7"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("328", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x328"), listOf("mhpmevent8"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("329", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x329"), listOf("mhpmevent9"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32A"), listOf("mhpmevent10"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32B"), listOf("mhpmevent11"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32C", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32C"), listOf("mhpmevent12"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32D", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32D"), listOf("mhpmevent13"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32E", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32E"), listOf("mhpmevent14"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("32F", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x32F"), listOf("mhpmevent15"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("330", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x330"), listOf("mhpmevent16"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("331", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x331"), listOf("mhpmevent17"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("332", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x332"), listOf("mhpmevent18"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("333", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x333"), listOf("mhpmevent19"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("334", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x334"), listOf("mhpmevent20"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("335", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x335"), listOf("mhpmevent21"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("336", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x336"), listOf("mhpmevent22"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("337", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x337"), listOf("mhpmevent23"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("338", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x338"), listOf("mhpmevent24"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("339", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x339"), listOf("mhpmevent25"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33A", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33A"), listOf("mhpmevent26"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33B", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33B"), listOf("mhpmevent27"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33C", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33C"), listOf("mhpmevent28"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33D", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33D"), listOf("mhpmevent29"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33E", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33E"), listOf("mhpmevent30"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("33F", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x33F"), listOf("mhpmevent31"), Variable(REG_INIT, XLEN), "Machine performance-monitoring event selector.", listOf(EXTENSION.CSR.ordinal)),
        // Debug/Trace Registers (shared with Debug Mode)
        CSRegister(Hex("7A0", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x7A0"), listOf("tselect"), Variable(REG_INIT, XLEN), "Debug/Trace trigger register select.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7A1", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x7A1"), listOf("tdata1"), Variable(REG_INIT, XLEN), "First Debug/Trace trigger data register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7A2", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x7A2"), listOf("tdata2"), Variable(REG_INIT, XLEN), "Second Debug/Trace trigger data register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7A3", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x7A3"), listOf("tdata3"), Variable(REG_INIT, XLEN), "Third Debug/Trace trigger data register.", listOf(EXTENSION.CSR.ordinal)),
        CSRegister(Hex("7A8", CSR_REG_ADDRESS_SIZE), Privilege.MRW, listOf("x7A8"), listOf("mcontext"), Variable(REG_INIT, XLEN), "Machine-mode context register.", listOf(EXTENSION.CSR.ordinal)),
    )
    private val csrSupervisor = arrayOf(
        // Supervisor Trap Setup
        CSRegister(Hex("100", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x100"), listOf("sstatus"), Variable(REG_INIT, XLEN), "Supervisor status register.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("104", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x104"), listOf("sie"), Variable(REG_INIT, XLEN), "Supervisor interrupt-enable register.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("105", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x105"), listOf("stvec"), Variable(REG_INIT, XLEN), "Supervisor trap handler base address.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("106", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x106"), listOf("scounteren"), Variable(REG_INIT, XLEN), "Supervisor counter enable.", listOf(EXTENSION.S.ordinal)),
        // Supervisor Configuration
        CSRegister(Hex("10A", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x10A"), listOf("senvcfg"), Variable(REG_INIT, XLEN), "Supervisor environment configuration register.", listOf(EXTENSION.S.ordinal)),
        // Supervisor Trap Handling
        CSRegister(Hex("140", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x140"), listOf("sscratch"), Variable(REG_INIT, XLEN), "Scratch register for supervisor trap handlers.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("141", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x141"), listOf("sepc"), Variable(REG_INIT, XLEN), "Supervisor exception program counter.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("142", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x142"), listOf("scause"), Variable(REG_INIT, XLEN), "Supervisor trap cause.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("143", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x143"), listOf("stval"), Variable(REG_INIT, XLEN), "Supervisor bad address or instruction.", listOf(EXTENSION.S.ordinal)),
        CSRegister(Hex("144", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x144"), listOf("sip"), Variable(REG_INIT, XLEN), "Supervisor interrupt pending.", listOf(EXTENSION.S.ordinal)),
        // Supervisor Protection and Translation
        CSRegister(Hex("180", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x180"), listOf("satp"), Variable(REG_INIT, XLEN), "Supervisor address translation and protection.", listOf(EXTENSION.S.ordinal)),
        // Debug/Trace Registers
        CSRegister(Hex("5A8", CSR_REG_ADDRESS_SIZE), Privilege.SRW, listOf("x5A8"), listOf("scontext"), Variable(REG_INIT, XLEN), "Supervisor-mode context register.", listOf(EXTENSION.S.ordinal)),
    )

    private val csrRegFile = RegisterFile(CSR_REGFILE_NAME, arrayOf(*csrUnprivileged, *csrDebug, *csrMachine, *csrSupervisor), hasPrivileges = true)

    /**
     * Configuration
     */
    val settings = listOf(
        SetupSetting.Enumeration("Instruction Cache", Setting.entries, Setting.NONE) { arch, setting ->
            if (arch is ArchRV64) {
                arch.instrMemory = when (setting.get()) {
                    Setting.NONE -> arch.memory
                    Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Instruction")
                    Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                    Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console,4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Instruction")
                    Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Instruction")
                    Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Instruction")
                }
            }
        },
        SetupSetting.Enumeration("Data Cache", Setting.entries, Setting.NONE) { arch, setting ->
            if (arch is ArchRV64) {
                arch.dataMemory = when (setting.get()) {
                    Setting.NONE -> arch.memory
                    Setting.DirectedMapped -> DMCache(arch.memory, arch.console, CacheSize.KiloByte_32, "Data")
                    Setting.FullAssociativeRandom -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Setting.FullAssociativeLRU -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Setting.FullAssociativeFIFO -> FACache(arch.memory, arch.console, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                    Setting.SetAssociativeRandom -> SACache(arch.memory, arch.console,4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.RANDOM, "Data")
                    Setting.SetAssociativeLRU -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.LRU, "Data")
                    Setting.SetAssociativeFIFO -> SACache(arch.memory, arch.console, 4, CacheSize.KiloByte_32, Cache.Model.ReplaceAlgo.FIFO, "Data")
                }
            }
        }
    )

    val config = Config(
        Config.Description("RV64I", "RISC-V 64Bit", riscVDocs),
        fileEnding = "s",
        RegContainer(
            mutableListOf(standardRegFile, csrRegFile),
            pcSize = REG_VALUE_SIZE,
            standardRegFileName = MAIN_REGFILE_NAME
        ),
        MainMemory(MEM_ADDRESS_WIDTH, MEM_VALUE_WIDTH, Memory.Endianess.LittleEndian),
        settings
    )



}