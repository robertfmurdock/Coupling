package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Props
import react.ReactNode
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.option
import react.dom.html.SelectHTMLAttributes
import web.html.HTMLSelectElement

external interface EnumSelectorProps<E> : Props {
    var entries: List<E>
    var default: E
    var setEnum: (E) -> Unit
    var valueOf: (String) -> E
    var enumName: (E) -> String
    var label: ReactNode?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>?
}

@ReactFunc
val EnumSelector by nfc<EnumSelectorProps<Any>> { props ->
    CouplingSelect {
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

const val NULL_PLACEHOLDER = "NULL"

fun ChangeEvent<HTMLSelectElement>.handlePlaceholder() = target.value.let {
    if (it == NULL_PLACEHOLDER) null else it
}
