package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.html.classes
import react.RBuilder
import react.RHandler
import react.RProps
import react.dom.div

object ConfigFrame : FRComponent<ConfigFrameProps>(provider()) {

    fun RBuilder.configFrame(className: String? = null, handler: RHandler<ConfigFrameProps>) = child(
        ConfigFrame.component.rFunction,
        ConfigFrameProps(className),
        handler
    )

    val styles = useStyles("ConfigFrame")

    override fun render(props: ConfigFrameProps) = reactElement {
        div(classes = styles.className) {
            attrs { props.className?.let { classes += it } }
            div {
                props.children()
            }
        }
    }
}

data class ConfigFrameProps(val className: String?) : RProps