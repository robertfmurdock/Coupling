package com.zegreatrob.coupling.components

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
    add(CouplingButton(large, blue)) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}
