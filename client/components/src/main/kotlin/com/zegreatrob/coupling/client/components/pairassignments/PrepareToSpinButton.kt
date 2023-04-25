package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.router.dom.Link
import web.cssom.AnimationIterationCount
import web.cssom.ClassName
import web.cssom.ident
import web.cssom.s

data class PrepareToSpinButton(val party: Party) : DataPropsBind<PrepareToSpinButton>(prepareToSpinButton)

val prepareToSpinButtonClassName = ClassName("prepare-to-spin")

private val prepareToSpinButton by ntmFC<PrepareToSpinButton> { (party) ->
    Link {
        to = "/${party.id.value}/prepare/"
        tabIndex = -1
        draggable = false
        add(
            CouplingButton(
                sizeRuleSet = supersize,
                colorRuleSet = pink,
                className = prepareToSpinButtonClassName,
                css = {
                    animationName = ident("pulsate")
                    animationDuration = 2.s
                    animationIterationCount = AnimationIterationCount.infinite
                    hover {
                        animationDuration = 0.75.s
                    }
                },
            ),
        ) {
            +"Prepare to spin!"
        }
    }
}
