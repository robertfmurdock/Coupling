package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.pinListButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/pins/"
    tabIndex = -1
    draggable = false
    CouplingButton {
        sizeRuleSet = large
        colorRuleSet = white
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}
