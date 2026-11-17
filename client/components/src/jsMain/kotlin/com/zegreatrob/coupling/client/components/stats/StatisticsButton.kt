package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import web.cssom.ClassName

fun ChildrenBuilder.statisticsButton(party: PartyDetails) = CouplingLink {
    to = "/${party.id.value}/statistics"
    sizeRuleSet = large
    colorRuleSet = blue
    ReactHTML.i { this.className = ClassName("fa fa-database") }
    +" Statistics!"
}
