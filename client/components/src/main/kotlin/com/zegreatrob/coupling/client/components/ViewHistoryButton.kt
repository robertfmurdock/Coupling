package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.viewHistoryButton(party: Party) = Link {
    to = "/${party.id.value}/history/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, lightGreen)) {
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}
