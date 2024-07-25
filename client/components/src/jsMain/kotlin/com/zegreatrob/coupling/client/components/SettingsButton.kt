package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import csstype.PropertiesBuilder
import emotion.react.css
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
    CouplingButton(large, black, className, css = fun PropertiesBuilder.() {
        fontSize = 24.px
        padding = Padding(1.px, 4.px, 2.px)
        "i" {
            margin = 0.px
        }
    }) {
        i { css(ClassName("fa fa-cog")) {} }
    }
}
