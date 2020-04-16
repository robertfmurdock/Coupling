package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

fun tribePageFunction(handler: RBuilder.(PageProps, TribeId) -> Unit) =
    reactFunction<PageProps> { props ->
        val tribeId = props.tribeId
        if (tribeId != null) {
            handler(props, tribeId)
        } else throw Exception("WHAT")
    }