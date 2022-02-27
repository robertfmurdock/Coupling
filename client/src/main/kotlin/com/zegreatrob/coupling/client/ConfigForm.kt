package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import org.w3c.dom.HTMLFormElement
import react.FC
import react.PropsWithChildren
import react.dom.events.FormEvent
import react.dom.events.FormEventHandler
import react.dom.html.ReactHTML.form
import react.useState

private val styles = useStyles("ConfigForm")

val ConfigForm = FC { props: ConfigFormProps ->
    val onRemove = props.onRemove
    var isSaving by useState(false)
    val onSubmitFunc: FormEventHandler<HTMLFormElement> = { event: FormEvent<HTMLFormElement> ->
        event.preventDefault()
        isSaving = true
        props.onSubmit()
    }
    form {
        onSubmit = onSubmitFunc
        +props.children
        configSaveButton(isSaving, styles["saveButton"])
        if (onRemove != null) {
            retireButton(styles["deleteButton"], onRemove)
        }
    }
}

external interface ConfigFormProps : PropsWithChildren {
    var onSubmit: () -> Unit
    var onRemove: (() -> Unit)?
}
