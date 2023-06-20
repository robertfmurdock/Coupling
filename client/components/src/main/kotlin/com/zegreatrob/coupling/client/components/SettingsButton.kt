package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName
import web.cssom.Padding
import web.cssom.px

fun ChildrenBuilder.settingsButton(party: PartyDetails, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/edit"
    tabIndex = -1
    draggable = false
    add(
        com.zegreatrob.coupling.client.components.CouplingButton(
            com.zegreatrob.coupling.client.components.large,
            com.zegreatrob.coupling.client.components.black,
            className,
        ) {
            fontSize = 24.px
            padding = Padding(1.px, 4.px, 2.px)
        },
    ) {
        i { this.className = ClassName("fa fa-cog") }
    }
}
