import emotion.react.Global
import emotion.react.styles
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.body
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.main
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tfoot
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
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
                        width = StyleAttr.iconSizeNav
                        height = StyleAttr.iconSizeNav
                    }

                    button {
                        padding = 5.px
                        cursor = Cursor.pointer
                        background = StyleAttr.transparent
                        border = Border(0.px, LineStyle.hidden)
                        outline = Outline(0.px, LineStyle.hidden)
                        visibility = Visibility.hidden

                        StyleAttr.layoutSwitchMediaQuery {
                            visibility = Visibility.visible
                        }
                    }

                    ".${StyleAttr.Header.CLASS_DROPDOWN}" {
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
                        transitionDuration = 0.3.s
                        zIndex = StyleAttr.Header.IndexNavDropDown
                        backgroundColor = StyleAttr.Header.BgColorSec.get()
                    }

                    StyleAttr.layoutSwitchMediaQuery {
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
                            transition = 0.3.s
                            transform = translatey((-100).vh)
                            zIndex = StyleAttr.Header.IndexNavMobile
                            visibility = Visibility.hidden
                        }

                        ".${StyleAttr.Header.CLASS_MOBILE_OPEN}" {
                            transform = translatey(0.vh)
                            visibility = Visibility.visible
                        }
                    }
                }

                main {
                    width = 100.pct

                    // COMPONENTS

                    ".${StyleAttr.Main.Editor.CLASS}" {
                        display = Display.flex
                        position = Position.absolute
                        flexDirection = FlexDirection.row
                        height = 100.pct
                        width = 100.pct
                        borderRadius = StyleAttr.borderRadius
                        //gap = StyleAttr.paddingSize
                    }

                    ".${StyleAttr.Main.Editor.TextField.CLASS}" {

                        StyleAttr.Main.Editor.Font
                        fontSize = important(StyleAttr.Main.Editor.FontSize)

                        ".${StyleAttr.Main.Editor.TextField.CLASS_TABS}" {
                            display = Display.flex
                            justifyContent = JustifyContent.start
                            borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.Editor.TextField.TabBorderColor)
                            flexWrap = FlexWrap.wrap
                        }

                        ".${StyleAttr.Main.Editor.TextField.CLASS_TAB}" {
                            display = Display.flex
                            justifyContent = JustifyContent.spaceBetween
                            gap = 0.5.rem
                            padding = Padding(0.3.rem, 0.7.rem)
                            cursor = Cursor.pointer
                            color = StyleAttr.Main.Editor.TextField.TabFgColor

                            input {
                                backgroundColor = StyleAttr.transparent
                                color = StyleAttr.Main.Editor.TextField.TabFgColor
                                border = Border(1.px, LineStyle.solid, StyleAttr.Main.Editor.TextField.TabFgColor)
                                borderRadius = StyleAttr.borderRadius
                                textAlign = TextAlign.center
                            }

                            img {
                                width = StyleAttr.Main.Editor.TextField.TabIconSize
                                height = StyleAttr.Main.Editor.TextField.TabIconSize
                                background = StyleAttr.transparent
                                filter = invert(50.pct)
                                cursor = Cursor.pointer
                            }
                            hover {
                                backgroundColor = StyleAttr.Main.Editor.TextField.TabActiveBgColor
                            }
                        }

                        ".${StyleAttr.Main.Editor.TextField.CLASS_TAB_ACTIVE}" {
                            backgroundColor = StyleAttr.Main.Editor.TextField.TabActiveBgColor
                        }

                        ".${StyleAttr.Main.Editor.TextField.CLASS_SCROLL_CONTAINER}" {
                            scrollBehavior = ScrollBehavior.smooth
                            overflow = Overflow.scroll
                            display = Display.flex
                            maxHeight = 100.pct

                            ".${StyleAttr.Main.Editor.TextField.CLASS_INPUT_DIV}" {
                                position = Position.relative
                                display = Display.flex
                                flexGrow = number(1.0)

                            }

                            ".${StyleAttr.Main.Editor.TextField.CLASS_AREA}" {
                                position = Position.absolute
                                display = Display.block
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                overflowX = Overflow.clip
                                zIndex = StyleAttr.Main.Editor.TextField.IndexArea
                                color = StyleAttr.transparent
                                tabSize = StyleAttr.Main.Editor.TextField.tabSize.ch
                                lineHeight = StyleAttr.Main.Editor.TextField.lineHeight.px
                                paddingLeft = StyleAttr.paddingSize
                                background = StyleAttr.transparent
                                whiteSpace = WhiteSpace.pre
                                overflowWrap = OverflowWrap.normal
                            }

                            ".${StyleAttr.Main.Editor.TextField.CLASS_HIGHLIGHTING}" {
                                position = Position.absolute
                                display = Display.block
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                zIndex = StyleAttr.Main.Editor.TextField.IndexHL
                                whiteSpace = WhiteSpace.preWrap
                                overflowWrap = important(OverflowWrap.normal)
                            }
                        }
                    }

                    ".${StyleAttr.Main.Editor.Transcript.CLASS}" {
                        position = Position.relative
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        justifyContent = JustifyContent.center
                        alignItems = AlignItems.start
                        height = 100.pct
                    }

                    ".${StyleAttr.Main.Processor.CLASS_EXE}" {
                        width = 100.pct
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        //gap = 0.px
                        background = StyleAttr.transparent

                        div {
                            display = Display.flex
                            position = Position.relative
                            flex = 100.pct
                            flexDirection = FlexDirection.row
                            flexWrap = FlexWrap.wrap
                            justifyContent = JustifyContent.spaceBetween
                            alignItems = AlignItems.center
                            backgroundColor = StyleAttr.Main.LineColor.get()
                            gap = 1.px
                            paddingBottom = 1.px
                            //gap = StyleAttr.paddingSize

                            StyleAttr.layoutSwitchMediaQuery {
                                paddingTop = 1.px
                            }

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
                                //borderRadius = StyleAttr.borderRadius
                                padding = StyleAttr.paddingSize
                                height = 2 * StyleAttr.paddingSize + StyleAttr.iconSize
                            }

                            span {
                                position = Position.relative
                                display = Display.flex
                                flexGrow = number(0.5)
                                flexDirection = FlexDirection.row
                                gap = StyleAttr.paddingSize
                                cursor = Cursor.pointer
                                //borderRadius = StyleAttr.borderRadius
                                padding = StyleAttr.paddingSize
                                height = 2 * StyleAttr.paddingSize + StyleAttr.iconSize
                            }

                            input {
                                display = Display.block
                                maxWidth = 10.ch
                            }

                            img {
                                filter = StyleAttr.Main.Processor.iconFilter
                                width = StyleAttr.iconSize
                                height = StyleAttr.iconSize
                            }


                        }
                    }

                    ".${StyleAttr.Main.Processor.CLASS_REG}" {
                        width = 100.pct
                        display = Display.block
                        position = Position.relative
                        borderRadius = StyleAttr.borderRadius
                        background = StyleAttr.transparent
                    }

                    ".${StyleAttr.Main.Processor.CLASS_MEM}" {
                        width = 100.pct
                        display = Display.block
                        position = Position.relative
                        borderRadius = StyleAttr.borderRadius
                        background = StyleAttr.transparent
                        flexGrow = number(1.0)
                        minHeight = 0.px
                    }

                    table {
                        width = important(100.pct)
                        borderCollapse = BorderCollapse.collapse
                        borderRadius = StyleAttr.borderRadius
                        overflow = Overflow.hidden
                        tabSize = 6.ch
                        whiteSpace = WhiteSpace.pre
                        cursor = Cursor.text

                        caption {
                            position = Position.relative
                            paddingBottom = StyleAttr.paddingSize
                            textAlign = TextAlign.center
                        }

                        thead {
                            fontSize = StyleAttr.Main.Table.FontSizeHead


                            th {
                                position = Position.sticky
                                top = 0.rem
                                textAlign = TextAlign.left
                                /*paddingTop = 1.33.em*/
                                padding = StyleAttr.paddingSize
                                verticalAlign = VerticalAlign.bottom
                            }

                            td {
                                paddingBottom = 0.25.em
                                verticalAlign = VerticalAlign.bottom
                            }

                            button {
                                cursor = Cursor.pointer

                                padding = Padding(0.2.rem, 0.5.rem)
                                borderRadius = StyleAttr.borderRadius

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
                            fontSize = StyleAttr.Main.Table.FontSizeBody

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
                                borderRadius = StyleAttr.borderRadius
                                backgroundColor = StyleAttr.transparent
                                textAlign = TextAlign.center
                            }
                        }

                        tfoot {
                            fontSize = StyleAttr.Main.Table.FontSizeBody
                            width = 100.pct

                            tr {
                                width = 100.pct
                                display = Display.block

                            }
                        }

                        ".${StyleAttr.Main.Table.CLASS_TXT_LEFT}" {
                            textAlign = important(TextAlign.left)
                            verticalAlign = important(VerticalAlign.middle)
                        }
                        ".${StyleAttr.Main.Table.CLASS_TXT_CENTER}" {
                            textAlign = important(TextAlign.center)
                            verticalAlign = important(VerticalAlign.middle)
                        }
                        ".${StyleAttr.Main.Table.CLASS_TXT_RIGHT}" {
                            textAlign = important(TextAlign.right)
                            verticalAlign = important(VerticalAlign.middle)
                        }
                        ".${StyleAttr.Main.Table.CLASS_MONOSPACE}" {
                            StyleAttr.codeFont
                        }
                    }

                    ".${StyleAttr.Main.Table.CLASS_BORDERED}" {
                        border = Border(1.px, LineStyle.solid, StyleAttr.Main.Table.BorderColor)
                        td {
                            border = Border(1.px, LineStyle.solid, StyleAttr.Main.Table.BorderColor)
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        th {
                            border = Border(1.px, LineStyle.solid, StyleAttr.Main.Table.BorderColor)
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        "tr:not(:last-child)" {
                            borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.Table.BorderColor)
                        }
                    }

                    ".${StyleAttr.Main.Table.CLASS_STRIPED}" {
                        td {
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        th {
                            paddingLeft = 0.2.em
                            paddingRight = 0.2.em
                        }
                        "tr:not(:last-child)" {
                            borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.Table.BorderColor)
                        }
                        tbody {
                            "tr:nth-of-type(2n)" {
                                backgroundColor = StyleAttr.Main.Table.StripeColor
                            }
                        }
                    }

                    ".${StyleAttr.Main.Window.CLASS}" {

                        display = Display.flex
                        flexDirection = FlexDirection.column
                        position = Position.fixed
                        zIndex = StyleAttr.Main.Window.ZIndex
                        top = 0.rem
                        left = 0.rem
                        minWidth = 30.rem
                        height = important(100.vh)
                        boxShadow = BoxShadow(0.px, 5.px, 15.px, rgb(0, 0, 0, 0.35))

                        color = StyleAttr.Main.Window.FgColor
                        background = StyleAttr.Main.Window.BgColor

                        div {
                            whiteSpace = WhiteSpace.nowrap
                            height = important(Length.fitContent)
                        }

                        ".${StyleAttr.Main.Window.CLASS_HEADER}" {
                            display = Display.flex
                            position = Position.relative
                            justifyContent = JustifyContent.normal
                            alignItems = AlignItems.center
                            width = 100.pct
                            padding = StyleAttr.Main.Window.paddingHeader

                            button {
                                background = StyleAttr.transparent
                                cursor = Cursor.pointer
                                padding = StyleAttr.Main.Window.IconPadding
                                borderRadius = StyleAttr.Main.Window.IconBorderRadius
                                float = Float.right

                                img {
                                    width = StyleAttr.Main.Window.IconSize
                                    height = StyleAttr.Main.Window.IconSize
                                    filter = invert(100.pct)
                                }
                            }

                            a {
                                float = Float.left
                            }
                        }

                        ".${StyleAttr.Main.Window.CLASS_INFO}" {
                            display = Display.block
                            padding = StyleAttr.Main.Window.paddingInfo
                        }

                        ".${StyleAttr.Main.Window.CLASS_CONTENT}" {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            justifyContent = JustifyContent.start
                            gap = StyleAttr.Main.Window.paddingContent
                            alignItems = AlignItems.stretch
                            padding = StyleAttr.Main.Window.paddingContent
                            boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 11.px, 8.px, (-10).px, StyleAttr.Main.Window.FgColor)
                            overflowY = Overflow.scroll
                            flexGrow = number(1.0)

                            div {
                                display = Display.flex
                                flexDirection = FlexDirection.row
                                justifyContent = JustifyContent.spaceEvenly
                                flexWrap = FlexWrap.nowrap
                                alignItems = AlignItems.center
                                padding = StyleAttr.Main.Window.paddingContentItems
                                borderRadius = StyleAttr.borderRadius
                                gap = StyleAttr.Main.Window.paddingContentItems
                                color = StyleAttr.Main.Window.FgColor
                                backgroundColor = StyleAttr.Main.Window.BgColorSec

                                button {
                                    background = StyleAttr.transparent
                                    cursor = Cursor.pointer
                                    padding = StyleAttr.Main.Window.IconPadding
                                    borderRadius = StyleAttr.Main.Window.IconBorderRadius
                                    textAlign = TextAlign.center
                                    height = StyleAttr.Main.Window.IconSize + 2 * StyleAttr.Main.Window.IconPadding

                                    img {
                                        filter = StyleAttr.Main.Window.IconFilter
                                        width = StyleAttr.Main.Window.IconSize
                                        height = StyleAttr.Main.Window.IconSize
                                    }
                                }

                                input {
                                    background = StyleAttr.transparent
                                    cursor = Cursor.pointer
                                    padding = StyleAttr.Main.Window.IconPadding
                                    borderRadius = StyleAttr.Main.Window.IconBorderRadius
                                    color = StyleAttr.Main.Window.FgColor
                                    textAlign = TextAlign.center
                                    width = 100.px
                                    cursor = important(Cursor.text)
                                    placeholder {
                                        color = StyleAttr.Main.Window.FgColor
                                    }
                                }

                                a {
                                    textAlign = TextAlign.center
                                    flexGrow = number(1.0)
                                }
                            }
                        }

                        StyleAttr.layoutSwitchMediaQuery {
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
                    gap = StyleAttr.paddingSize
                    overflowX = Overflow.hidden

                    div {
                        img {
                            width = StyleAttr.Footer.iconSize
                            height = StyleAttr.Footer.iconSize
                        }
                    }
                }

                // GLOBAL
                ".${StyleAttr.Header.CLASS_OVERLAY}" {
                    position = Position.fixed
                    bottom = 0.px
                    left = 0.px
                    width = 100.vw
                    zIndex = integer(1000)
                    padding = 1.rem
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.center
                    gap = 2.rem
                    alignItems = AlignItems.center

                    backgroundColor = StyleAttr.Header.BgColorSec.get()
                    color = StyleAttr.Header.FgColorSec.get()

                    img {
                        width = StyleAttr.iconSize
                        height = StyleAttr.iconSize
                        cursor = Cursor.pointer
                        filter = invert(100.pct)
                    }

                    a {
                        cursor = Cursor.pointer
                        padding = StyleAttr.paddingSize
                        background = Color("#00000033")
                        borderRadius = StyleAttr.borderRadius
                    }

                    StyleAttr.layoutSwitchMediaQuery {
                        flexDirection = FlexDirection.column
                    }
                }

                ".${StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT}" {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    justifyContent = JustifyContent.center
                    alignItems = AlignItems.center

                    input {
                        textAlign = TextAlign.center
                        padding = StyleAttr.paddingSize
                        borderRadius = StyleAttr.borderRadius
                    }
                }

                ".${StyleAttr.CLASS_LOGO}" {
                    fontSize = 3.rem
                    StyleAttr.logoFont
                }

                ".${StyleAttr.Main.CLASS_DELETE}" {
                    backgroundColor = important(StyleAttr.Main.DeleteColor.get())
                }
            }
        }
    }
}



