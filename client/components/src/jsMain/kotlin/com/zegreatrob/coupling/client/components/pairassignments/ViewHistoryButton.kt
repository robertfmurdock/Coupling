package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.lightGreen
import com.zegreatrob.coupling.model.party.PartyDetails
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import tanstack.react.router.Link
import tanstack.router.core.RoutePath
import web.cssom.ClassName

fun ChildrenBuilder.viewHistoryButton(party: PartyDetails) = Link {
    to = RoutePath("/${party.id.value}/history/")
    tabIndex = -1
    draggable = false
    CouplingButton {
        sizeRuleSet = large
        colorRuleSet = lightGreen
        ReactHTML.i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}
