package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div

fun partyPageFunction(handler: ChildrenBuilder.(PageProps, PartyId) -> Unit) = FC<PageProps> { props ->
    val partyId = props.partyId
    if (partyId != null) {
        handler(props, partyId)
    } else {
        div { +"Hey, we're missing the party id. Things have gone terribly, terribly wrong." }
    }
}
