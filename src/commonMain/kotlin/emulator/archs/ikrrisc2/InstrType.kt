package emulator.archs.ikrrisc2

import emulator.kit.assembler.InstrTypeInterface

enum class InstrType(val id: String, val paramType: ParamType, val descr: String): InstrTypeInterface {
    ADD("add", ParamType.R_TYPE, "addiere"),
    ADDI("addi", ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenrichtig)"),
    ADDLI("addli", ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenlos)"),
    ADDHI("addhi", ParamType.I_TYPE, "addiere Konstante, höherwertiges Halbwort"),
    ADDX("addx", ParamType.R_TYPE, "berechne ausschließlich Übertrag der Addition"),
    //
    SUB("sub", ParamType.R_TYPE, "subtrahiere"),
    SUBX("subx", ParamType.R_TYPE, "berechne ausschließlich Übertrag der Subtraktion"),
    //
    CMPU("cmpu", ParamType.R_TYPE, "vergleiche vorzeichenlos"),
    CMPS("cmps", ParamType.R_TYPE, "vergleiche vorzeichenbehaftet"),
    CMPUI("cmpui", ParamType.I_TYPE, "vergleiche vorzeichenlos mit vorzeichenlos erweiterter Konstanten"),
    CMPSI("cmpsi", ParamType.I_TYPE, "vergleiche vorzeichenbehaftet mit vorzeichenrichtig erweiterter Konstanten"),
    //
    AND("and", ParamType.R_TYPE, "verknüpfe logisch Und"),
    AND0I("and0i", ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 00...0)"),
    AND1i("and1i", ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 11...1)"),
    //
    OR("or", ParamType.R_TYPE, "verknüpfe logisch Oder"),
    ORI("ori", ParamType.I_TYPE, "verknüpfe logisch Oder mit Konstante"),
    //
    XOR("xor", ParamType.R_TYPE, "verknüpfe logisch Exklusiv-Oder"),
    XORI("xori", ParamType.I_TYPE, "verknüpfe logisch Exklusiv-Oder mit Konstante"),
    //
    LSL("lsl", ParamType.R_TYPE_1S, "schiebe um eine Stelle logisch nach links"),
    LSR("lsr", ParamType.R_TYPE_1S, "schiebe um eine Stelle logisch nach rechts"),
    ASL("asl", ParamType.R_TYPE_1S, "schiebe um eine Stelle arithmetisch nach links"),
    ASR("asr", ParamType.R_TYPE_1S, "schiebe um eine Stelle arithmetisch nach rechts"),
    ROL("rol", ParamType.R_TYPE_1S, "rotiere um eine Stelle nach links"),
    ROR("ror", ParamType.R_TYPE_1S, "rotiere um eine Stelle nach rechts"),
    //
    EXTB("extb", ParamType.R_TYPE_1S, "erweitere niederwertigstes Byte (Byte 0) vorzeichenrichtig"),
    EXTH("exth", ParamType.R_TYPE_1S, "erweitere niederwertiges Halbwort (Byte 1, Byte 0) vorzeichenrichtig"),
    //
    SWAPB("swapb", ParamType.R_TYPE_1S, "vertausche Byte 3 mit Byte 2 und Byte 1 mit Byte 0"),
    SWAPH("swaph", ParamType.R_TYPE_1S, "vertausche höherwertiges Halbwort und niederwertiges Halbwort"),
    //
    NOT("not", ParamType.R_TYPE_1S, "invertiere bitweise"),
    //
    LDD("ldd", ParamType.L_TYPE_OFF, "load (Register indirekt mit Offset)"),
    LDR("ldr", ParamType.L_TYPE_INDEX, "load (Register indirekt mit Index)"),
    //
    STD("std", ParamType.S_TYPE_OFF, "store (Register indirekt mit Offset)"),
    STR("str", ParamType.S_TYPE_INDEX, "store (Register indirekt mit Index)"),
    //
    BEQ("beq", ParamType.BRANCH_DISP18, "verzweige, falls rc gleich 0 (EQual to 0)"),
    BNE("bne", ParamType.BRANCH_DISP18, "verzweige, falls rc ungleich 0 (Not Equal to 0)"),
    BLT("blt", ParamType.BRANCH_DISP18, "verzweige, falls rc kleiner als 0 (Less Than 0)"),
    BGT("bgt", ParamType.BRANCH_DISP18, "verzweige, falls rc größer als 0 (Greater Than 0)"),
    BLE("ble", ParamType.BRANCH_DISP18, "verzweige, falls rc kleiner oder gleich 0 (Less than or Equal to 0)"),
    BGE("bge", ParamType.BRANCH_DISP18, "verzweige, falls rc größer oder gleich 0 (Greater than or Equal to 0)"),
    //
    BRA("bra", ParamType.J_DISP26, "verzweige unbedingt (branch always)"),
    BSR("bsr", ParamType.J_DISP26, "verzweige in Unterprogramm (sichere Rücksprungadresse in r31)"),
    //
    JMP("jmp", ParamType.J_RB, "springe an Adresse in rb"),
    JSR("jsr", ParamType.J_RB, "springe in Unterprg. an Adresse in rb (sichere Rücksprungadr. in r31)")


    ;

    override fun getDetectionName(): String = id

}