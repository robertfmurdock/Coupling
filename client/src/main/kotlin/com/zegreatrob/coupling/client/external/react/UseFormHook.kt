package com.zegreatrob.coupling.client.external.react

import react.ChildrenBuilder
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import kotlin.js.Json
import kotlin.js.json

fun useForm(initialValues: Json) = useStateWithSetterFunction(initialValues)
    .let { (values, setValues) ->
        Pair(values, eventHandler(setValues))
    }

private fun eventHandler(setValues: ((Json) -> Json) -> Unit) = { event: ChangeEvent<*> ->
    event.persist()
    setValues { previousValues -> previousValues.copyWithChangeFrom(event) }
}

private inline fun Json.copyWithChangeFrom(event: ChangeEvent<*>) = json()
    .add(this)
    .add(event.toChangeJson())

private fun ChangeEvent<*>.toChangeJson(): Json {
    val target = target.unsafeCast<Json>()
    val name = target["name"].unsafeCast<String>()

    return if (target["type"] == "checkbox") {
        json(name to target["checked"])
    } else {
        json(name to target["value"])
    }
}

fun ChildrenBuilder.configInput(
    labelText: String,
    id: String,
    name: String,
    value: String,
    type: InputType,
    onChange: (ChangeEvent<*>) -> Unit,
    placeholder: String = "",
    list: String = "",
    checked: Boolean = false,
    autoFocus: Boolean? = false
) {
    label { htmlFor = id; +labelText }
    input {
        this.name = name
        this.id = id
        this.type = type
        this.value = value
        this.placeholder = placeholder
        this.list = list
        this.checked = checked
        this.onChange = onChange
        this.autoFocus = autoFocus
    }
}
