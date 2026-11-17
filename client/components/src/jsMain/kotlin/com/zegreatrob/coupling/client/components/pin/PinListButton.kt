package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import web.cssom.ClassName

fun ChildrenBuilder.pinListButton(party: PartyDetails) = CouplingLink {
    to = "/${party.id.value}/pins/"
    sizeRuleSet = large
    colorRuleSet = white
    ReactHTML.i { this.className = ClassName("fa fa-peace") }
    +" Pin Bag!"
}
