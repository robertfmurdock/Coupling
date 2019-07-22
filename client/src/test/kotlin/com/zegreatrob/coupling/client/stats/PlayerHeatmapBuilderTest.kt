package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findComponent
import loadStyles
import shallow
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    private val styles = loadStyles<PlayerHeatmapStyles>("stats/PlayerHeatmap")

    @Test
    fun hasRowOfPlayersToTheSide() = setup(object : PlayerHeatmapBuilder {
        val players = listOf(
                Player("harry"),
                Player("larry"),
                Player("curly"),
                Player("moe")
        )
        val props = PlayerHeatmapProps(
                tribe = KtTribe(TribeId("2")),
                players = players,
                heatmapData = emptyList()
        )
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.find(".${styles.heatmapPlayersSideRow}")
                .findComponent(PlayerCard)
                .map { it.props().player }
                .toList()
                .assertIsEqualTo(players)
    }

    @Test
    fun hasRowOfPlayersAboveHeatmap() = setup(object : PlayerHeatmapBuilder {
        val players = listOf(
                Player("harry"),
                Player("larry"),
                Player("curly"),
                Player("moe")
        )
        val props = PlayerHeatmapProps(
                tribe = KtTribe(TribeId("2")),
                players = players,
                heatmapData = emptyList()
        )
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.find(".${styles.heatmapPlayersTopRow}")
                .findComponent(PlayerCard)
                .map { it.props().player }
                .toList()
                .assertIsEqualTo(players)
    }

}