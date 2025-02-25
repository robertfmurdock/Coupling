package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLineProps
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color
import kotlin.time.Duration

external interface PartyStatisticsProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: List<PlayerPair>
    var spinsUntilFullRotation: Int

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var medianSpinDuration: Duration?
    var chartComponent: FC<CouplingResponsiveLineProps>?
}

@ReactFunc
val PartyStatistics by nfc<PartyStatisticsProps> { props ->
    val (party, players, pairs, spinsUntilFullRotation, medianSpinDuration, chartComponent) = props
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = party
                +"Statistics"
            }
            PartyStatisticsContent(spinsUntilFullRotation, players, medianSpinDuration, pairs, chartComponent)
        }
    }
}
