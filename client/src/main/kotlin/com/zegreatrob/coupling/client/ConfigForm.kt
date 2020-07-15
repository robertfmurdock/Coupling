package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.form
import react.useState

private val styles = useStyles("ConfigForm")

fun RBuilder.configForm(
    name: String,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?,
    entityEditor: RBuilder.() -> Unit
) = form {
    val (isSaving, setIsSaving) = useState(false)
    attrs {
        this.name = name
        onSubmitFunction = onSubmitFunction(setIsSaving, onSubmit)
    }
    entityEditor()
    configSaveButton(isSaving, styles["saveButton"])
    if (onRemove != null) {
        retireButton(styles["deleteButton"], onRemove)
    }
}

private fun onSubmitFunction(setIsSaving: (Boolean) -> Unit, onSubmit: () -> Unit) = { event: Event ->
    event.preventDefault()
    setIsSaving(true)
    onSubmit()
}