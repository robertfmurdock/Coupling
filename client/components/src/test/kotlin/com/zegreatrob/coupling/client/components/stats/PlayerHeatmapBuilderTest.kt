package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import org.w3c.dom.asList
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    @Test
    fun hasRowOfPlayersToTheSide() = asyncSetup(object : ScopeMint() {
        val players = listOf(
            Player(name = "harry", avatarType = null),
            Player(name = "larry", avatarType = null),
            Player(name = "curly", avatarType = null),
            Player(name = "moe", avatarType = null),
        )
    }) exercise {
        render(PlayerHeatmap(players = players, heatmapData = emptyList()).create())
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
            Player(name = "harry", avatarType = null),
            Player(name = "larry", avatarType = null),
            Player(name = "curly", avatarType = null),
            Player(name = "moe", avatarType = null),
        )
    }) exercise {
        render(PlayerHeatmap(players = players, heatmapData = emptyList()).create())
    } verify { wrapper ->
        wrapper.baseElement
            .querySelector(".$heatmapTopRowClass")!!
            .querySelectorAll("[data-player-id]")
            .asList()
            .map { it.textContent }
            .assertIsEqualTo(players.map { it.name })
    }
}
