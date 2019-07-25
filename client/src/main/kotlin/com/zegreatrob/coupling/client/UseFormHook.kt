package com.zegreatrob.coupling.client

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.input
import react.dom.label
import kotlin.js.Json
import kotlin.js.json

interface UseFormHook {

    fun useForm(initialValues: Json) = useStateWithSetterFunction(initialValues)
            .let { (values, setValues) ->
                Pair(
                        values,
                        { event: Event ->
                            event.unsafeCast<dynamic>().persist()
                            setValues { previousValues -> previousValues.copyWithChangeFrom(event) }
                        }
                )
            }

    private fun Json.copyWithChangeFrom(event: Event) = json()
            .add(this)
            .add(event.toChangeJson())

    private fun Event.toChangeJson(): Json {
        val target = target.unsafeCast<Json>()
        val name = target["name"].unsafeCast<String>()

        return if (target["type"] == "checkbox") {
            json(name to target["checked"])
        } else {
            json(name to target["value"])
        }
    }

    fun RBuilder.configInput(
            labelText: String,
            id: String,
            name: String,
            value: String,
            type: InputType,
            onChange: (Event) -> Unit,
            placeholder: String = "",
            list: String = "",
            checked: Boolean = false
    ) {
        label { attrs { htmlFor = id }; +labelText }
        input {
            attrs {
                this.name = name
                this.id = id
                this.type = type
                this.value = value
                this.placeholder = placeholder
                this.list = list
                this.checked = checked
                onChangeFunction = onChange
            }
        }
    }
}
