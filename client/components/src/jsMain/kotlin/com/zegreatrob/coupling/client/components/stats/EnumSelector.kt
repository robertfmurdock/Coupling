package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.ChildrenBuilder
import react.Props
import react.ReactNode
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.option
import react.dom.html.SelectHTMLAttributes
import web.cssom.BackgroundColor
import web.html.HTMLSelectElement
import kotlin.enums.enumEntries

external interface EnumSelectorProps<E> : Props {
    var entries: List<E>
    var default: E
    var onChange: (E) -> Unit
    var valueOf: (String) -> E
    var enumName: (E) -> String
    var backgroundColor: BackgroundColor?
    var label: ReactNode?
    var selectProps: SelectHTMLAttributes<HTMLSelectElement>?
}

@ReactFunc
val EnumSelector by nfc<EnumSelectorProps<Any>> { props ->
    CouplingSelect {
        label = props.label
        backgroundColor = props.backgroundColor
        selectProps = jso {
            +props.selectProps
            defaultValue = props.enumName(props.default)
            onChange = { event ->
                event.handlePlaceholder()?.let(props.valueOf)?.let(props.onChange)
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

@Suppress("FunctionName")
inline fun <reified E : Enum<E>> ChildrenBuilder.EnumSelector(
    default: E,
    noinline onChange: (E) -> Unit,
    label: ReactNode,
    backgroundColor: BackgroundColor? = null,
    selectProps: SelectHTMLAttributes<HTMLSelectElement>? = null,
) {
    this@EnumSelector.EnumSelector(
        label = label,
        entries = enumEntries<E>(),
        default = default,
        onChange = onChange,
        valueOf = { enumValueOf<E>(it) },
        enumName = { it.name },
        selectProps = selectProps,
        backgroundColor = backgroundColor,
    )
}
