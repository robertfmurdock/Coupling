package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PartyButtonProps
import com.zegreatrob.coupling.client.components.component1
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.dom.html.ReactHTML.i
import tanstack.react.router.Link
import tanstack.router.core.RoutePath
import web.cssom.ClassName

@ReactFunc
val ContributionVisualizationButton by nfc<PartyButtonProps> { (partyId) ->
    Link {
        to = RoutePath("/${partyId.value}/contributions/visualization")
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = white
            i { this.className = ClassName("fa fa-magnifying-glass-chart") }
            +" "
            +"Visualization"
        }
    }
}
