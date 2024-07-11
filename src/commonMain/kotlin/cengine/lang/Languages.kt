package cengine.lang

import cengine.lang.asm.AsmLang

enum class Languages(val service: LanguageService) {
    ASM(AsmLang)
}