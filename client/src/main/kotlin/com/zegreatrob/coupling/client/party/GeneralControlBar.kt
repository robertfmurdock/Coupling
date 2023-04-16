package com.zegreatrob.coupling.client.party

import com.zegreatrob.minreact.nfc
import csstype.AlignItems
import csstype.Color
import csstype.Display
import csstype.Margin
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.number
import csstype.px
import emotion.react.css
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span

external interface GeneralControlBarProps : PropsWithChildren {
    var title: String
    var splashComponent: ReactNode
}

val GeneralControlBar by nfc<GeneralControlBarProps> { props ->
    div {
        css {
            backgroundColor = Color("#faf0d2")
            borderRadius = 50.px
        }
        div {
            css {
                display = Display.flex
                textAlign = TextAlign.left
                margin = (5.px)
            }
            span {
                css {
                    margin = (10.px)
                    display = Display.flex
                    alignItems = AlignItems.center
                }
                +props.splashComponent
            }
            h1 {
                css { flexGrow = number(2.0); display = Display.inlineBlock }
                div {
                    css {
                        display = Display.flex
                        alignItems = AlignItems.center
                        "*" {
                            verticalAlign = VerticalAlign.middle
                        }
                    }
                    span {
                        css { flexGrow = number(2.0); textAlign = TextAlign.left }
                        +props.title
                    }
                    span {
                        css { margin = Margin(0.px, 20.px) }
                        +props.children
                    }
                }
            }
        }
    }
}
