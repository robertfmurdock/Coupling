package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div

external interface ContributionCardProps : Props {
    var contribution: Contribution
}

@ReactFunc
val ContributionCard by nfc<ContributionCardProps> { (contribution) ->
    div {
        +"$contribution"
    }
}
