package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.tribe.Tribe
import react.PropsWithChildren
import react.RBuilder
import react.RHandler
import react.dom.div
import react.dom.h1
import react.functionComponent

external interface ConfigHeaderProps : PropsWithChildren {
    var tribe: Tribe
}

fun RBuilder.configHeader(tribe: Tribe, handler: RHandler<ConfigHeaderProps> = {}) =
    child(ConfigHeader) {
        attrs.tribe = tribe
        handler()
    }

private val styles = useStyles("ConfigHeader")

val ConfigHeader = functionComponent<ConfigHeaderProps> { props ->
    div(classes = styles.className) {
        div { tribeCard(TribeCardProps(props.tribe, 50)) }
        h1 { props.children() }
    }
}
