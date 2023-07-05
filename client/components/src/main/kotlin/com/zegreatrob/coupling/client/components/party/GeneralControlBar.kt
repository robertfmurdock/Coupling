package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.Margin
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.number
import web.cssom.px

external interface GeneralControlBarProps : PropsWithChildren {
    var title: String
    var splashComponent: ReactNode
}

@ReactFunc
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
