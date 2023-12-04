import web.cssom.*

object StyleAttr {

    // GLOBAL
    val transparent = Color("#00000000")
    val layoutSwitchMediaQuery = "@media (max-width: 1200px)"
    val responsiveQuery = "max-width: 1200px"

    var mode: Mode = Mode.LIGHT

    const val codeFont = "font-family: 'JetBrains Mono', monospace !important;"
    const val CLASS_LOGO = "logo"

    object Header {
        // COLORS
        val BgColor = ModeColor("#1B3C60","#272732")
        val BgColorSec = ModeColor("#5767aa")
        val FgColor = ModeColor("#EEE")

        val IndexNavMobile = integer(20)
        val IndexNavDropDown = integer(21)

        const val CLASS_DROPDOWN = "menu-dropdown"
        const val CLASS_MOBILE_OPEN = "menu-mobile-open"
        const val CLASS_OVERLAY = "menu-overlay"
        const val CLASS_OVERLAY_LABELEDINPUT = "menu-overlay-items"
    }
    object Main {
        val DeleteColor = Color("#EE2222FF")
        val BgColor = ModeColor("#F1F1E6", "#575767")
        val FgColor = ModeColor("#454545", "#D5D5D5")

        val elementShadow = BoxShadow(0.px, 3.px, 6.px, rgb(0, 0, 0, 0.77))

        val DeleteFilter = important(invert(0.pct))

        object Editor {
            val BgColor = ModeColor("#EEEEEE", "#222222")
            val FgColor = ModeColor("#313131", "#AABACA")
            val Font = codeFont
            val FontSize = 16.px

            object Controls {
                val BgColor = ModeColor("#1B3C60", "#272737")
                val FgColor = ModeColor("#EEE")

                val iconSize = 1.8.rem
                val iconPadding = 0.1.rem
                val iconFilter = invert(100.pct)
                val controlSize = 2.0.rem
                val borderRadius = 0.4.rem
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
                base00(ModeColor("#202746", "#f5f7ff")),
                base01(ModeColor("#293256", "#dfe2f1")),
                base02(ModeColor("#5e6687", "#979db4")),
                base03(ModeColor("#6b7394", "#898ea4")),
                base04(ModeColor("#898ea4", "#6b7394")),
                base05(ModeColor("#979db4", "#5e6687")),
                base06(ModeColor("#dfe2f1", "#293256")),
                base07(ModeColor("#f5f7ff", "#202746")),
                red(ModeColor("#c94922")),
                orange(ModeColor("#c76b29")),
                yellow(ModeColor("#c08b30")),
                green(ModeColor("#ac9739")),
                greenPCMark(ModeColor("#008b19")),
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
            val BorderColor = ModeColor("#915A69","#313131")
            val BgColor = ModeColor("#313131","#313131")
            val FgColor = ModeColor("#EEEEEE", "#CCCCCC")
            val iconFilter = important(invert(100.pct))
            val TableFgColor = ModeColor("#313131", "#AABACA")
            val TableBgColor = ModeColor("#EEEEEE", "#222222")
            val TabBgColor = ModeColor("#00000031", "#00000031")
            val TabFgColor = ModeColor("#EEEEEE", "#999999")

            val BgColorTransparent = ModeColor("#77777731","#11111151")

            val BtnFgFilter = invert(100.pct)
            val BtnBgColor = ModeColor("#1B3C60", "#272732")
            val BtnFgColor = ModeColor("#EEE")
            val BtnBgColorDeActivated = ModeColor("#1B3C6031","#27273231")

            val MaxHeightMem = 40.vh
            val MaxHeightReg = 40.vh

            enum class BtnBg(val modeColor: ModeColor) {
                CONTINUOUS(ModeColor("#58CC79", "#19A744")),
                SSTEP(ModeColor("#98D8AA", "#41A05A")),
                MSTEP(ModeColor("#E2B124", "#B68B0F")),
                SOVER(ModeColor("#549FD8", "#126EB4")),
                ESUB(ModeColor("#EE9955", "#AC5916")),
                RESET(ModeColor("#EE2222", "#9A0000"))
                ;

                fun get(): Color {
                    return this.modeColor.get()
                }
            }

