package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.stats.PairFrequencyControls
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

external interface IncubatingPartyStatisticsProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: List<PlayerPair>
}

@ReactFunc
val IncubatingPartyStatistics by nfc<IncubatingPartyStatisticsProps> { props ->
    val (partyDetails) = props
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = partyDetails
                +"Statistics"
            }
            PairFrequencyControls(props.pairs, view = { data -> PairFrequencyLineGraph.create(data) })
        }
    }
}
