package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.stats.PairFrequencyControls
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

external interface IncubatingPartyStatisticsContentProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: List<PlayerPair>
    var window: JsonContributionWindow?
}

@ReactFunc
val IncubatingPartyStatisticsContent by nfc<IncubatingPartyStatisticsContentProps> { props ->
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                party = props.party
                +"Statistics"
            }
            PairFrequencyControls(
                pairsContributions = props.pairs.toPairContributions(),
                view = { data -> PairFrequencyLineGraph.create(data, props.window) },
                window = props.window,
            )
        }
    }
}

private fun List<PlayerPair>.toPairContributions(): List<Pair<CouplingPair, List<Contribution>>> = mapNotNull {
    it.players?.elements?.toCouplingPair()
        ?.let { pair -> pair to (it.contributions?.elements ?: emptyList()) }
}
