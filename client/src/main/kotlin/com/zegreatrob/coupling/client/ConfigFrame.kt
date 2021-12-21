package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import react.FC
import react.PropsWithChildren
import react.PropsWithClassName
import react.dom.html.ReactHTML.div

private val styles = useStyles("ConfigFrame")

external interface ConfigFrameProps : PropsWithClassName, PropsWithChildren

val ConfigFrame = FC<ConfigFrameProps> { props ->
    div {
        className = listOfNotNull(styles.className, props.className).joinToString(" ")
        div { props.children() }
    }
}
