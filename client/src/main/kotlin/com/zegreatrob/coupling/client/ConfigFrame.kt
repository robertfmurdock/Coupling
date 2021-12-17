package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.html.classes
import react.*
import react.dom.attrs
import react.dom.div

private val styles = useStyles("ConfigFrame")

fun RBuilder.configFrame(className: String? = null, handler: RHandler<ConfigFrameProps>) = child(ConfigFrame) {
    attrs.className = className
    handler()
}

external interface ConfigFrameProps : PropsWithClassName, PropsWithChildren

val ConfigFrame = fc<ConfigFrameProps> { props ->
    div(classes = styles.className) {
        attrs { props.className?.let { classes = classes + it } }
        div { props.children() }
    }
}
