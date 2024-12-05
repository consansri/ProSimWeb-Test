package cengine.lang.asm.ast.target.riscv

import cengine.lang.asm.ast.RegTypeInterface

interface RVCsr: RegTypeInterface {

    companion object{
        val regs: List<RVCsr> = RVCsrUnprivileged.entries + RVCsrSupervisor.entries + RVCsrMachine.entries + RVCsrDebug.entries
    }
}