package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import web.html.HTMLSelectElement

external interface EnumSelectorProps<E> : Props {
    var entries: List<E>
    var default: E
    var setEnum: (E) -> Unit
    var valueOf: (String) -> E
    var enumName: (E) -> String
}

@ReactFunc
val EnumSelector by nfc<EnumSelectorProps<Any>> { props ->
    select {
        defaultValue = props.enumName(props.default)
        onChange = { event ->
            event.handlePlaceholder()?.let(props.valueOf)?.let(props.setEnum)
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
