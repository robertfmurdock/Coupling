package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.TooltipProps
import com.zegreatrob.coupling.client.components.external.nivo.tooltip.BasicTooltip
import com.zegreatrob.coupling.client.components.pairContext
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.player.Player
import react.FC
import react.Key
import react.use
import web.cssom.Angle

val CouplingHeatmapTooltip = FC<TooltipProps> { props ->
    val cell = props.cell
    val pairs = use(pairContext)
    val flatten = pairs.flatten()
    val players = flatten.filter { cell.id.split(".").contains(it.id.value.toString()) }
    val pair = players.toCouplingPair()

    if (cell.formattedValue === null) return@FC

    BasicTooltip {
        id = TiltedPlayerList.create(playerList = pair, children = { tilt: Angle, player: Player ->
            PlayerCard(player, tilt = tilt, size = 35, key = Key(player.id.value.toString()))
        })
        value = cell.formattedValue
        enableChip = true
        color = cell.color
    }
}
