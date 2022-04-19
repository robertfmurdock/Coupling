package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.cssH1
import com.zegreatrob.coupling.client.cssSpan
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.TextAlign
import kotlinx.css.VerticalAlign
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.display
import kotlinx.css.flexGrow
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.verticalAlign
import react.FC
import react.PropsWithChildren
import react.ReactNode

external interface GeneralControlBarProps : PropsWithChildren {
    var title: String
    var splashComponent: ReactNode
}

val GeneralControlBar = FC<GeneralControlBarProps> { props ->
    cssDiv(css = {
        backgroundColor = Color("#faf0d2")
        borderRadius = 50.px
    }) {
        cssDiv(css = {
            display = Display.flex
            textAlign = TextAlign.left
            margin(5.px)
        }) {
            cssSpan(css = {
                margin(10.px)
                display = Display.flex
                alignItems = Align.center
            }) {
                +props.splashComponent
            }
            cssH1(css = { flexGrow = 2.0; display = Display.inlineBlock }) {
                cssDiv(css = {
                    display = Display.flex
                    alignItems = Align.center
                    descendants {
                        verticalAlign = VerticalAlign.middle
                    }
                }) {
                    cssSpan(css = { flexGrow = 2.0; textAlign = TextAlign.left }) {
                        +props.title
                    }
                    cssSpan(css = {
                        margin(0.px, 20.px)
                    }) {
                        +props.children
                    }
                }
            }
        }
    }
}
