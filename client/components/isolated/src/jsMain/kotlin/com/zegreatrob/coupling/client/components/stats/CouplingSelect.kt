package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.InputHTMLAttributes
import react.dom.html.ReactHTML
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
        ReactHTML.select {
            css {
                display = Display.Companion.block
                outline = None.Companion.none
                border = None.Companion.none
                fontSize = FontSize.Companion.larger
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
    ReactHTML.div {
        css {
            display = Display.Companion.block
            margin = 1.17.em
            padding = 0.5.em
            border = Border(0.1.em, LineStyle.Companion.solid, Color("rgb(100 50 50 / 10%)"))
            marginBottom = 0.2.em
            borderRadius = 0.3.em
        }
        ReactHTML.label {
            ReactHTML.div {
                css {
                    backgroundColor = props.backgroundColor
                    display = Display.Companion.block
                    float = Float.Companion.left
                    marginTop = (-1.05).em
                    padding = Padding(0.117.em, 0.29.em)
                    overflow = Overflow.Companion.hidden
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
        ReactHTML.input {
            css {
                display = Display.Companion.block
                outline = None.Companion.none
                border = None.Companion.none
                fontSize = FontSize.Companion.larger
                borderRadius = 0.41.em
                width = "calc(100% - 0.7em)".unsafeCast<Width>()
                padding = 0.35.em
            }
            +props.inputProps
        }
    }
}
