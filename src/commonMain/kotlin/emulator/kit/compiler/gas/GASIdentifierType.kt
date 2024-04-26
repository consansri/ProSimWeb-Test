package emulator.kit.compiler.gas

import emulator.kit.compiler.lexer.TokenSeq

enum class GASIdentifierType(val seq: TokenSeq, val regex: Regex) {
    LOCAL_LABEL_DOLLAR_DEF(TokenSeq(TokenSeq.Component.InSpecific.INTEGER(), TokenSeq.Component.Specific("$"), TokenSeq.Component.Specific(":")), Regex("""^[0-9]+\$:""")),
    LOCAL_LABEL_DOLLAR_REF(TokenSeq(TokenSeq.Component.InSpecific.INTEGER(), TokenSeq.Component.Specific("$")), Regex("""^[0-9]+\$""")),
    LOCAL_LABEL_DEF(TokenSeq(TokenSeq.Component.InSpecific.INTEGER(), TokenSeq.Component.Specific(":")), Regex("""^[0-9]+:""")),
    LOCAL_LABEL_REFF(TokenSeq(TokenSeq.Component.InSpecific.INTEGER(), TokenSeq.Component.Specific("f")), Regex("""^[0-9]+f""")),
    LOCAL_LABEL_REFB(TokenSeq(TokenSeq.Component.InSpecific.INTEGER(), TokenSeq.Component.Specific("b")), Regex("""^[0-9]+b""")),
    LOCAL(TokenSeq(TokenSeq.Component.InSpecific.SYMBOL(startsWith = Settings.LOCAL_SYMBOL_PREFIX)), Regex("""^${Regex.escape(Settings.LOCAL_SYMBOL_PREFIX)}[a-zA-Z$._][a-zA-Z0-9$._]*""")),
    LABEL_DEF(TokenSeq(TokenSeq.Component.InSpecific.SYMBOL(), TokenSeq.Component.Specific(":")), Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*:""")),
    CLASSIC(TokenSeq(TokenSeq.Component.InSpecific.SYMBOL()), Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*"""))
}