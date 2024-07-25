package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.lightGreen
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

fun ChildrenBuilder.viewHistoryButton(party: PartyDetails) = Link {
    to = "/${party.id.value}/history/"
    tabIndex = -1
    draggable = false
    CouplingButton {
        sizeRuleSet = large
        colorRuleSet = lightGreen
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}
