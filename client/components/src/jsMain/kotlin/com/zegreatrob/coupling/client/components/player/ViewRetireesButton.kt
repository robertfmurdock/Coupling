package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.yellow
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import web.cssom.ClassName

fun ChildrenBuilder.viewRetireesButton(party: PartyDetails) = CouplingLink {
    to = "/${party.id.value}/players/retired"
    sizeRuleSet = large
    colorRuleSet = yellow
    ReactHTML.i { this.className = ClassName("fa fa-user-slash") }
    +" Retirees!"
}
