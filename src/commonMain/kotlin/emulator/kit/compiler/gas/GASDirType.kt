package emulator.kit.compiler.gas

import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.lexer.TokenSeq
import emulator.kit.compiler.parser.Node

enum class GASDirType(val disabled: Boolean = false) : DirTypeInterface {
    ABORT(disabled = true),
    ALIGN,
    ALTMACRO,
    ASCII,
    ASCIZ,
    ATTACH_TO_GROUP_NAME,
    BALIGN,
    BSS,
    BYTE,
    COMM,
    DATA,
    DEF,
    DESC,
    DIM,
    DOUBLE,
    EJECT,
    ELSE,
    ELSEIF,
    END,
    ENDEF,
    ENDFUNC,
    ENDIF,
    EQU,
    EQUIV,
    EQV,
    ERR,
    ERROR,
    EXITM,
    EXTERN,
    FAIL,
    FILE,
    FILL,
    FLOAT,
    FUNC,
    GLOBAL,
    GNU_ATTRIBUTE_TAG,
    HIDDEN,
    HWORD,
    IDENT,
    IF,
    INCBIN,
    INCLUDE,
    INT,
    INTERNAL,
    IRP,
    IRPC,
    LCOMM,
    LFLAGS,
    LINE,
    LINKONCE,
    LIST,
    LN,
    LOC,
    LOC_MARK_LABELS,
    LOCAL,
    LONG,
    MACRO,
    MRI,
    NOALTMACRO,
    NOLIST,
    NOP,
    NOPS,
    OCTA,
    OFFSET,
    ORG,
    P2ALIGN,
    POPSECTION,
    PREVIOUS,
    PRINT,
    PROTECTED,
    PSIZE,
    PURGEM,
    PUSHSECTION,
    QUAD,
    RELOC,
    REPT,
    SBTTL,
    SCL,
    SECTION,
    SET,
    SHORT,
    SINGLE,
    SIZE,
    SKIP,
    SLEB128,
    SPACE,
    STABD,
    STABN,
    STABS,
    STRING,
    STRING8,
    STRING16,
    STRUCT,
    SUBSECTION,
    SYMVER,
    TAG,
    TEXT,
    TITLE,
    TLS_COMMON_SYMBOL,
    TYPE,
    ULEB128,
    VAL,
    VERSION,
    VTABLE_ENTRY,
    VTABLE_INHERIT,
    WARNING,
    WEAK,
    WEAKREF,
    WORD,
    ZERO,
    _2BYTE,
    _4BYTE,
    _8BYTE;

    override fun getDetectionString(): String = this.name.removePrefix("_")

    fun buildDirectiveContent(remainingTokens: MutableList<Token>): List<Node>?{
        val remainingTokens = remainingTokens
        when(this){
            ABORT -> TODO()
            ALIGN -> TODO()
            ALTMACRO -> TODO()
            ASCII -> TODO()
            ASCIZ -> TODO()
            ATTACH_TO_GROUP_NAME -> TODO()
            BALIGN -> TODO()
            BSS -> TODO()
            BYTE -> TODO()
            COMM -> TODO()
            DATA -> TODO()
            DEF -> TODO()
            DESC -> TODO()
            DIM -> TODO()
            DOUBLE -> TODO()
            EJECT -> TODO()
            ELSE -> TODO()
            ELSEIF -> TODO()
            END -> TODO()
            ENDEF -> TODO()
            ENDFUNC -> TODO()
            ENDIF -> TODO()
            EQU -> TODO()
            EQUIV -> TODO()
            EQV -> TODO()
            ERR -> TODO()
            ERROR -> TODO()
            EXITM -> TODO()
            EXTERN -> TODO()
            FAIL -> TODO()
            FILE -> TODO()
            FILL -> TODO()
            FLOAT -> TODO()
            FUNC -> TODO()
            GLOBAL -> TODO()
            GNU_ATTRIBUTE_TAG -> TODO()
            HIDDEN -> TODO()
            HWORD -> TODO()
            IDENT -> TODO()
            IF -> TODO()
            INCBIN -> TODO()
            INCLUDE -> TODO()
            INT -> TODO()
            INTERNAL -> TODO()
            IRP -> TODO()
            IRPC -> TODO()
            LCOMM -> TODO()
            LFLAGS -> TODO()
            LINE -> TODO()
            LINKONCE -> TODO()
            LIST -> TODO()
            LN -> TODO()
            LOC -> TODO()
            LOC_MARK_LABELS -> TODO()
            LOCAL -> TODO()
            LONG -> TODO()
            MACRO -> TODO()
            MRI -> TODO()
            NOALTMACRO -> TODO()
            NOLIST -> TODO()
            NOP -> TODO()
            NOPS -> TODO()
            OCTA -> TODO()
            OFFSET -> TODO()
            ORG -> TODO()
            P2ALIGN -> TODO()
            POPSECTION -> TODO()
            PREVIOUS -> TODO()
            PRINT -> TODO()
            PROTECTED -> TODO()
            PSIZE -> TODO()
            PURGEM -> TODO()
            PUSHSECTION -> TODO()
            QUAD -> TODO()
            RELOC -> TODO()
            REPT -> TODO()
            SBTTL -> TODO()
            SCL -> TODO()
            SECTION -> TODO()
            SET -> TODO()
            SHORT -> TODO()
            SINGLE -> TODO()
            SIZE -> TODO()
            SKIP -> TODO()
            SLEB128 -> TODO()
            SPACE -> TODO()
            STABD -> TODO()
            STABN -> TODO()
            STABS -> TODO()
            STRING -> TODO()
            STRING8 -> TODO()
            STRING16 -> TODO()
            STRUCT -> TODO()
            SUBSECTION -> TODO()
            SYMVER -> TODO()
            TAG -> TODO()
            TEXT -> TODO()
            TITLE -> TODO()
            TLS_COMMON_SYMBOL -> TODO()
            TYPE -> TODO()
            ULEB128 -> TODO()
            VAL -> TODO()
            VERSION -> TODO()
            VTABLE_ENTRY -> TODO()
            VTABLE_INHERIT -> TODO()
            WARNING -> TODO()
            WEAK -> TODO()
            WEAKREF -> TODO()
            WORD -> TODO()
            ZERO -> TODO()
            _2BYTE -> TODO()
            _4BYTE -> TODO()
            _8BYTE -> TODO()
        }
    }

}