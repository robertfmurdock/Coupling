package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import org.w3c.dom.asList
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    @Test
    fun hasRowOfPlayersToTheSide() = asyncSetup(object : ScopeMint() {
        val players = listOf(
            defaultPlayer.copy(name = "harry"),
            defaultPlayer.copy(name = "larry"),
            defaultPlayer.copy(name = "curly"),
            defaultPlayer.copy(name = "moe"),
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
    fun hasRowOfPlayersAboveHeatmap() = asyncSetup(object : ScopeMint() {
        val players = listOf(
            defaultPlayer.copy(name = "harry"),
            defaultPlayer.copy(name = "larry"),
            defaultPlayer.copy(name = "curly"),
            defaultPlayer.copy(name = "moe"),
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
