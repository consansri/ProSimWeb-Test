import web.cssom.*

object StyleConst {

    var mode: Mode = Mode.LIGHT

    /**
     * CSSOM
     */

    // GLOBAL
    val transparent = Color("#00000000")
    val layoutSwitchMediaQuery = "@media (max-width: 1200px)"


    const val CLASS_LOGO = "logo"

    object Header {
        // COLORS
        val BgColor = Color("#29335C")
        val BgColorSec = Color("#5767aa")
        val BgColorThird = Color("#DB2B39")
        val FgColor = Color("#EEE")

        val IndexNavMobile = integer(20)
        val IndexNavDropDown = integer(21)

        const val CLASS_DROPDOWN = "menu-dropdown"
        const val CLASS_MOBILE_OPEN = "menu-mobile-open"
        const val CLASS_OVERLAY = "menu-overlay"
        const val CLASS_OVERLAY_LABELEDINPUT = "menu-overlay-items"
    }

    object Main {
        val appControlBgColor = Color("#717171")
        val AccColor = Color("#29335C")
        val AccColorSec = Color("#EA5455")
        val AccColorThird = Color("#6D8AF5")
        val DeleteColor = Color("#EE2222FF")
        val BgColor = ModeColor("#F1F1E6", "#515151")
        val FgColor = ModeColor("#454545", "#D5D5D5")
        val tableRegBgColor = ModeColor("#E3E3E2", "#4D628F")
        val tableMemBgColor = ModeColor("#E3E3E2", "#29335C")

        val lPercentage = 40
        val rPercentage = 100 - lPercentage

        object Editor {
            val BgColor = ModeColor("#EEEEEE", "#222222")
            val FgColor = ModeColor("#313131", "#AABACA")
            val Font = "font-family: 'JetBrains Mono', monospace !important;"
            val FontSize = 16.px

            object Controls {
                val BgColor = Color("#29335c")
                val BgHoverColor = Color("#7983Ac")

                val iconSize = 1.8.rem
                val iconPadding = 0.1.rem
                val controlSize = 2.0.rem
                val borderRadius = 0.4.rem

                const val CLASS = "controls"
                const val CLASS_CONTROL = "control"
                const val CLASS_ACTIVE = "active"
                const val CLASS_INFOPANEL = "info-panel"
            }

            object Transcript {
                val FgColor = ModeColor("#313131", "#AABACA")
                const val CLASS = "transcript"
                const val CLASS_TABLE = "ts-table"
                const val CLASS_TITLE = "ts-title"
            }

            object TextField {
                val TabBorderColor = Color("#717171")
                val TabFgColor = Color("#717171")
                val TabActiveBgColor = Color("#71717131")
                val TabIconSize = 1.4.rem
                val LineNumbersBorderColor = ModeColor("#B5B5B5")
                val LineNumbersColor = ModeColor("#999999")
                val LineActiveColor = ModeColor("#00FF00")
                val minLineNumWidth = 35.px
                val tabSize = 6
                val lineHeight = 21
                val IndexArea = integer(2)
                val IndexHL = integer(1)

                const val CLASS = "textfield"

                const val CLASS_TABS = "editor-tabs"
                const val CLASS_TAB = "editor-tab"
                const val CLASS_TAB_ACTIVE = "editor-tab-active"

                const val CLASS_SCROLL_CONTAINER = "editor-scroll-container"

                const val CLASS_LINE_ACTIVE = "line-active"
                const val CLASS_LINE_NUMBERS = "editor-line-numbers"

                const val CLASS_INPUT_DIV = "editor-input-div"
                const val CLASS_AREA = "editor-area"
                const val CLASS_HIGHLIGHTING = "editor-highlighting"
                const val CLASS_HIGHLIGHTING_CONTENT = "editor-highlighting-content"
            }

            enum class HL(val color: ModeColor, val appendsOn: On = On.Color) {
                base00(ModeColor("#202746")),
                base01(ModeColor("#293256")),
                base02(ModeColor("#5e6687")),
                base03(ModeColor("#6b7394")),
                base04(ModeColor("#898ea4")),
                base05(ModeColor("#979db4")),
                base06(ModeColor("#dfe2f1")),
                base07(ModeColor("#f5f7ff")),
                red(ModeColor("#c94922")),
                orange(ModeColor("#c76b29")),
                yellow(ModeColor("#c08b30")),
                greenOld(ModeColor("#ac9739")),
                green(ModeColor("#008b19")),
                cyan(ModeColor("#22a2c9")),
                blue(ModeColor("#3d8fd1")),
                violet(ModeColor("#6679cc")),
                magenta(ModeColor("#9c637a")),
                whitespace(ModeColor("#B0C4DEFF"), On.BackgroundColor);

                fun getFlag(): String {
                    return this.name
                }
            }

            enum class On {
                BackgroundColor,
                Color
            }

