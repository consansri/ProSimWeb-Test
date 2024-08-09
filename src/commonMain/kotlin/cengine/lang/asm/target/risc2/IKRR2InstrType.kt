package cengine.lang.asm.target.risc2

import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule


enum class IKRR2InstrType(val id: String, val paramType: IKRR2ParamType, val descr: String = "", override val isPseudo: Boolean = true, override val bytesNeeded: Int? = 4) : InstrTypeInterface {
    ADD("add", IKRR2ParamType.R2_TYPE, "addiere"),
    ADDI("addi", IKRR2ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenrichtig)"),
    ADDLI("addli", IKRR2ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenlos)"),
    ADDHI("addhi", IKRR2ParamType.I_TYPE, "addiere Konstante, höherwertiges Halbwort"),
    ADDX("addx", IKRR2ParamType.R2_TYPE, "berechne ausschließlich Übertrag der Addition"),

    //
    SUB("sub", IKRR2ParamType.R2_TYPE, "subtrahiere"),
    SUBX("subx", IKRR2ParamType.R2_TYPE, "berechne ausschließlich Übertrag der Subtraktion"),

    //
    CMPU("cmpu", IKRR2ParamType.R2_TYPE, "vergleiche vorzeichenlos"),
    CMPS("cmps", IKRR2ParamType.R2_TYPE, "vergleiche vorzeichenbehaftet"),
    CMPUI("cmpui", IKRR2ParamType.I_TYPE, "vergleiche vorzeichenlos mit vorzeichenlos erweiterter Konstanten"),
    CMPSI("cmpsi", IKRR2ParamType.I_TYPE, "vergleiche vorzeichenbehaftet mit vorzeichenrichtig erweiterter Konstanten"),

    //
    AND("and", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Und"),
    AND0I("and0i", IKRR2ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 00...0)"),
    AND1i("and1i", IKRR2ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 11...1)"),

    //
    OR("or", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Oder"),
    ORI("ori", IKRR2ParamType.I_TYPE, "verknüpfe logisch Oder mit Konstante"),

    //
    XOR("xor", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Exklusiv-Oder"),
    XORI("xori", IKRR2ParamType.I_TYPE, "verknüpfe logisch Exklusiv-Oder mit Konstante"),

    //
    LSL("lsl", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle logisch nach links"),
    LSR("lsr", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle logisch nach rechts"),
    ASL("asl", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle arithmetisch nach links"),
    ASR("asr", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle arithmetisch nach rechts"),
    ROL("rol", IKRR2ParamType.R1_TYPE, "rotiere um eine Stelle nach links"),
    ROR("ror", IKRR2ParamType.R1_TYPE, "rotiere um eine Stelle nach rechts"),

    //
    EXTB("extb", IKRR2ParamType.R1_TYPE, "erweitere niederwertigstes Byte (Byte 0) vorzeichenrichtig"),
    EXTH("exth", IKRR2ParamType.R1_TYPE, "erweitere niederwertiges Halbwort (Byte 1, Byte 0) vorzeichenrichtig"),

    //
    SWAPB("swapb", IKRR2ParamType.R1_TYPE, "vertausche Byte 3 mit Byte 2 und Byte 1 mit Byte 0"),
    SWAPH("swaph", IKRR2ParamType.R1_TYPE, "vertausche höherwertiges Halbwort und niederwertiges Halbwort"),

    //
    NOT("not", IKRR2ParamType.R1_TYPE, "invertiere bitweise"),

    //
    LDD("ldd", IKRR2ParamType.L_OFF_TYPE, "load (Register indirekt mit Offset)"),
    LDR("ldr", IKRR2ParamType.L_INDEX_TYPE, "load (Register indirekt mit Index)"),

    //
    STD("std", IKRR2ParamType.S_OFF_TYPE, "store (Register indirekt mit Offset)"),
    STR("str", IKRR2ParamType.S_INDEX_TYPE, "store (Register indirekt mit Index)"),

    //
    BEQ("beq", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc gleich 0 (EQual to 0)"),
    BNE("bne", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc ungleich 0 (Not Equal to 0)"),
    BLT("blt", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc kleiner als 0 (Less Than 0)"),
    BGT("bgt", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc größer als 0 (Greater Than 0)"),
    BLE("ble", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc kleiner oder gleich 0 (Less than or Equal to 0)"),
    BGE("bge", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc größer oder gleich 0 (Greater than or Equal to 0)"),

    //
    BRA("bra", IKRR2ParamType.B_DISP26_TYPE, "verzweige unbedingt (branch always)"),
    BSR("bsr", IKRR2ParamType.B_DISP26_TYPE, "verzweige in Unterprogramm (sichere Rücksprungadresse in r31)"),

    //
    JMP(
        "jmp",
        IKRR2ParamType.B_REG_TYPE,

        "springe an Adresse in rb"
    ),
    JSR(
        "jsr",
        IKRR2ParamType.B_REG_TYPE,
        "springe in Unterprg. an Adresse in rb (sichere Rücksprungadr. in r31)"
    );

    override val paramRule: Rule?
        get() = paramType.rule

    override val typeName: String
        get() = name

    fun isBranchToSubRoutine(): Boolean {
        return when (this) {
            JSR -> true
            BSR -> true
            else -> false
        }
    }

    fun isReturnFromSubRoutine(): Boolean {
        return when (this) {
            JMP -> true
            else -> false
        }
    }

    override fun getDetectionName(): String = id

}