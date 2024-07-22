package prosim.ide

import cengine.lang.LanguageService
import cengine.lang.asm.AsmLang
import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates


fun LanguageService.getFileIcon(): FlatSVGIcon? {
    return when (this) {
        is AsmLang -> UIStates.icon.get().asmFile
        else -> null
    }
}