            val fontSizeTitle = 1.2.rem
            val fontWeight = FontWeight.bold
            val fontStyle = FontStyle.normal


            const val CLASS = "processor"
            const val CLASS_EXE = "processor-exediv"
            const val CLASS_REG = "processor-regdiv"
            const val CLASS_MEM = "processor-memdiv"

            object Memory{
                const val emptyPlaceholderRows = 10
            }

        }
        object AppControls {
            val BgColor = ModeColor("#1B3C60", "#272732")
            val FgColor = ModeColor("#EEE")
            val BgColorDeActivated = ModeColor("#1B3C6031","#27273231")
            val FgColorDeActivated = ModeColor("#1B3C60", "#EEE")

            val iconSize = 1.8.rem
            val iconPadding = 0.1.rem
            val size = iconSize + 2 * iconPadding
        }
        object InfoView {
            val marginTopH1 = 1.rem
            val marginTop = 0.3.rem
            val marginBottom = 0.1.rem

            val tabSize = 1.rem

            val fontSizeH1 = 2.rem
            val fontSizeH2 = 1.7.rem
            val fontSizeH3 = 1.4.rem
            val fontSizeH4 = 1.1.rem
            val fontSizeStandard = 1.0.rem

            val iconFilter = ModeFilter(invert(0.pct), invert(90.pct))

            enum class Colors(val color: ModeColor) {
                Bg(ModeColor("#77778731")),
                TableBg(ModeColor("#9797A731","#27273731")),
                base00(ModeColor("#202746", "#f5f7ff")),
                base01(ModeColor("#293256", "#dfe2f1")),
                base02(ModeColor("#5e6687", "#979db4")),
                base03(ModeColor("#6b7394", "#898ea4")),
                base04(ModeColor("#898ea4", "#6b7394")),
                base05(ModeColor("#979db4", "#5e6687")),
                base06(ModeColor("#dfe2f1", "#293256")),
                base07(ModeColor("#f5f7ff", "#202746")),
                red(ModeColor("#c94922")),
                orange(ModeColor("#c76b29")),
                yellow(ModeColor("#c08b30")),
                greenOld(ModeColor("#ac9739")),
                green(ModeColor("#008b19")),
                cyan(ModeColor("#22a2c9")),
                blue(ModeColor("#3d8fd1")),
                violet(ModeColor("#6679cc")),
                magenta(ModeColor("#9c637a")),
                whitespace(ModeColor("#B0C4DEFF"));

                fun get(): Color {
                    return this.color.get()
                }
            }
        }
        object Table {
            val BgPC = Color("#008b1966")
            val FgPC = Color("#008b19")

            val BorderColor = Color("#E3E3E2FF")
            val StripeColor = Color("#FFFFFF19")

            val IconSize = 1.8.rem
            val IconPadding = 0.1.rem

            val FontSizeHead = 0.84.em
            val FontSizeBody = 0.84.em
            val FontSizeSelect = 0.7.em

            const val CLASS_TXT_CENTER = "txt-center"
            const val CLASS_TXT_LEFT = "txt-left"
            const val CLASS_TXT_RIGHT = "txt-right"
            const val CLASS_MONOSPACE = "txt-monospace"

            const val CLASS_BORDERED = "table-bordered"
            const val CLASS_STRIPED = "table-striped"

            enum class Mark(val color: ModeColor) {
                PROGRAM(ModeColor("#A040A0")),
                DATA(ModeColor("#40A0A0")),
                EDITABLE(ModeColor("#222222", "#A0A0A0")),
                NOTUSED(ModeColor("#77777731")),
                ELSE(ModeColor("#A0A040"));

                fun get(): Color {
                    return this.color.get()
                }
            }
        }
        object Window {
            const val CLASS = "window"
            const val CLASS_HEADER = "window-header"
            const val CLASS_INFO = "window-info"
            const val CLASS_CONTENT = "window-content"

            const val CLASS_CLEAR = "window-clear-btn"
            const val CLASS_CLOSE = "window-close-btn"
            const val CLASS_TITLE = "window-title"

            val FgColor = Color("#EEEEEE")
            val BgColor = Color("#222222EE")
            val BgColorSec = Color("#777777")

