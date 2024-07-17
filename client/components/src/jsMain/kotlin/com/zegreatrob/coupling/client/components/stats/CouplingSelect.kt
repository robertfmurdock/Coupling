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
import web.cssom.pct
import web.cssom.px
import web.cssom.string
import web.html.HTMLSelectElement

external interface CouplingSelectProps : PropsWithChildren {
    var label: ReactNode?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>
}

val CouplingSelect = FC<CouplingSelectProps> { props ->
    div {
        css {
            display = Display.block
            marginTop = 20.px
        }
        label {
            div {
                css {
                    display = Display.block
                    float = Float.left
                    marginTop = (-19).px
                    padding = Padding(2.px, 5.px)
                    overflow = Overflow.hidden
                    fontFamily = string("Arial, Helvetica, sans-serif")
                }
                +props.label
            }
            select {
                css {
                    display = Display.block
                    outline = None.none
                    border = None.none
                    fontSize = FontSize.larger
                    borderRadius = 7.px
                    width = 100.pct
                    padding = 6.px
                }
                +props.selectProps
                +props.children
            }
        }
    }
}
