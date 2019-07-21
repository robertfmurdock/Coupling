package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.element
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import react.RBuilder
import react.RClass
import react.RProps
import kotlin.js.json


@JsModule("components/statistics/PlayersHeatmap")
@JsNonModule
private external val reactHeatmapModule: dynamic

interface PlayerHeatmapSyntax {

    companion object {
        val rClass = reactHeatmapModule.default.unsafeCast<RClass<RProps>>()
    }

    fun RBuilder.playerHeatmap(props: PlayerHeatmapProps) {
        element(
                rClass,
                json(
                        "tribe" to props.tribe.toJson(),
                        "players" to props.players.map { it.toJson() }.toTypedArray(),
                        "heatmapData" to props.heatmapData.map { it.toTypedArray() }.toTypedArray()
                ).unsafeCast<RProps>()
        )
    }

}

data class PlayerHeatmapProps(
        val tribe: KtTribe,
        val players: List<Player>,
        val heatmapData: List<List<Double?>>
)