package com.zegreatrob.coupling.components

import react.FC
import react.PropsWithChildren
import react.dom.events.FormEvent
import react.dom.events.FormEventHandler
import react.dom.html.ReactHTML.form
import react.useCallback
import react.useState

val ConfigForm = FC { props: ConfigFormProps ->
    val onRemove = props.onRemove
    var isSaving by useState(false)
    val onSubmitFunc: FormEventHandler<*> = useCallback(props.onSubmit) { event: FormEvent<*> ->
        event.preventDefault()
        isSaving = true
        props.onSubmit()
    }
    form {
        onSubmit = onSubmitFunc
        +props.children
        configSaveButton(isSaving)
        if (onRemove != null) {
            retireButton(onRemove)
        }
    }
}

external interface ConfigFormProps : PropsWithChildren {
    var onSubmit: () -> Unit
    var onRemove: (() -> Unit)?
}
