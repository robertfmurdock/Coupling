package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div

fun partyPageFunction(handler: ChildrenBuilder.(PageProps, PartyId) -> Unit) = FC<PageProps> { props ->
    val tribeId = props.partyId
    if (tribeId != null)
        handler(props, tribeId)
    else
        div { +"Hey, we're missing the tribe id. Things have gone terribly, terribly wrong." }
}
