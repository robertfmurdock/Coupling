package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.lightGreen
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import web.cssom.ClassName

fun ChildrenBuilder.viewHistoryButton(party: PartyDetails) = CouplingLink {
    to = "/${party.id.value}/history/"
    sizeRuleSet = large
    colorRuleSet = lightGreen
    ReactHTML.i { this.className = ClassName("fa fa-history") }
    +" History!"
}
