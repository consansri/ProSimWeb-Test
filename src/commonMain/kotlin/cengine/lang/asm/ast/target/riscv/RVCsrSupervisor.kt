package cengine.lang.asm.ast.target.riscv

enum class RVCsrSupervisor(val alias: String, val description: String) : RVCsr {
    X100("sstatus","Supervisor status register"),
    X104("sie", "Supervisor interrupt-enable register"),
    X105("stvec", "Supervisor trap handler base address"),
    X106("scounteren", "Supervisor counter enable"),
    X10A("senvcfg", "Supervisor environment configuration register"),
    X140("sscratch", "Scratch register for supervisor trap handlers"),
    X141("sepc", "Supervisor exception program counter"),
    X142("scause", "Supervisor trap cause"),
    X143("stval", "Supervisor bad address or instruction"),
    X144("sip", "Supervisor interrupt pending"),
    X180("satp", "Supervisor address translation and protection"),
    X5A8("scontext", "Supervisor-mode context register")

    ;

    override val numericalValue: UInt = name.removePrefix("X").toUInt(16)

    override val recognizable: List<String> = listOf(alias, name.lowercase())

}