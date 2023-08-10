import emotion.react.Global
import emotion.react.styles
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.body
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h4
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.main
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.center
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tfoot
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.ul
import web.cssom.*

val AppStyle = FC<Props> {
    Global {
        styles {
            body {
                // LAYOUT
                header {
                    backgroundColor = StyleConst.Header.BgColor
                    color = StyleConst.Header.FgColor

                    display = Display.flex
                    alignItems = AlignItems.center
                    justifyContent = JustifyContent.spaceBetween
                    height = 80.px
                    padding = Padding(0.rem, 2.rem)
                    boxShadow = BoxShadow(0.px, 3.px, 6.px, rgb(0, 0, 0, 1.0))

                    a {
                        margin = Margin(0.rem, 2.rem)
                        textDecoration = TextDecoration.solid
                        transition = Transition(TransitionProperty.all, 0.1.s, TransitionTimingFunction.ease)
                        color = StyleConst.Header.FgColor
                        cursor = Cursor.pointer
                        hover {
                            filter = brightness(0.9)
                        }
                    }

                    img {
                        width = StyleConst.iconSizeNav
                        height = StyleConst.iconSizeNav
                        filter = StyleConst.iconFilter
                    }

                    button {
                        padding = 5.px
                        cursor = Cursor.pointer
                        background = StyleConst.transparent
                        border = Border(0.px, LineStyle.hidden)
                        outline = Outline(0.px, LineStyle.hidden)
                        visibility = Visibility.hidden

                        StyleConst.layoutSwitchMediaQuery {
                            visibility = Visibility.visible
                        }
                    }

                    nav {
                        backgroundColor = StyleConst.Header.BgColor
                    }

                    ".${StyleConst.Header.CLASS_DROPDOWN}" {
                        position = Position.fixed
                        top = 0.rem
                        left = 0.rem
                        height = 100.pct
                        width = 100.pct
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        alignItems = AlignItems.center
                        justifyContent = JustifyContent.center
                        gap = 1.5.rem
                        transitionDuration = 1.s
                        zIndex = StyleConst.Header.IndexNavDropDown
                        backgroundColor = StyleConst.Header.BgColorSec
                    }

                    ".${StyleConst.Header.CLASS_OVERLAY}" {
                        position = Position.fixed
                        bottom = 0.px
                        left = 0.px
                        width = 100.vw
                        zIndex = integer(1000)
                        padding = 1.rem
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        gap = 2.rem
                        alignItems = AlignItems.center

                        backgroundColor = StyleConst.Header.BgColorSec
                    }

                    ".${StyleConst.Header.CLASS_OVERLAY_LABELEDINPUT}" {
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        justifyContent = JustifyContent.center
                        alignItems = AlignItems.center
                    }

                    StyleConst.layoutSwitchMediaQuery {
                        nav {
                            position = Position.fixed
                            top = 0.rem
                            left = 0.rem
                            height = 100.pct
                            width = 100.pct
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            alignItems = AlignItems.center
                            justifyContent = JustifyContent.center
                            gap = 1.5.rem
                            transition = 1.s
                            transform = translatey(-100.vh)
                            zIndex = StyleConst.Header.IndexNavMobile
                            visibility = Visibility.hidden
                        }

                        ".${StyleConst.Header.CLASS_MOBILE_OPEN}" {
                            transform = translatey(0.vh)
                            visibility = Visibility.visible
                        }
                    }
                }

                main {
                    width = 100.pct

                    // COMPONENTS

                    ".${StyleConst.Main.Editor.CLASS}" {
                        display = Display.flex
                        position = Position.absolute
                        flexDirection = FlexDirection.row
                        height = 100.pct
                        width = 100.pct
                        borderRadius = StyleConst.borderRadius
                        gap = StyleConst.paddingSize
                    }

                    ".${StyleConst.Main.Editor.Controls.CLASS}" {
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        justifyContent = JustifyContent.start
                        alignItems = AlignItems.start
                        gap = StyleConst.paddingSize
                        minWidth = StyleConst.Main.Editor.Controls.controlSize

                        img {
                            width = StyleConst.Main.Editor.Controls.iconSize
                            height = StyleConst.Main.Editor.Controls.iconSize
                            filter = StyleConst.iconFilter
                        }

                        a {
                            width = StyleConst.Main.Editor.Controls.controlSize
                            boxShadow = BoxShadow(0.px, 3.px, 8.px, rgb(0, 0, 0, 0.24))
                            padding = StyleConst.Main.Editor.Controls.iconPadding
                            borderRadius = StyleConst.Main.Editor.Controls.borderRadius
                            background = StyleConst.Main.Editor.Controls.BgColor
                            transition = Transition(TransitionProperty.all, 0.1.s, TransitionTimingFunction.ease)
                        }

                        ".${StyleConst.Main.Editor.Controls.CLASS_CONTROL}" {
                            height = StyleConst.Main.Editor.Controls.controlSize
                            cursor = Cursor.pointer
                        }

                        ".${StyleConst.Main.Editor.Controls.CLASS_ACTIVE}" {
                            img {
                                filter = important(StyleConst.iconActiveFilter)
                            }
                        }

                        ".${StyleConst.Main.Editor.Controls.CLASS_INFOPANEL}" {
                            cursor = Cursor.pointer
                            position = Position.absolute
                            bottom = 0.rem
                            writingMode = WritingMode.verticalLr
                            display = Display.block
                            color = StyleConst.Main.Editor.Controls.BgHoverColor
                            backgroundColor = StyleConst.Main.Editor.Controls.BgColor

                            img {
                                position = Position.relative
                            }
                        }
                    }

                    ".${StyleConst.Main.Editor.TextField.CLASS}" {

                        StyleConst.Main.Editor.Font
                        fontSize = important(StyleConst.Main.Editor.FontSize)

                        ".${StyleConst.Main.Editor.TextField.CLASS_TABS}" {
                            display = Display.flex
                            justifyContent = JustifyContent.start
                            borderBottom = Border(1.px, LineStyle.solid, StyleConst.Main.Editor.TextField.TabBorderColor)
                            flexWrap = FlexWrap.wrap
                        }

                        ".${StyleConst.Main.Editor.TextField.CLASS_TAB}" {
                            display = Display.flex
                            justifyContent = JustifyContent.spaceBetween
                            gap = 0.5.rem
                            padding = Padding(0.3.rem, 0.7.rem)
                            cursor = Cursor.pointer
                            color = StyleConst.Main.Editor.TextField.TabFgColor

                            input {
                                backgroundColor = StyleConst.transparent
                                color = StyleConst.Main.Editor.TextField.TabFgColor
                                border = Border(1.px, LineStyle.solid, StyleConst.Main.Editor.TextField.TabFgColor)
                                borderRadius = StyleConst.borderRadius
                                textAlign = TextAlign.center
                            }

                            img {
                                width = StyleConst.Main.Editor.TextField.TabIconSize
                                height = StyleConst.Main.Editor.TextField.TabIconSize
                                background = StyleConst.transparent
                                filter = invert(50.pct)
                                cursor = Cursor.pointer
                            }
                            hover {
                                backgroundColor = StyleConst.Main.Editor.TextField.TabActiveBgColor
                            }
                        }

                        ".${StyleConst.Main.Editor.TextField.CLASS_TAB_ACTIVE}" {
                            backgroundColor = StyleConst.Main.Editor.TextField.TabActiveBgColor
                        }

                        ".${StyleConst.Main.Editor.TextField.CLASS_SCROLL_CONTAINER}" {
                            scrollBehavior = ScrollBehavior.smooth
                            overflow = Overflow.scroll
                            display = Display.flex
                            maxHeight = 100.pct

                            ".${StyleConst.Main.Editor.TextField.CLASS_INPUT_DIV}" {
                                position = Position.relative
                                display = Display.flex
                                flexGrow = number(1.0)

                            }

                            ".${StyleConst.Main.Editor.TextField.CLASS_AREA}" {
                                position = Position.absolute
                                display = Display.block
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                overflowX = Overflow.clip
                                zIndex = StyleConst.Main.Editor.TextField.IndexArea
                                color = StyleConst.transparent
                                tabSize = StyleConst.Main.Editor.TextField.tabSize.ch
                                lineHeight = StyleConst.Main.Editor.TextField.lineHeight.px
                                paddingLeft = StyleConst.paddingSize
                                background = StyleConst.transparent
                                whiteSpace = WhiteSpace.pre
                                overflowWrap = OverflowWrap.normal
                            }

                            ".${StyleConst.Main.Editor.TextField.CLASS_HIGHLIGHTING}" {
                                position = Position.absolute
                                display = Display.block
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                zIndex = StyleConst.Main.Editor.TextField.IndexHL
                                whiteSpace = WhiteSpace.preWrap
                                overflowWrap = important(OverflowWrap.normal)
                            }
                        }
                    }

                    ".${StyleConst.Main.Editor.Transcript.CLASS}" {
                        position = Position.relative
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        justifyContent = JustifyContent.center
                        alignItems = AlignItems.start
                        height = 100.pct
                        transition = Transition(TransitionProperty.all, 0.1.s, TransitionTimingFunction.easeInOut)
                    }

                    ".${StyleConst.Main.InfoView.CLASS_MD_STYLE}" {
                        whiteSpace = WhiteSpace.pre

                        h1 {
                            fontSize = StyleConst.Main.InfoView.fontSizeH1
                            marginTop = important(StyleConst.Main.InfoView.marginTop)
                            marginBottom = important(StyleConst.Main.InfoView.marginBottom)
                        }
                        h2 {
                            fontSize = StyleConst.Main.InfoView.fontSizeH2
                            marginTop = important(StyleConst.Main.InfoView.marginTop)
                            marginBottom = important(StyleConst.Main.InfoView.marginBottom)
                        }
                        h3 {
                            fontSize = StyleConst.Main.InfoView.fontSizeH3
                            marginTop = important(StyleConst.Main.InfoView.marginTop)
                            marginBottom = important(StyleConst.Main.InfoView.marginBottom)
                        }
                        h4 {
                            fontSize = StyleConst.Main.InfoView.fontSizeH4
                            marginTop = important(StyleConst.Main.InfoView.marginTop)
                            marginBottom = important(StyleConst.Main.InfoView.marginBottom)
                        }
                        ul {
                            marginTop = important(StyleConst.Main.InfoView.marginTop)
                            marginBottom = important(StyleConst.Main.InfoView.marginBottom)
                        }

                        p {
                            fontWeight = FontWeight.lighter
                        }

                        pre {
                            background = important(StyleConst.Main.InfoView.Colors.Bg.get())
                            borderRadius = StyleConst.borderRadius
                            padding = StyleConst.paddingSize
                        }

                        code {
                            StyleConst.codeFont
                        }

                        table {
                            td {
                                padding = StyleConst.paddingSize
                            }
                            th {
                                paddingRight = StyleConst.paddingSize
                            }
                        }
                    }

                    table {
                        width = important(100.pct)
                        borderCollapse = BorderCollapse.collapse
                        transition = 0.3.s
                        tabSize = 6.ch
                        whiteSpace = WhiteSpace.pre
                        cursor = Cursor.text

                        caption {
                            position = Position.relative
                            fontSize = StyleConst.Main.Table.FontSizeCaption
                            fontWeight = FontWeight.bold
                            padding = 0.56.em

                            a {
                                fontSize = StyleConst.Main.Table.FontSizeCaption
                                fontWeight = FontWeight.bold
                                textAlign = TextAlign.center
                            }
                        }

                        thead {
                            fontSize = StyleConst.Main.Table.FontSizeHead

                            th {
                                textAlign = TextAlign.left
                                /*paddingTop = 1.33.em*/
                                paddingRight = 0.2.em
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.bottom
                            }

                            td {
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.bottom
                            }

                            button {
                                cursor = Cursor.pointer
                                backgroundColor = StyleConst.Main.Table.BgColor
                                boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 2.rem, 50.px, -30.px, StyleConst.Main.AccColor)
                                color = StyleConst.Main.Table.FgColor
                                padding = Padding(0.2.rem, 0.5.rem)
                                borderBottomLeftRadius = StyleConst.borderRadius
                                borderBottomRightRadius = StyleConst.borderRadius

                                span {
                                    margin = Margin(0.rem, 4.ch)
                                }
                                hover {
                                    filter = brightness(120.pct)

                                    "> span" {
                                        margin = Margin(0.rem, 3.ch)
                                        before {
                                            content = Content("[")
                                        }
                                        after {
                                            content = Content("]")
                                        }
                                    }
                                }

                            }
                        }

                        tbody {
                            fontSize = StyleConst.Main.Table.FontSizeBody

                            th {
                                paddingTop = 0.25.em
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.top
                            }

                            td {
                                paddingTop = 0.25.em
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.top
                                textAlign = TextAlign.center
                                paddingRight = 0.2.em
                            }

                            input {
                                width = 100.pct
                                borderRadius = StyleConst.borderRadius
                                backgroundColor = StyleConst.transparent
                                textAlign = TextAlign.center
                            }
                        }

                        tfoot {
                            position = Position.absolute
                            fontSize = StyleConst.Main.Table.FontSizeBody
                            height = 4.rem
                            top = 0.px
                            width = 100.pct

                            tr {
                                width = 100.pct
                            }
                        }

                        ".${StyleConst.Main.Table.CLASS_TXT_LEFT}" {
                            textAlign = important(TextAlign.left)
                        }
                        ".${StyleConst.Main.Table.CLASS_TXT_CENTER}" {
                            textAlign = important(TextAlign.center)
                        }
                        ".${StyleConst.Main.Table.CLASS_TXT_RIGHT}" {
                            textAlign = important(TextAlign.right)
                        }
                        ".${StyleConst.Main.Table.CLASS_MONOSPACE}" {
                            StyleConst.codeFont
                        }

                        ".${StyleConst.Main.Table.CLASS_CONTROL}" {
                            display = Display.flex
                            flexDirection = FlexDirection.row
                            flexWrap = FlexWrap.nowrap
                            justifyContent = JustifyContent.end
                            padding = StyleConst.paddingSize
                            alignItems = AlignItems.center
                            gap = StyleConst.paddingSize

                            "input[type=range]" {
                                display = Display.inlineBlock
                                cursor = Cursor.pointer
                                border = Border(0.px, LineStyle.hidden)
                                height = StyleConst.iconSize + 2 * StyleConst.iconPadding
                                width = StyleConst.Main.Table.RangeWidth
                                float = Float.left
                                minHeight = 1.em
                                borderRadius = StyleConst.iconBorderRadius
                                verticalAlign = VerticalAlign.middle
                                accentColor = StyleConst.Main.Table.BgColor
                            }

                            select {
                                height = StyleConst.Main.Table.IconSize + 2 * StyleConst.Main.Table.IconPadding
                                fontSize = important(StyleConst.Main.Table.FontSizeSelect)
                                fontWeight = FontWeight.lighter
                            }

                            button {
                                display = Display.inlineBlock
                                cursor = Cursor.pointer
                                padding = StyleConst.Main.Table.IconPadding
                                float = Float.left
                                color = StyleConst.Main.Table.FgColor
                                backgroundColor = StyleConst.Main.Table.BgColor
                                borderRadius = StyleConst.iconBorderRadius
                                transition = Transition(TransitionProperty.all, 0.2.s, TransitionTimingFunction.ease)

                                a {
                                    padding = StyleConst.paddingSize
                                }

                                img {
                                    display = Display.block
                                    height = StyleConst.Main.Table.IconSize
                                    width = StyleConst.Main.Table.IconSize
                                    filter = invert(100.pct)
                                }
                            }
                        }
                    }

                    ".${StyleConst.Main.Table.CLASS_BORDERED}" {
                        border = Border(1.px, LineStyle.solid, StyleConst.Main.Table.BorderColor)
                        td {
                            border = Border(1.px, LineStyle.solid, StyleConst.Main.Table.BorderColor)
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        th {
                            border = Border(1.px, LineStyle.solid, StyleConst.Main.Table.BorderColor)
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        "tr:not(:last-child)" {
                            borderBottom = Border(1.px, LineStyle.solid, StyleConst.Main.Table.BorderColor)
                        }
                    }

                    ".${StyleConst.Main.Table.CLASS_STRIPED}" {
                        td {
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        th {
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        "tr:not(:last-child)" {
                            borderBottom = Border(1.px, LineStyle.solid, StyleConst.Main.Table.BorderColor)
                        }
                        tbody {
                            "tr:nth-of-type(2n)" {
                                backgroundColor = StyleConst.Main.Table.StripeColor
                            }
                        }
                    }

                    ".${StyleConst.Main.Table.CLASS_OVERFLOWXSCROLL}" {
                        overflowX = important(Overflow.scroll)
                    }

                }

                footer {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.center
                    alignItems = AlignItems.start
                    textAlign = TextAlign.center
                    padding = 1.rem
                    gap = StyleConst.paddingSize

                    div {
                        img {
                            width = StyleConst.Footer.iconSize
                            height = StyleConst.Footer.iconSize
                        }
                    }
                }

                // GLOBAL
                ".${StyleConst.CLASS_LOGO}" {
                    fontSize = 3.rem
                    StyleConst.logoFont
                }

                ".${StyleConst.Main.CLASS_DELETE}" {
                    backgroundColor = important(StyleConst.Main.DeleteColor)
                }
            }
        }


    }

}