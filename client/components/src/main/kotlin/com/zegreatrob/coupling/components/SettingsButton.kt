package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import csstype.ClassName
import csstype.Padding
import csstype.px
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link

fun ChildrenBuilder.settingsButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/edit"
    tabIndex = -1
    draggable = false
    add(
        CouplingButton(large, black, className) {
            fontSize = 24.px
            padding = Padding(1.px, 4.px, 2.px)
        },
    ) {
        i { this.className = ClassName("fa fa-cog") }
    }
}