            // MAIN
            const val CLASS = "editor"


        }

        object Processor {
            val BgColor = ModeColor("#091A40")

            const val CLASS = "processor"
        }

        object AppControls {
            const val CLASS = "controls"

            val iconSize = 1.8.rem
            val iconPadding = 0.1.rem
            val size = iconSize + 2 * iconPadding
        }

        object Console {
            const val CLASS = "console"
        }

        const val CLASS_DELETE = "delete"
        const val CLASS_ANIM_ROTATION = "anim-rotation"
        const val CLASS_ANIM_SHAKERED = "anim-shakered"
        const val CLASS_ANIM_BLINKGREEN = "anim-blinkgreen"
        const val CLASS_ANIM_DEACTIVATED = "anim-deactivated"
    }

    object Footer {
        val BgColor = ModeColor("#905356")
        val FgColor = ModeColor("#FFF")
        var iconSize = 4.rem
    }


    // NUMBERS
    val paddingSize = 0.4.rem
    val borderRadius = 0.3.rem

    val iconSizeNav = 1.8.rem
    val iconFilter = invert(1).also { sepia(0.2) }.also { saturate(1.278) }.also { hueRotate(202.deg) }.also { brightness(1.20) }.also { contrast(0.87) }
    val iconActiveFilter = invert(71.pct).also { sepia(54.pct) }.also { saturate(429.pct) }.also { hueRotate(83.deg) }.also { brightness(89.pct) }.also { contrast(87.pct) }

    val iconSize = 1.8.rem
    val iconPadding = 0.1.rem
    val iconBorderRadius = 0.4.rem

    // FONTS
    val logoFont = "font-family: 'Bungee Shade', cursive"


    /**
     *
     */


    // MENU


    // PROCESSOR
    val CLASS_EXEC_PROGRESS = "exec-progress"
    val CLASS_EXEC_PROGRESS_BAR = "exec-progress-bar"
    val CLASS_TABLE_INPUT = "dcf-input"


    // PROCESSOR.FLAGSCONDSVIEW
    val CLASS_PROC_FC_CONTAINER = "proc-fc-container"
    val CLASS_PROC_FC_COND_CONTAINER = "proc-fc-cond-container"
    val CLASS_PROC_FC_FLAG_CONTAINER = "proc-fc-flag-container"
    val CLASS_PROC_FC_COND = "proc-fc-cond"
    val CLASS_PROC_FC_FLAG = "proc-fc-flag"
    val CLASS_PROC_FC_COND_ACTIVE = "proc-fc-cond-active"
    val CLASS_PROC_FC_FLAG_ACTIVE = "proc-fc-flag-active"

    // PROCESSOR.MEMORY
    val CLASS_TABLE_MARK_PROGRAM = "dcf-mark-program"
    val CLASS_TABLE_MARK_DATA = "dcf-mark-data"
    val CLASS_TABLE_MARK_ELSE = "dcf-mark-else"
    val CLASS_TABLE_MARK_EDITABLE = "dcf-mark-editable"

    // INFO


    // CONSOLE
    val MESSAGE_TYPE_INFO = 0
    val MESSAGE_TYPE_LOG = 1
    val MESSAGE_TYPE_WARN = 2
    val MESSAGE_TYPE_ERROR = 3


    // FOOTER


    // ANIM
    val ANIM_SHAKERED = "anim-shakered"
    val ANIM_BLINKGREEN = "anim-blinkgreen"

    /*  */

    object Icons {
        const val autoscroll = "benicons/ver3/autoscroll.svg"
        const val backwards = "benicons/ver3/backwards.svg"
        const val darkmode = "benicons/ver3/darkmode.svg"
        const val disassembler = "benicons/ver3/disassembler.svg"
        const val export = "benicons/ver3/export.svg"
        const val forwards = "benicons/ver3/forwards.svg"
        const val home = "benicons/ver3/home.svg"
        const val import = "benicons/ver3/import.svg"
        const val info = "benicons/ver3/info.svg"
        const val lightmode = "benicons/ver3/lightmode.svg"
        const val pin = "benicons/ver3/pin.svg"
        const val processor = "benicons/ver3/processor.svg"
        const val status_error = "benicons/ver3/status_error.svg"
        const val status_fine = "benicons/ver3/status_fine.svg"
        const val status_loading = "benicons/ver3/status_loading.svg"
        const val delete = "benicons/ver3/delete.svg"
        const val tag = "benicons/ver3/tag.svg"
    }

    class ModeColor(light: String, dark: String? = null) {
        val light: Color
        val dark: Color?

        init {
            this.light = Color(light)
            this.dark = if (dark != null) Color(dark) else null
        }

        fun get(): Color {
            return when (mode) {
                Mode.LIGHT -> light
                Mode.DARK -> dark ?: light
            }
        }
    }

    enum class Mode {
        LIGHT,
        DARK
    }

}