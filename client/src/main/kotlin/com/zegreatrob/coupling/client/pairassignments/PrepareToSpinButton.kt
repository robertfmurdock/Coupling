package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.ident
import csstype.s
import react.router.dom.Link

data class PrepareToSpinButton(val party: Party) : DataPropsBind<PrepareToSpinButton>(prepareToSpinButton)

private val styles = useStyles("pairassignments/PairAssignments")

private val prepareToSpinButton = tmFC<PrepareToSpinButton> { (party) ->
    Link {
        to = "/${party.id.value}/prepare/"
        tabIndex = -1
        draggable = false
        add(
            CouplingButton(
                sizeRuleSet = supersize,
                colorRuleSet = pink,
                className = styles["newPairsButton"],
                css = {
                    animationName = ident("pulsate")
                    animationDuration = 2.s
                    animationIterationCount = AnimationIterationCount.infinite
                    hover {
                        animationDuration = 0.75.s
                    }
                }
            )
        ) {
            +"Prepare to spin!"
        }
    }
}
