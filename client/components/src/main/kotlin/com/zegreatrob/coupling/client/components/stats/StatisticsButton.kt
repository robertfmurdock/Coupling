package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.statisticsButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/statistics"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, blue)) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}
