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
                        backgroundColor = StyleConst.Header.BgColorSec.get()
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

                        backgroundColor = StyleConst.Header.BgColorSec.get()
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
                    }

                    ".${StyleConst.Main.Processor.CLASS_EXE}" {
                        width = 100.pct
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        gap = StyleConst.paddingSize
                        background = StyleConst.transparent

                        div {
                            display = Display.flex
                            position = Position.relative
                            flex = 100.pct
                            flexDirection = FlexDirection.row
                            justifyContent = JustifyContent.spaceBetween
                            alignItems = AlignItems.center
                            gap = StyleConst.paddingSize

                            a {
                                flexGrow = number(1.0)
                                textAlign = TextAlign.center
                            }

                            p {
                                cursor = Cursor.pointer
                                display = Display.inlineBlock
                                position = Position.relative
                                flexGrow = number(1.0)
                                textAlign = TextAlign.center
                            }

                            button {
                                cursor = Cursor.pointer
                                flexGrow = number(1.0)
                                borderRadius = StyleConst.borderRadius
                                padding = StyleConst.paddingSize
                                height = 2 * StyleConst.paddingSize + StyleConst.iconSize
                            }

                            span {
                                position = Position.relative
                                display = Display.flex
                                flexGrow = number(0.5)
                                flexDirection = FlexDirection.row
                                gap = StyleConst.paddingSize
                                cursor = Cursor.pointer
                                borderRadius = StyleConst.borderRadius
                                padding = StyleConst.paddingSize
                                height = 2 * StyleConst.paddingSize + StyleConst.iconSize
                            }

                            input {
                                display = Display.block
                                maxWidth = 10.ch
                            }

                            img {
                                filter = StyleConst.Main.Processor.iconFilter
                                width = StyleConst.iconSize
                                height = StyleConst.iconSize
                            }


                        }
                    }

                    ".${StyleConst.Main.Processor.CLASS_REG}" {
                        width = 100.pct
                        display = Display.block
                        position = Position.relative
                        borderRadius = StyleConst.borderRadius
                        background = StyleConst.transparent
                    }

                    ".${StyleConst.Main.Processor.CLASS_MEM}" {
                        width = 100.pct
                        display = Display.block
                        position = Position.relative
                        borderRadius = StyleConst.borderRadius
                        background = StyleConst.transparent

                    }

                    ".${StyleConst.Main.InfoView.CLASS_MD_STYLE}" {
                        whiteSpace = WhiteSpace.pre
                        overflowX = Overflow.scroll

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

                        StyleConst.layoutSwitchMediaQuery {

                        }
                    }

                    table {
                        width = important(100.pct)
                        borderCollapse = BorderCollapse.collapse
                        borderRadius = StyleConst.borderRadius
                        tabSize = 6.ch
                        whiteSpace = WhiteSpace.pre
                        cursor = Cursor.text

                        caption {
                            position = Position.relative
                            paddingBottom = StyleConst.paddingSize
                            textAlign = TextAlign.center
                        }

                        thead {
                            fontSize = StyleConst.Main.Table.FontSizeHead

                            th {
                                position = Position.sticky
                                top = 0.rem
                                textAlign = TextAlign.left
                                /*paddingTop = 1.33.em*/
                                padding = StyleConst.paddingSize
                                verticalAlign = VerticalAlign.bottom
                            }

                            td {
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.bottom
                            }

                            button {
                                cursor = Cursor.pointer

                                padding = Padding(0.2.rem, 0.5.rem)
                                borderRadius = StyleConst.borderRadius

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
                            fontSize = StyleConst.Main.Table.FontSizeBody
                            width = 100.pct

                            tr {
                                width = 100.pct
                                display = Display.block

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

                    ".${StyleConst.Main.Window.CLASS}" {

                        display = Display.flex
                        flexDirection = FlexDirection.column
                        position = Position.fixed
                        zIndex = StyleConst.Main.Window.ZIndex
                        top = 0.rem
                        left = 0.rem
                        minWidth = 30.rem
                        height = important(100.vh)
                        boxShadow = BoxShadow(0.px, 5.px, 15.px, rgb(0, 0, 0, 0.35))

                        color = StyleConst.Main.Window.FgColor
                        background = StyleConst.Main.Window.BgColor

                        div {
                            whiteSpace = WhiteSpace.nowrap
                            height = important(Length.fitContent)
                        }

                        ".${StyleConst.Main.Window.CLASS_HEADER}" {
                            display = Display.flex
                            position = Position.relative
                            justifyContent = JustifyContent.normal
                            alignItems = AlignItems.center
                            width = 100.pct
                            padding = StyleConst.Main.Window.paddingHeader

                            button {
                                background = StyleConst.transparent
                                cursor = Cursor.pointer
                                padding = StyleConst.Main.Window.IconPadding
                                borderRadius = StyleConst.Main.Window.IconBorderRadius
                                float = Float.right

                                img {
                                    width = StyleConst.Main.Window.IconSize
                                    height = StyleConst.Main.Window.IconSize
                                    filter = invert(100.pct)
                                }
                            }

                            a {
                                float = Float.left
                            }
                        }

                        ".${StyleConst.Main.Window.CLASS_INFO}" {
                            display = Display.block
                            padding = StyleConst.Main.Window.paddingInfo
                        }

                        ".${StyleConst.Main.Window.CLASS_CONTENT}" {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            justifyContent = JustifyContent.start
                            gap = StyleConst.Main.Window.paddingContent
                            alignItems = AlignItems.stretch
                            padding = StyleConst.Main.Window.paddingContent
                            boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 11.px, 8.px, -10.px, StyleConst.Main.Window.FgColor)
                            overflowY = Overflow.scroll
                            flexGrow = number(1.0)

                            div {
                                display = Display.flex
                                flexDirection = FlexDirection.row
                                justifyContent = JustifyContent.spaceEvenly
                                flexWrap = FlexWrap.nowrap
                                alignItems = AlignItems.center
                                padding = StyleConst.Main.Window.paddingContentItems
                                borderRadius = StyleConst.borderRadius
                                gap = StyleConst.Main.Window.paddingContentItems
                                color = StyleConst.Main.Window.FgColor
                                backgroundColor = StyleConst.Main.Window.BgColorSec

                                button {
                                    background = StyleConst.transparent
                                    cursor = Cursor.pointer
                                    padding = StyleConst.Main.Window.IconPadding
                                    borderRadius = StyleConst.Main.Window.IconBorderRadius
                                    textAlign = TextAlign.center
                                    height = StyleConst.Main.Window.IconSize + 2 * StyleConst.Main.Window.IconPadding

                                    img {
                                        filter = StyleConst.Main.Window.IconFilter
                                        width = StyleConst.Main.Window.IconSize
                                        height = StyleConst.Main.Window.IconSize
                                    }
                                }

                                input {
                                    background = StyleConst.transparent
                                    cursor = Cursor.pointer
                                    padding = StyleConst.Main.Window.IconPadding
                                    borderRadius = StyleConst.Main.Window.IconBorderRadius
                                    color = StyleConst.Main.Window.FgColor
                                    textAlign = TextAlign.center
                                    width = 100.px
                                    cursor = important(Cursor.text)
                                    placeholder {
                                        color = StyleConst.Main.Window.FgColor
                                    }
                                }

                                a {
                                    textAlign = TextAlign.center
                                    flexGrow = number(1.0)
                                }
                            }
                        }

                        StyleConst.layoutSwitchMediaQuery {
                            width = 100.vw
                        }
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