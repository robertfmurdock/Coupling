package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.tribe.Tribe
import react.PropsWithChildren
import react.dom.div
import react.dom.h1
import react.fc

external interface ConfigHeaderProps : PropsWithChildren {
    var tribe: Tribe
}

private val styles = useStyles("ConfigHeader")

val ConfigHeader = fc<ConfigHeaderProps> { props ->
    div(classes = styles.className) {
        div { tribeCard(TribeCard(props.tribe, 50)) }
        h1 { props.children() }
    }
}
