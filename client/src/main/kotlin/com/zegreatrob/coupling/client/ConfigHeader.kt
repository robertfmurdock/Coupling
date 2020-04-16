package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RHandler
import react.RProps
import react.dom.div
import react.dom.h1

data class ConfigHeaderProps(val tribe: Tribe, val pathSetter: (String) -> Unit) : RProps

fun RBuilder.configHeader(tribe: Tribe, pathSetter: (String) -> Unit, handler: RHandler<ConfigHeaderProps> = {}) =
    child(ConfigHeader.component.rFunction, ConfigHeaderProps(tribe, pathSetter), handler)

private val styles = useStyles("ConfigHeader")

val ConfigHeader = reactFunction<ConfigHeaderProps> { props ->
    val (tribe, pathSetter) = props

    div(classes = styles.className) {
        div { tribeCard(TribeCardProps(tribe, 50, pathSetter)) }
        h1 { props.children() }
    }
}
