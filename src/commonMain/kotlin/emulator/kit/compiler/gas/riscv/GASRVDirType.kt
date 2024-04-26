package emulator.kit.compiler.gas.riscv

import emulator.kit.compiler.DirTypeInterface

enum class GASRVDirType : DirTypeInterface {
    ALIGN,
    HALF,
    WORD,
    DWORD,
    DTPRELWORD,
    DTPRELDWORD,
    ULEB128,
    SLEB128,
    OPTION,
    INSN,
    ATTRIBUTE
    ;

    override fun getDetectionString(): String = this.name


}