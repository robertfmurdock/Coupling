package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.dom.div

fun tribePageFunction(handler: RBuilder.(PageProps, TribeId) -> Unit) = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null)
        handler(props, tribeId)
    else
        div { +"Hey, we're missing the tribe id. Things have gone terribly, terribly wrong." }
}