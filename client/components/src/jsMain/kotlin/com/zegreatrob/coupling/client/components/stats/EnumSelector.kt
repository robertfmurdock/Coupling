package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
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

external interface EnumSelectorProps<E> : Props {
    var entries: List<E>
    var default: E
    var setEnum: (E) -> Unit
    var valueOf: (String) -> E
    var enumName: (E) -> String
    var label: String?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>?
}

@ReactFunc
val EnumSelector by nfc<EnumSelectorProps<Any>> { props ->
    CouplingSelector {
        label = props.label
        selectProps = jso {
            +props.selectProps
            defaultValue = props.enumName(props.default)
            onChange = { event ->
                event.handlePlaceholder()?.let(props.valueOf)?.let(props.setEnum)
            }
        }
        props.entries.map { entry ->
            option {
                value = props.enumName(entry)
                +props.enumName(entry)
            }
        }
    }
}

external interface CouplingSelectorProps : PropsWithChildren {
    var label: String?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>
}

val CouplingSelector = FC<CouplingSelectorProps> { props ->
    val label = props.label
    div {
        css {
            display = Display.block
            marginTop = 30.px
        }
        label {
            div {
                css {
                    display = Display.block
                    float = Float.left
                    marginTop = (-19).px
                    height = 14.px
                    padding = Padding(2.px, 5.px)
                    overflow = Overflow.hidden
                    fontFamily = string("Arial, Helvetica, sans-serif")
                }
                +label
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

const val NULL_PLACEHOLDER = "NULL"

fun ChangeEvent<HTMLSelectElement>.handlePlaceholder() = target.value.let {
    if (it == NULL_PLACEHOLDER) null else it
}
