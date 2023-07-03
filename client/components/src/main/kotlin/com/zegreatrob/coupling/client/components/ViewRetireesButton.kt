package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.viewRetireesButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/players/retired"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, yellow)) {
        i { this.className = ClassName("fa fa-user-slash") }
        +" Retirees!"
    }
}
