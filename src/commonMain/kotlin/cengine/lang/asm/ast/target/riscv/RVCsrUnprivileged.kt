package cengine.lang.asm.ast.target.riscv

/**
 * Machine-Level CSR
 */
enum class RVCsrUnprivileged(alias: String, val description: String = "") : RVCsr {
    X001("fflags", "Floating-Point Accrued Exceptions"),
    X002("frm", "Floating-Point Dynamic Rounding Mode"),
    X003("fcsr", "Floating-Point Control and Status Register (frm + fflags)"),

    XC00("cycle", "Cycle counter for RDCYCLE instruction"),
    XC01("time", "Timer for RDTIME instruction"),
    XC02("instret", "Instructions-retired counter for RDINSTRET instruction"),

    XC03("hpmcounter3", "Performance-monitoring counter"),
    XC04("hpmcounter4", "Performance-monitoring counter"),
    XC05("hpmcounter5", "Performance-monitoring counter"),
    XC06("hpmcounter6", "Performance-monitoring counter"),
    XC07("hpmcounter7", "Performance-monitoring counter"),
    XC08("hpmcounter8", "Performance-monitoring counter"),
    XC09("hpmcounter9", "Performance-monitoring counter"),
    XC0A("hpmcounter10", "Performance-monitoring counter"),
    XC0B("hpmcounter11", "Performance-monitoring counter"),
    XC0C("hpmcounter12", "Performance-monitoring counter"),
    XC0D("hpmcounter13", "Performance-monitoring counter"),
    XC0E("hpmcounter14", "Performance-monitoring counter"),
    XC0F("hpmcounter15", "Performance-monitoring counter"),
    XC10("hpmcounter16", "Performance-monitoring counter"),
    XC11("hpmcounter17", "Performance-monitoring counter"),
    XC12("hpmcounter18", "Performance-monitoring counter"),
    XC13("hpmcounter19", "Performance-monitoring counter"),
    XC14("hpmcounter20", "Performance-monitoring counter"),
    XC15("hpmcounter21", "Performance-monitoring counter"),
    XC16("hpmcounter22", "Performance-monitoring counter"),
    XC17("hpmcounter23", "Performance-monitoring counter"),
    XC18("hpmcounter24", "Performance-monitoring counter"),
    XC19("hpmcounter25", "Performance-monitoring counter"),
    XC1A("hpmcounter26", "Performance-monitoring counter"),
    XC1B("hpmcounter27", "Performance-monitoring counter"),
    XC1C("hpmcounter28", "Performance-monitoring counter"),
    XC1D("hpmcounter29", "Performance-monitoring counter"),
    XC1E("hpmcounter30", "Performance-monitoring counter"),
    XC1F("hpmcounter31", "Performance-monitoring counter"),
    ;

    override val numericalValue: UInt = name.removePrefix("X").toUInt(16)

    override val recognizable: List<String> = listOf(alias, name.lowercase())

}