package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.viewHistoryButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/history/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, lightGreen)) {
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}
