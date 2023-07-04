package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.pinListButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/pins/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, white)) {
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}
