package cengine.lang.asm.ast.target.riscv

enum class RVCsrDebug(val alias: String, val description: String) : RVCsr {
    X7B0("dcsr", "Debug control and status register"),
    X7B1("dpc", "Debug PC"),
    X7B2("dscratch0", "Debug scratch register 0"),
    X7B3("dscratch1", "Debug scratch register 1")

    ;

    override val address: UInt = name.removePrefix("X").toUInt(16)

    override val recognizable: List<String> = listOf(alias, name.lowercase())


}