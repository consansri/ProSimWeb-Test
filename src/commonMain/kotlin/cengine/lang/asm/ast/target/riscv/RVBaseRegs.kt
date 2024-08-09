package cengine.lang.asm.ast.target.riscv

import cengine.lang.asm.ast.RegTypeInterface

enum class RVBaseRegs(val names: List<String>, val aliases: List<String>) : RegTypeInterface {
    ZERO(listOf("zero"), listOf("x0")),
    RA(listOf("ra"), listOf("x1")),
    SP(listOf("sp"), listOf("x2")),
    GP(listOf("gp"), listOf("x3")),
    TP(listOf("tp"), listOf("x4")),
    T0(listOf("t0"), listOf("x5")),
    T1(listOf("t1"), listOf("x6")),
    T2(listOf("t2"), listOf("x7")),
    S0_FP(listOf("s0", "fp"), listOf("x8")),
    S1(listOf("s1"), listOf("x9")),
    A0(listOf("a0"), listOf("x10")),
    A1(listOf("a1"), listOf("x11")),
    A2(listOf("a2"), listOf("x12")),
    A3(listOf("a3"), listOf("x13")),
    A4(listOf("a4"), listOf("x14")),
    A5(listOf("a5"), listOf("x15")),
    A6(listOf("a6"), listOf("x16")),
    A7(listOf("a7"), listOf("x17")),
    S2(listOf("s2"), listOf("x18")),
    S3(listOf("s3"), listOf("x19")),
    S4(listOf("s4"), listOf("x20")),
    S5(listOf("s5"), listOf("x21")),
    S6(listOf("s6"), listOf("x22")),
    S7(listOf("s7"), listOf("x23")),
    S8(listOf("s8"), listOf("x24")),
    S9(listOf("s9"), listOf("x25")),
    S10(listOf("s10"), listOf("x26")),
    S11(listOf("s11"), listOf("x27")),
    T3(listOf("t3"), listOf("x28")),
    T4(listOf("t4"), listOf("x29")),
    T5(listOf("t5"), listOf("x30")),
    T6(listOf("t6"), listOf("x31"));

    override val recognizable: List<String> = names + aliases
}