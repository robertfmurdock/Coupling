package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.dom.div
import react.fc

fun tribePageFunction(handler: RBuilder.(PageProps, TribeId) -> Unit) = fc<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null)
        handler(props, tribeId)
    else
        div { +"Hey, we're missing the tribe id. Things have gone terribly, terribly wrong." }
}
