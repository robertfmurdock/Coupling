package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.PropsWithChildren
import react.dom.form
import react.fc
import react.useState

private val styles = useStyles("ConfigForm")

val ConfigForm = fc { props: ConfigFormProps ->
    val onRemove = props.onRemove
    var isSaving by useState(false)
    val onSubmitFunc = { event: Event ->
        event.preventDefault()
        isSaving = true
        props.onSubmit()
    }
    form {
        attrs.onSubmitFunction = onSubmitFunc
        props.children()
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
