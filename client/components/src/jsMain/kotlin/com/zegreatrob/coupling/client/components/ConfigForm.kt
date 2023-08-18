package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import react.FC
import react.PropsWithChildren
import react.dom.events.FormEvent
import react.dom.events.FormEventHandler
import react.dom.html.ReactHTML.form
import react.useState

@ReactFunc
val ConfigForm = FC { props: ConfigFormProps ->
    val onRemove = props.onRemove
    var isSaving by useState(false)
    val onSubmitFunc: FormEventHandler<*> = { event: FormEvent<*> ->
        event.preventDefault()
        isSaving = true
        props.onSubmit?.invoke()
    }
    form {
        name = "ConfigForm"
        onSubmit = onSubmitFunc
        +props.children
        if (props.onSubmit != null) {
            configSaveButton(isSaving)
        }
        if (onRemove != null) {
            retireButton(onRemove)
        }
    }
}

external interface ConfigFormProps : PropsWithChildren {
    var onSubmit: (() -> Unit)?
    var onRemove: (() -> Unit)?
}
