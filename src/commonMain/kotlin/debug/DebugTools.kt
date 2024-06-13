package debug

object DebugTools {

    /*
     * EXTENDABLE
     */

    const val RV32_showAsmInfo = false
    const val RV32_showBinMapperInfo = false
    const val RV32_showOpCodeInfo = false
    const val RV32_showGrammarScanTiers = false

    const val RV64_showAsmInfo = false
    const val RV64_showBinMapperInfo = false
    const val RV64_showGrammarScanTiers = false
    const val RV64_showLIDecisions = false

    const val IKRRisc2_showBinMapperInfo = true

    /*
     * KIT
     */

    const val KIT_showCheckCodeEvents = false
    const val KIT_showFileHandlerInfo = false
    const val KIT_showMemoryInfo = false

    const val KIT_showFilteredTokens = false
    const val KIT_showSections = false
    const val KIT_showRuleChecks = false
    const val KIT_showGrammarTree = false
    const val KIT_showPostFixExpressions = false

    val KIT_ValBinaryToolsDivisionLoopLimit: Long? = null
    const val KIT_showValCheckWarnings = false
    const val KIT_showValTypeConversionInfo = false
    const val KIT_showValOperatorCalculations = false
    const val KIT_showValBinaryToolsCalculations = false
    const val KIT_showValBinaryToolsCalculationDetails = false
    const val KIT_showValDecToolsCalculations = false
    const val KIT_showValDecToolsCalculationDetails = false

    /*
     * VISUAL
     */

    const val REACT_showUpdateInfo = false


}