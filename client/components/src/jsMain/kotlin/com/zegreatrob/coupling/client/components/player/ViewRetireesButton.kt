package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.yellow
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.viewRetireesButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/players/retired"
    tabIndex = -1
    draggable = false
    CouplingButton {
        sizeRuleSet = large
        colorRuleSet = yellow
        i { this.className = ClassName("fa fa-user-slash") }
        +" Retirees!"
    }
}
