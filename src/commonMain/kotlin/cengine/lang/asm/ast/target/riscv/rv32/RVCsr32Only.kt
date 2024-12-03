package cengine.lang.asm.ast.target.riscv.rv32

import cengine.lang.asm.ast.target.riscv.RVCsr

enum class RVCsr32Only(val alias: String, val description: String): RVCsr {
    XC80("cycleh", "Upper 32 bits of cycle, RV32I only"),
    XC81("timeh", "Upper 32 bits of time, RV32I only"),
    XC82("instreth", "Upper 32 bits of instret, RV32I only"),
    XC83("hpmcounter3h", "Upper 32 bits of hpmcounter3, RV32I only"),
    XC84("hpmcounter4h", "Upper 32 bits of hpmcounter4, RV32I only"),
    XC85("hpmcounter5h", "Upper 32 bits of hpmcounter5, RV32I only"),
    XC86("hpmcounter6h", "Upper 32 bits of hpmcounter6, RV32I only"),
    XC87("hpmcounter7h", "Upper 32 bits of hpmcounter7, RV32I only"),
    XC88("hpmcounter8h", "Upper 32 bits of hpmcounter8, RV32I only"),
    XC89("hpmcounter9h", "Upper 32 bits of hpmcounter9, RV32I only"),
    XC8A("hpmcounter10h", "Upper 32 bits of hpmcounter10, RV32I only"),
    XC8B("hpmcounter11h", "Upper 32 bits of hpmcounter11, RV32I only"),
    XC8C("hpmcounter12h", "Upper 32 bits of hpmcounter12, RV32I only"),
    XC8D("hpmcounter13h", "Upper 32 bits of hpmcounter13, RV32I only"),
    XC8E("hpmcounter14h", "Upper 32 bits of hpmcounter14, RV32I only"),
    XC8F("hpmcounter15h", "Upper 32 bits of hpmcounter15, RV32I only"),
    XC90("hpmcounter16h", "Upper 32 bits of hpmcounter16, RV32I only"),
    XC91("hpmcounter17h", "Upper 32 bits of hpmcounter17, RV32I only"),
    XC92("hpmcounter18h", "Upper 32 bits of hpmcounter18, RV32I only"),
    XC93("hpmcounter19h", "Upper 32 bits of hpmcounter19, RV32I only"),
    XC94("hpmcounter20h", "Upper 32 bits of hpmcounter20, RV32I only"),
    XC95("hpmcounter21h", "Upper 32 bits of hpmcounter21, RV32I only"),
    XC96("hpmcounter22h", "Upper 32 bits of hpmcounter22, RV32I only"),
    XC97("hpmcounter23h", "Upper 32 bits of hpmcounter23, RV32I only"),
    XC98("hpmcounter24h", "Upper 32 bits of hpmcounter24, RV32I only"),
    XC99("hpmcounter25h", "Upper 32 bits of hpmcounter25, RV32I only"),
    XC9A("hpmcounter26h", "Upper 32 bits of hpmcounter26, RV32I only"),
    XC9B("hpmcounter27h", "Upper 32 bits of hpmcounter27, RV32I only"),
    XC9C("hpmcounter28h", "Upper 32 bits of hpmcounter28, RV32I only"),
    XC9D("hpmcounter29h", "Upper 32 bits of hpmcounter29, RV32I only"),
    XC9E("hpmcounter30h", "Upper 32 bits of hpmcounter30, RV32I only"),
    XC9F("hpmcounter31h", "Upper 32 bits of hpmcounter31, RV32I only"),
    ;

    override val address: UInt = name.removePrefix("X").toUInt(16)

    override val recognizable: List<String> = listOf(alias, name.lowercase())


}