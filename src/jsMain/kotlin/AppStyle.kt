import StyleConst
import emotion.react.Global
import emotion.react.styles
import extendable.ArchConst
import js.import.import
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
import react.dom.html.ReactHTML.pre
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
                                overflow = Overflow.hidden
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                zIndex = StyleConst.Main.Editor.TextField.IndexArea
                                resize = Resize.block
                                color = StyleConst.transparent
                                tabSize = StyleConst.Main.Editor.TextField.tabSize.ch
                                lineHeight = StyleConst.Main.Editor.TextField.lineHeight.px
                                overflowWrap = OverflowWrap.normal
                                paddingLeft = StyleConst.paddingSize
                                background = StyleConst.transparent
                            }

                            ".${StyleConst.Main.Editor.TextField.CLASS_HIGHLIGHTING}" {
                                position = Position.absolute
                                display = Display.block
                                overflow = Overflow.hidden
                                top = 0.rem
                                left = 0.rem
                                minWidth = 100.pct
                                minHeight = 100.pct
                                zIndex = StyleConst.Main.Editor.TextField.IndexHL
                                whiteSpace = WhiteSpace.preWrap
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