package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName

@ReactFunc
val ContributionVisualizationButton by nfc<PartyButtonProps> { (partyId) ->
    Link {
        to = "/${partyId.value}/contributions/visualization"
        draggable = false
        CouplingButton(large, white, className) {
            i { this.className = ClassName("fa fa-magnifying-glass-chart") }
            +" "
            +"Visualization"
        }
    }
}
