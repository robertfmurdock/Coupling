package com.zegreatrob.coupling.client

import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.form
import react.useState

fun RBuilder.configForm(name: String, onSubmit: () -> Unit, content: RBuilder.(Boolean) -> Unit) = form {
    val (isSaving, setIsSaving) = useState(false)
    attrs {
        this.name = name
        onSubmitFunction = onSubmitFunction(setIsSaving, onSubmit)
    }
    content(isSaving)
}

private fun onSubmitFunction(setIsSaving: (Boolean) -> Unit, onSubmit: () -> Unit) = { event: Event ->
    event.preventDefault()
    setIsSaving(true)
    onSubmit()
}