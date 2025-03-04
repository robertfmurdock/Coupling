package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import org.w3c.dom.asList
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    @Test
    fun hasRowOfPlayersToTheSide() = asyncSetup(object {
        val players = listOf(
            stubPlayer().copy(name = "harry"),
            stubPlayer().copy(name = "larry"),
            stubPlayer().copy(name = "curly"),
            stubPlayer().copy(name = "moe"),
        )
    }) exercise {
        render { PlayerHeatmap(players = players, heatmapData = emptyList()) }
    } verify { wrapper ->
        wrapper.baseElement
            .querySelector(".$heatmapSideRow")!!
            .querySelectorAll("[data-player-id]")
            .asList()
            .map { it.textContent }
            .assertIsEqualTo(players.map { it.name })
    }

    @Test
    fun hasRowOfPlayersAboveHeatmap() = asyncSetup(object {
        val players = listOf(
            stubPlayer().copy(name = "harry"),
            stubPlayer().copy(name = "larry"),
            stubPlayer().copy(name = "curly"),
            stubPlayer().copy(name = "moe"),
        )
    }) exercise {
        render { PlayerHeatmap(players = players, heatmapData = emptyList()) }
    } verify { wrapper ->
        wrapper.baseElement
            .querySelector(".$heatmapTopRowClass")!!
            .querySelectorAll("[data-player-id]")
            .asList()
            .map { it.textContent }
            .assertIsEqualTo(players.map { it.name })
    }
}
