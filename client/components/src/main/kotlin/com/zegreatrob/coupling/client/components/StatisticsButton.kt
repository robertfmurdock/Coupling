package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import csstype.ClassName
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link

fun ChildrenBuilder.statisticsButton(party: Party) = Link {
    to = "/${party.id.value}/statistics"
    tabIndex = -1
    draggable = false
    add(
        com.zegreatrob.coupling.client.components.CouplingButton(
            com.zegreatrob.coupling.client.components.large,
            com.zegreatrob.coupling.client.components.blue,
        ),
    ) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}
