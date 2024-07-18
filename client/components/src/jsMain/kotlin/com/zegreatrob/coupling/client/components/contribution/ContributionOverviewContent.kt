package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props

external interface ContributionOverviewContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
}

@ReactFunc
val ContributionOverviewContent by nfc<ContributionOverviewContentProps> { (_, contributions) ->
    contributions.forEach { contribution ->
        ContributionCard(contribution, key = contribution.id)
    }
}
