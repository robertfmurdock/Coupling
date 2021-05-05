package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RProps
import react.dom.form
import react.useState

private val styles = useStyles("ConfigForm")

val ConfigForm = reactFunction { props: ConfigFormProps ->
    val (name, onSubmit, onRemove) = props
    form {
        val (isSaving, setIsSaving) = useState(false)
        attrs {
            this.name = name
            onSubmitFunction = onSubmitFunction(setIsSaving, onSubmit)
        }
        props.children()
        configSaveButton(isSaving, styles["saveButton"])
        if (onRemove != null) {
            retireButton(styles["deleteButton"], onRemove)
        }
    }
}

data class ConfigFormProps(
    val name: String = "",
    val onSubmit: () -> Unit,
    val onRemove: (() -> Unit)?
) : RProps

private fun onSubmitFunction(setIsSaving: (Boolean) -> Unit, onSubmit: () -> Unit) = { event: Event ->
    event.preventDefault()
    setIsSaving(true)
    onSubmit()
}