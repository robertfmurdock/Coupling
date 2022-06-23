package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.white
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import csstype.ClassName
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link

fun ChildrenBuilder.pinListButton(party: Party) = Link {
    to = "/${party.id.value}/pins/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, white)) {
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}
