package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import react.Props
import react.router.dom.Link
import web.cssom.AnimationIterationCount
import web.cssom.ClassName
import web.cssom.ident
import web.cssom.s

val prepareToSpinButtonClassName = ClassName("prepare-to-spin")

external interface PrepareToSpinButtonProps : Props {
    var party: PartyDetails
}

@ReactFunc
val PrepareToSpinButton by nfc<PrepareToSpinButtonProps> { (party) ->
    Link {
        to = "/${party.id.value}/prepare/"
        tabIndex = -1
        draggable = false
        CouplingButton(
            sizeRuleSet = supersize,
            colorRuleSet = pink,
            className = prepareToSpinButtonClassName,
            css = fun PropertiesBuilder.() {
                animationName = ident("pulsate")
                animationDuration = 2.s
                animationIterationCount = AnimationIterationCount.infinite
                hover {
                    animationDuration = 0.75.s
                }
            },
        ) {
            +"Prepare to spin!"
        }
    }
}
