package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
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
    }) exercise {
        shallow(PlayerHeatmap(players = players, heatmapData = emptyList()))
    } verify { wrapper ->
        wrapper.find<Any>(".${styles["heatmapPlayersSideRow"]}")
            .find(playerCard)
            .map { it.dataprops().player }
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
    }) exercise {
        shallow(PlayerHeatmap(players = players, heatmapData = emptyList()))
    } verify { wrapper ->
        wrapper.find<Any>(".${styles["heatmapPlayersTopRow"]}")
            .find(playerCard)
            .map { it.dataprops().player }
            .toList()
            .assertIsEqualTo(players)
    }
}