            val IconFilter = invert(90.pct)
            val IconSize = 1.8.rem
            val IconPadding = 0.1.rem
            val IconBorderRadius = 0.3.rem

            val ZIndex = integer(10)

            val paddingHeader = 0.5.rem
            val paddingInfo = 1.0.rem
            val paddingContent = 1.0.rem
            val paddingContentItems = 0.2.rem

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

    // ...

    // NUMBERS
    val paddingSize = 0.4.rem
    val borderRadius = 0.3.rem

    val iconSizeNav = 1.8.rem
    val iconFilter = invert(1).also { sepia(0.2) }.also { saturate(1.278) }.also { hueRotate(202.deg) }.also { brightness(1.20) }.also { contrast(0.87) }
    val iconActiveFilter = invert(71).also { sepia(54) }.also { saturate(429) }.also { hueRotate(83.deg) }.also { brightness(89) }.also { contrast(87) }

    val iconSize = 1.8.rem
    val iconPadding = 0.1.rem
    val iconBorderRadius = 0.4.rem

    // FONTS
    val logoFont = "font-family: 'Bungee Shade', cursive"

    // PROCESSOR.FLAGSCONDSVIEW
    val CLASS_PROC_FC_CONTAINER = "proc-fc-container"
    val CLASS_PROC_FC_COND = "proc-fc-cond"
    val CLASS_PROC_FC_FLAG = "proc-fc-flag"
    val CLASS_PROC_FC_COND_ACTIVE = "proc-fc-cond-active"
    val CLASS_PROC_FC_FLAG_ACTIVE = "proc-fc-flag-active"

    // CONSOLE
    val MESSAGE_TYPE_INFO = 0
    val MESSAGE_TYPE_LOG = 1
    val MESSAGE_TYPE_WARN = 2
    val MESSAGE_TYPE_ERROR = 3

    // ANIM
    val ANIM_SHAKERED = "anim-shakered"
    val ANIM_BLINKGREEN = "anim-blinkgreen"

    object Icons {
        const val add = "benicons/add.svg"
        const val autoscroll = "benicons/autoscroll.svg"
        const val backwards = "benicons/backwards.svg"
        const val bars = "benicons/bars.svg"
        const val build = "benicons/build.svg"
        const val cancel = "benicons/cancel.svg"
        const val clear_storage = "benicons/clear_storage.svg"
        const val continuous_exe = "benicons/continuous_exe.svg"
        const val darkmode = "benicons/darkmode.svg"
        const val delete_red = "benicons/delete_red.svg"
        const val delete_black = "benicons/delete_black.svg"
        const val disassembler = "benicons/disassembler.svg"
        const val edit = "benicons/edit.svg"
        const val export = "benicons/export.svg"
        const val file_compiled = "benicons/file_compiled.svg"
        const val file_not_compiled = "benicons/file_not_compiled.svg"
        const val forwards = "benicons/forwards.svg"
        const val home = "benicons/home.svg"
        const val import = "benicons/import.svg"
        const val info = "benicons/info.svg"
        const val lightmode = "benicons/lightmode.svg"
        const val logo = "benicons/logo.svg"
        const val pin = "benicons/pin.svg"
        const val processor = "benicons/processor.svg"
        const val processor_bold = "benicons/processor_bold.svg"
        const val processor_light = "benicons/processor_light.svg"
        const val recompile = "benicons/recompile.svg"
        const val refresh = "benicons/refresh.svg"
        const val report_bug = "benicons/report_bug.svg"
        const val return_subroutine = "benicons/return_subroutine.svg"
        const val reverse = "benicons/reverse.svg"
        const val single_exe = "benicons/single_exe.svg"
        const val status_error = "benicons/status_error.svg"
        const val status_fine = "benicons/status_fine.svg"
        const val status_loading = "benicons/status_loading.svg"
        const val step_into = "benicons/step_into.svg"
        const val step_multiple = "benicons/step_multiple.svg"
        const val step_out = "benicons/step_out.svg"
        const val step_over = "benicons/step_over.svg"
        const val tag = "benicons/tag.svg"
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
    class ModeFilter(val light: FilterFunction, val dark: FilterFunction?) {
        fun get(): FilterFunction {
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