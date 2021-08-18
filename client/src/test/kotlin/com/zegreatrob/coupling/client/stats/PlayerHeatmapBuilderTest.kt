package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    private val styles = useStyles("stats/PlayerHeatmap")

    @Test
    fun hasRowOfPlayersToTheSide() = setup(object {
        val players = listOf(
            Player("harry"),
            Player("larry"),
            Player("curly"),
            Player("moe")
        )
        val props = PlayerHeatmapProps(
            tribe = Tribe(TribeId("2")),
            players = players,
            heatmapData = emptyList()
        )
    }) exercise {
        shallow(PlayerHeatmap, props)
    } verify { wrapper ->
        wrapper.find<Any>(".${styles["heatmapPlayersSideRow"]}")
            .find(PlayerCard)
            .map { it.props().player }
            .toList()
            .assertIsEqualTo(players)
    }

    @Test
    fun hasRowOfPlayersAboveHeatmap() = setup(object {
        val players = listOf(
            Player("harry"),
            Player("larry"),
            Player("curly"),
            Player("moe")
        )
        val props = PlayerHeatmapProps(
            tribe = Tribe(TribeId("2")),
            players = players,
            heatmapData = emptyList()
        )
    }) exercise {
        shallow(PlayerHeatmap, props)
    } verify { wrapper ->
        wrapper.find<Any>(".${styles["heatmapPlayersTopRow"]}")
            .find(PlayerCard)
            .map { it.props().player }
            .toList()
            .assertIsEqualTo(players)
    }

}