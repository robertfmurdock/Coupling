package com.zegreatrob.coupling.client.components.stats

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.select
import react.dom.html.SelectHTMLAttributes
import web.cssom.Display
import web.cssom.Float
import web.cssom.FontSize
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.em
import web.cssom.pct
import web.cssom.string
import web.html.HTMLSelectElement

external interface CouplingSelectProps : PropsWithChildren {
    var label: ReactNode?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>
}

val CouplingSelect = FC<CouplingSelectProps> { props ->
    CouplingInput {
        label = props.label
        select {
            css {
                display = Display.block
                outline = None.none
                border = None.none
                fontSize = FontSize.larger
                borderRadius = 0.41.em
                width = 100.pct
                padding = 0.35.em
            }
            +props.selectProps
            +props.children
        }
    }
}

external interface CouplingInputProps : PropsWithChildren {
    var label: ReactNode?
}

val CouplingInput = FC<CouplingInputProps> { props ->
    div {
        css {
            display = Display.block
            margin = 1.17.em
        }
        label {
            div {
                css {
                    display = Display.block
                    float = Float.left
                    marginTop = (-1.05).em
                    padding = Padding(0.117.em, 0.29.em)
                    overflow = Overflow.hidden
                    fontFamily = string("Arial, Helvetica, sans-serif")
                }
                +props.label
            }
            +props.children
        }
    }
}
