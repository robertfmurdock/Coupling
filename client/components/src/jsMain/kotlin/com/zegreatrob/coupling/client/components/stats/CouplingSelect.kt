package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.InputHTMLAttributes
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.select
import react.dom.html.SelectHTMLAttributes
import web.cssom.BackgroundColor
import web.cssom.Border
import web.cssom.Color
import web.cssom.Display
import web.cssom.Float
import web.cssom.FontSize
import web.cssom.LineStyle
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.Width
import web.cssom.em
import web.cssom.px
import web.cssom.string
import web.html.HTMLInputElement
import web.html.HTMLSelectElement

external interface CouplingSelectProps : PropsWithChildren {
    var label: ReactNode?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>
    var backgroundColor: BackgroundColor?
}

val CouplingSelect = FC<CouplingSelectProps> { props ->
    CouplingLabelWrapper(props.label, props.backgroundColor) {
        select {
            css {
                display = Display.block
                outline = None.none
                border = None.none
                fontSize = FontSize.larger
                borderRadius = 0.41.em
                width = "calc(100% - 0.7em)".unsafeCast<Width>()
                padding = 0.35.em
            }
            +props.selectProps
            +props.children
        }
    }
}

external interface CouplingLabelWrapperProps : PropsWithChildren {
    var label: ReactNode?
    var backgroundColor: BackgroundColor?
}

@ReactFunc
val CouplingLabelWrapper = FC<CouplingLabelWrapperProps> { props ->
    div {
        css {
            display = Display.block
            margin = 1.17.em
            padding = 0.5.em
            border = Border(1.px, LineStyle.solid, Color("rgb(100 50 50 / 10%)"))
            marginBottom = 30.px
            borderRadius = 3.px
        }
        label {
            div {
                css {
                    backgroundColor = props.backgroundColor
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

external interface CouplingInputProps : Props {
    var label: ReactNode?
    var backgroundColor: BackgroundColor?
    var inputProps: InputHTMLAttributes<HTMLInputElement>
}

@ReactFunc
val CouplingInput = FC<CouplingInputProps> { props ->
    CouplingLabelWrapper(props.label, props.backgroundColor) {
        input {
            css {
                display = Display.block
                outline = None.none
                border = None.none
                fontSize = FontSize.larger
                borderRadius = 0.41.em
                width = "calc(100% - 0.7em)".unsafeCast<Width>()
                padding = 0.35.em
            }
            +props.inputProps
        }
    }
}
