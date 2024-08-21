package emulator.archs.ikrrisc2

import cengine.util.integer.*
import emulator.archs.ArchIKRRisc2
import emulator.archs.ikrrisc2.IKRRisc2BinMapper.MaskLabel.*
import emulator.archs.ikrrisc2.IKRRisc2BinMapper.OpCode
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.common.RegContainer
import emulator.kit.memory.Memory


enum class InstrType(val id: String, val paramType: ParamType, val opCode: OpCode, val descr: String = "") : InstrTypeInterface {
    ADD("add", ParamType.R2_TYPE, OpCode("111111 00000 00000 000000 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "addiere") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get() + decodeResult.ra.get())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ADDI("addi", ParamType.I_TYPE, OpCode("000000 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "addiere Konstante (erweitere Konstante vorzeichenrichtig)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get() + decodeResult.imm16.getResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ADDLI("addli", ParamType.I_TYPE, OpCode("000001 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "addiere Konstante (erweitere Konstante vorzeichenlos)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get() + decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ADDHI("addhi", ParamType.I_TYPE, OpCode("000010 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "addiere Konstante, höherwertiges Halbwort") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get() + decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH).shl(16))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ADDX("addx", ParamType.R2_TYPE, OpCode("111111 00000 00000 100000 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "berechne ausschließlich Übertrag der Addition") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val result = decodeResult.rb.get().toBin().detailedPlus(decodeResult.ra.get())
            decodeResult.rc.set(if (result.carry) IKRRisc2.WORD_WIDTH_ONE else IKRRisc2.WORD_WIDTH_ZERO)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    SUB("sub", ParamType.R2_TYPE, OpCode("111111 00000 00000 000010 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "subtrahiere") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get() - decodeResult.ra.get())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    SUBX("subx", ParamType.R2_TYPE, OpCode("111111 00000 00000 100010 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "berechne ausschließlich Übertrag der Subtraktion") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val result = decodeResult.rb.get().toBin().detailedMinus(decodeResult.ra.get())
            decodeResult.rc.set(if (result.borrow) IKRRisc2.WORD_WIDTH_ONE else IKRRisc2.WORD_WIDTH_ZERO)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    CMPU("cmpu", ParamType.R2_TYPE, OpCode("111111 00000 00000 001000 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "vergleiche vorzeichenlos") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val b = decodeResult.rb.get().toBin()
            val a = decodeResult.ra.get().toBin()
            if (b == a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ZERO)
            } else if (b > a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ONE)
            } else {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_NEGATIVE_ONE)
            }

            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    CMPS("cmps", ParamType.R2_TYPE, OpCode("111111 00000 00000 001001 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "vergleiche vorzeichenbehaftet") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val b = decodeResult.rb.get().toDec()
            val a = decodeResult.ra.get().toDec()
            if (b == a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ZERO)
            } else if (b > a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ONE)
            } else {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_NEGATIVE_ONE)
            }

            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    CMPUI("cmpui", ParamType.I_TYPE, OpCode("001000 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "vergleiche vorzeichenlos mit vorzeichenlos erweiterter Konstanten") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val b = decodeResult.rb.get().toBin()
            val a = decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH)
            if (b == a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ZERO)
            } else if (b > a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ONE)
            } else {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_NEGATIVE_ONE)
            }

            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    CMPSI("cmpsi", ParamType.I_TYPE, OpCode("001001 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "vergleiche vorzeichenbehaftet mit vorzeichenrichtig erweiterter Konstanten") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val b = decodeResult.rb.get().toDec()
            val a = decodeResult.imm16.getResized(IKRRisc2.WORD_WIDTH).toDec()
            if (b == a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ZERO)
            } else if (b > a) {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_ONE)
            } else {
                decodeResult.rc.set(IKRRisc2.WORD_WIDTH_NEGATIVE_ONE)
            }

            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    AND("and", ParamType.R2_TYPE, OpCode("111111 00000 00000 000100 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "verknüpfe logisch Und") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() and decodeResult.ra.get().toBin())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    AND0I("and0i", ParamType.I_TYPE, OpCode("000100 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 00...0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() and decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    AND1i("and1i", ParamType.I_TYPE, OpCode("000101 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 11...1)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val imm = Bin("1".repeat(IKRRisc2.WORD_WIDTH.bitWidth / 2) + decodeResult.imm16.toRawString(), IKRRisc2.WORD_WIDTH)
            decodeResult.rc.set(decodeResult.rb.get().toBin() and imm)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    OR("or", ParamType.R2_TYPE, OpCode("111111 00000 00000 000110 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "verknüpfe logisch Oder") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() or decodeResult.ra.get().toBin())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ORI("ori", ParamType.I_TYPE, OpCode("000110 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "verknüpfe logisch Oder mit Konstante") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() or decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    XOR("xor", ParamType.R2_TYPE, OpCode("111111 00000 00000 000111 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "verknüpfe logisch Exklusiv-Oder") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() xor decodeResult.ra.get().toBin())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    XORI("xori", ParamType.I_TYPE, OpCode("000111 00000 00000 0000000000000000", OPCODE, RC, RB, IMM16), "verknüpfe logisch Exklusiv-Oder mit Konstante") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() xor decodeResult.imm16.getUResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    LSL("lsl", ParamType.R1_TYPE, OpCode("111111 00000 00000 101000 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "schiebe um eine Stelle logisch nach links") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() ushl 1)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    LSR("lsr", ParamType.R1_TYPE, OpCode("111111 00000 00000 101001 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "schiebe um eine Stelle logisch nach rechts") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() ushr 1)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ASL("asl", ParamType.R1_TYPE, OpCode("111111 00000 00000 101010 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "schiebe um eine Stelle arithmetisch nach links") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() shl 1)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ASR("asr", ParamType.R1_TYPE, OpCode("111111 00000 00000 101011 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "schiebe um eine Stelle arithmetisch nach rechts") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin() shr 1)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ROL("rol", ParamType.R1_TYPE, OpCode("111111 00000 00000 101100 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "rotiere um eine Stelle nach links") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin().rotateLeft(1))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    ROR("ror", ParamType.R1_TYPE, OpCode("111111 00000 00000 101101 00000 00001", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "rotiere um eine Stelle nach rechts") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin().rotateRight(1))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    EXTB("extb", ParamType.R1_TYPE, OpCode("111111 00000 00000 110000 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "erweitere niederwertigstes Byte (Byte 0) vorzeichenrichtig") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin().getUResized(Size.Bit8).getResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    EXTH("exth", ParamType.R1_TYPE, OpCode("111111 00000 00000 110001 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "erweitere niederwertiges Halbwort (Byte 1, Byte 0) vorzeichenrichtig") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin().getUResized(Size.Bit16).getResized(IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    SWAPB("swapb", ParamType.R1_TYPE, OpCode("111111 00000 00000 110010 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "vertausche Byte 3 mit Byte 2 und Byte 1 mit Byte 0") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val binStr = decodeResult.rb.get().toBin().toRawString()
            val swappedStr = binStr.substring(8, 16) + binStr.substring(0, 8) + binStr.substring(24, 32) + binStr.substring(16, 24)
            decodeResult.rc.set(Bin(swappedStr, IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    SWAPH("swaph", ParamType.R1_TYPE, OpCode("111111 00000 00000 101100 00000 10000", FUNCT6, RC, RB, OPCODE, NONE5, FUNCT5), "vertausche höherwertiges Halbwort und niederwertiges Halbwort") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val binStr = decodeResult.rb.get().toBin().toRawString()
            val swappedStr = binStr.substring(16, 32) + binStr.substring(0, 16)
            decodeResult.rc.set(Bin(swappedStr, IKRRisc2.WORD_WIDTH))
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    NOT("not", ParamType.R1_TYPE, OpCode("111111 00000 00000 110011 00000 00000", OPCODE, RC, RB, OPCODE, NONE5, FUNCT5), "invertiere bitweise") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            decodeResult.rc.set(decodeResult.rb.get().toBin().inv())
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    LDD("ldd", ParamType.L_OFF_TYPE, OpCode("010000 00000 00000 0000000000000000", OPCODE, RC, RB, DISP16), "load (Register indirekt mit Offset)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val offset = decodeResult.disp16.getResized(IKRRisc2.WORD_WIDTH)
            val addr = decodeResult.rb.get().toBin() + offset
            val loaded = arch.dataMemory.load(addr.toHex(), 1, tracker)
            decodeResult.rc.set(loaded)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    LDR("ldr", ParamType.L_INDEX_TYPE, OpCode("111111 00000 00000 010000 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "load (Register indirekt mit Index)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val addr = decodeResult.rb.get().toBin() + decodeResult.ra.get()
            val loaded = arch.dataMemory.load(addr.toHex(), 1, tracker)
            decodeResult.rc.set(loaded)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    STD("std", ParamType.S_OFF_TYPE, OpCode("010100 00000 00000 0000000000000000", OPCODE, RC, RB, DISP16), "store (Register indirekt mit Offset)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val offset = decodeResult.disp16.getResized(IKRRisc2.WORD_WIDTH)
            val addr = decodeResult.rb.get().toBin() + offset
            arch.dataMemory.store(addr.toHex(), decodeResult.rc.get(), Memory.InstanceType.DATA, tracker = tracker)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },
    STR("str", ParamType.S_INDEX_TYPE, OpCode("111111 00000 00000 010100 00000 00000", FUNCT6, RC, RB, OPCODE, NONE5, RA), "store (Register indirekt mit Index)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val addr = decodeResult.rb.get().toBin() + decodeResult.ra.get()
            arch.dataMemory.store(addr.toHex(), decodeResult.rc.get(), Memory.InstanceType.DATA, tracker = tracker)
            pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
        }
    },

    //
    BEQ("beq", ParamType.B_DISP18_TYPE, OpCode("111110 00000 000 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc gleich 0 (EQual to 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = decodeResult.rc.get().toDec().toIntOrNull() == 0
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },
    BNE("bne", ParamType.B_DISP18_TYPE, OpCode("111110 00000 001 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc ungleich 0 (Not Equal to 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = decodeResult.rc.get().toDec().toIntOrNull() != 0
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },
    BLT("blt", ParamType.B_DISP18_TYPE, OpCode("111110 00000 010 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc kleiner als 0 (Less Than 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = decodeResult.rc.get().toBin().toRawString().firstOrNull() == '1'
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },
    BGT("bgt", ParamType.B_DISP18_TYPE, OpCode("111110 00000 011 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc größer als 0 (Greater Than 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = (decodeResult.rc.get().toDec().toIntOrNull() ?: throw Error("Couldn't calculate integer for ${decodeResult.rc.get()}.")) > 0
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },
    BLE("ble", ParamType.B_DISP18_TYPE, OpCode("111110 00000 100 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc kleiner oder gleich 0 (Less than or Equal to 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = (decodeResult.rc.get().toDec().toIntOrNull() ?: throw Error("Couldn't calculate integer for ${decodeResult.rc.get()}.")) <= 0
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },
    BGE("bge", ParamType.B_DISP18_TYPE, OpCode("111110 00000 101 000000000000000000", OPCODE, RC, FUNCT3, DISP18), "verzweige, falls rc größer oder gleich 0 (Greater than or Equal to 0)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val cond = (decodeResult.rc.get().toDec().toIntOrNull() ?: throw Error("Couldn't calculate integer for ${decodeResult.rc.get()}.")) >= 0
            if (cond) {
                pc.set(pc.get() + decodeResult.disp18.getResized(IKRRisc2.WORD_WIDTH))
            } else {
                pc.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            }
        }
    },

    //
    BRA("bra", ParamType.B_DISP26_TYPE, OpCode("111100 00000000000000000000000000", OPCODE, DISP26), "verzweige unbedingt (branch always)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            pc.set(pc.get() + decodeResult.disp26.getResized(IKRRisc2.WORD_WIDTH))
        }
    },
    BSR("bsr", ParamType.B_DISP26_TYPE, OpCode("111101 00000000000000000000000000", OPCODE, DISP26), "verzweige in Unterprogramm (sichere Rücksprungadresse in r31)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val r31 = arch.getRegByAddr(IKRRisc2.R31_ADDR) ?: throw Exception("Couldn't find r31 while executing IKRRisc2 BSR.")
            r31.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            pc.set(pc.get() + decodeResult.disp26.getResized(IKRRisc2.WORD_WIDTH))
        }
    },

    //
    JMP("jmp", ParamType.B_REG_TYPE, OpCode("111111 00000 00000 111100 00000 00000", FUNCT6, NONE5, RB, OPCODE, NONE5, FUNCT5), "springe an Adresse in rb") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            pc.set(decodeResult.rb.get())
        }
    },
    JSR("jsr", ParamType.B_REG_TYPE, OpCode("111111 00000 00000 111101 00000 00000", FUNCT6, NONE5, RB, OPCODE, NONE5, FUNCT5), "springe in Unterprg. an Adresse in rb (sichere Rücksprungadr. in r31)") {
        override fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker) {
            val r31 = arch.getRegByAddr(IKRRisc2.R31_ADDR) ?: throw Exception("Couldn't find r31 while executing IKRRisc2 BSR.")
            r31.set(pc.get() + IKRRisc2.WORD_WIDTH_ONE)
            pc.set(decodeResult.rb.get())
        }
    }
    ;

    abstract fun execute(arch: ArchIKRRisc2, pc: RegContainer.PC, decodeResult: IKRRisc2BinMapper.DecodeResult, tracker: Memory.AccessTracker)

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