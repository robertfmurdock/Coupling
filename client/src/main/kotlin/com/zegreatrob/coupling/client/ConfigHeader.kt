package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RHandler
import react.RProps
import react.dom.div
import react.dom.h1

data class ConfigHeaderProps(val tribe: Tribe) : RProps

fun RBuilder.configHeader(tribe: Tribe, handler: RHandler<ConfigHeaderProps> = {}) =
    child(ConfigHeader, ConfigHeaderProps(tribe), handler)

private val styles = useStyles("ConfigHeader")

val ConfigHeader = reactFunction<ConfigHeaderProps> { props ->
    div(classes = styles.className) {
        div { tribeCard(TribeCardProps(props.tribe, 50)) }
        h1 { props.children() }
    }
}
