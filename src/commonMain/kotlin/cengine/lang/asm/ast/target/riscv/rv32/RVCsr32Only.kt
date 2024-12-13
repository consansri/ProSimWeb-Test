package cengine.lang.asm.ast.target.riscv.rv32

import cengine.lang.asm.ast.target.riscv.RVCsr

enum class RVCsr32Only(alias: String, val description: String): RVCsr {
    X3A1("pmpcfg1", "Physical memory protection configuration, RV32 only"),
    X3A3("pmpcfg3","Physical memory protection configuration, RV32 only"),

    XB80("mcycleh", "Upper 32 bits of mcycle, RV32I only"),
    XB82("minstreth", "Upper 32 bits of minstret, RV32I only"),
    XB83("mhpmcounter3h", "Upper 32 bits of mhpmcounter3, RV32I only"),
    XB84("mhpmcounter4h", "Upper 32 bits of mhpmcounter4, RV32I only"),
    XB85("mhpmcounter5h", "Upper 32 bits of mhpmcounter5, RV32I only"),
    XB86("mhpmcounter6h", "Upper 32 bits of mhpmcounter6, RV32I only"),
    XB87("mhpmcounter7h", "Upper 32 bits of mhpmcounter7, RV32I only"),
    XB88("mhpmcounter8h", "Upper 32 bits of mhpmcounter8, RV32I only"),
    XB89("mhpmcounter9h", "Upper 32 bits of mhpmcounter9, RV32I only"),
    XB8A("mhpmcounter10h", "Upper 32 bits of mhpmcounter10, RV32I only"),
    XB8B("mhpmcounter11h", "Upper 32 bits of mhpmcounter11, RV32I only"),
    XB8C("mhpmcounter12h", "Upper 32 bits of mhpmcounter12, RV32I only"),
    XB8D("mhpmcounter13h", "Upper 32 bits of mhpmcounter13, RV32I only"),
    XB8E("mhpmcounter14h", "Upper 32 bits of mhpmcounter14, RV32I only"),
    XB8F("mhpmcounter15h", "Upper 32 bits of mhpmcounter15, RV32I only"),
    XB90("mhpmcounter16h", "Upper 32 bits of mhpmcounter16, RV32I only"),
    XB91("mhpmcounter17h", "Upper 32 bits of mhpmcounter17, RV32I only"),
    XB92("mhpmcounter18h", "Upper 32 bits of mhpmcounter18, RV32I only"),
    XB93("mhpmcounter19h", "Upper 32 bits of mhpmcounter19, RV32I only"),
    XB94("mhpmcounter20h", "Upper 32 bits of mhpmcounter20, RV32I only"),
    XB95("mhpmcounter21h", "Upper 32 bits of mhpmcounter21, RV32I only"),
    XB96("mhpmcounter22h", "Upper 32 bits of mhpmcounter22, RV32I only"),
    XB97("mhpmcounter23h", "Upper 32 bits of mhpmcounter23, RV32I only"),
    XB98("mhpmcounter24h", "Upper 32 bits of mhpmcounter24, RV32I only"),
    XB99("mhpmcounter25h", "Upper 32 bits of mhpmcounter25, RV32I only"),
    XB9A("mhpmcounter26h", "Upper 32 bits of mhpmcounter26, RV32I only"),
    XB9B("mhpmcounter27h", "Upper 32 bits of mhpmcounter27, RV32I only"),
    XB9C("mhpmcounter28h", "Upper 32 bits of mhpmcounter28, RV32I only"),
    XB9D("mhpmcounter29h", "Upper 32 bits of mhpmcounter29, RV32I only"),
    XB9E("mhpmcounter30h", "Upper 32 bits of mhpmcounter30, RV32I only"),
    XB9F("mhpmcounter31h", "Upper 32 bits of mhpmcounter31, RV32I only"),

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

    override val numericalValue: UInt = name.removePrefix("X").toUInt(16)

    override val recognizable: List<String> = listOf(alias, name.lowercase())


}