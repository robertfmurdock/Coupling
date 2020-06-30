package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.html.classes
import react.RBuilder
import react.RHandler
import react.RProps
import react.dom.div

private val styles = useStyles("ConfigFrame")

fun RBuilder.configFrame(className: String? = null, handler: RHandler<ConfigFrameProps>) = child(
    ConfigFrame,
    ConfigFrameProps(className),
    handler
)

data class ConfigFrameProps(val className: String?) : RProps

val ConfigFrame = reactFunction<ConfigFrameProps> { props ->
    div(classes = styles.className) {
        attrs { props.className?.let { classes += it } }
        div { props.children() }
    }
}
