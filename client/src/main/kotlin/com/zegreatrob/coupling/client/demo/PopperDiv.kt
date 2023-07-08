package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
import emotion.react.css
import popper.core.Popper
import popper.core.modifiers.Arrow
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.popper.PopperInstance
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.Globals
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Padding
import web.cssom.Position
import web.cssom.Visibility
import web.cssom.deg
import web.cssom.integer
import web.cssom.px
import web.cssom.rotate
import web.cssom.string
import web.html.HTMLElement
import kotlin.js.Json

fun ChildrenBuilder.popperDiv(
    popperRef: MutableRefObject<HTMLElement>,
    arrowRef: MutableRefObject<HTMLElement>,
    state: DemoAnimationState,
    popperInstance: PopperInstance,
) = div {
    css {
        if (state.description.isBlank()) display = None.none
    }
    div {
        css {
            background = Color("#333333eb")
            color = NamedColor.white
            fontWeight = FontWeight.bold
            padding = Padding(4.px, 8.px)
            fontSize = 24.px
            borderRadius = 20.px
            width = 400.px
            zIndex = integer(200)
            display = Display.inlineBlock

            "h2" {
                fontSize = 30.px
            }
        }
        ref = popperRef
        style = popperInstance.styles[Popper]

        +popperInstance.attributes[Popper]
        Markdown { +state.description }
        if (state.showReturnButton) {
            returnToCouplingButton()
        }
        div {
            css {
                visibility = Visibility.hidden
                position = Position.absolute
                width = 8.px
                height = 8.px
                background = Globals.inherit
                when (
                    popperInstance.attributes[Popper]
                        ?.unsafeCast<Json>()
                        ?.get("data-popper-placement")
                ) {
                    "top" -> bottom = (-4).px
                    "bottom", "bottom-start" -> top = (-4).px
                    "left" -> right = (-4).px
                    "right" -> left = (-4).px
                }
                before {
                    position = Position.absolute
                    width = 8.px
                    height = 8.px
                    background = Globals.inherit
                    visibility = Visibility.visible
                    content = string("''")
                    transform = rotate(45.deg)
                    top = 0.px
                    left = 0.px
                }
            }
            ref = arrowRef
            style = popperInstance.styles[Arrow]
            +popperInstance.attributes[Arrow]
        }
    }
}
