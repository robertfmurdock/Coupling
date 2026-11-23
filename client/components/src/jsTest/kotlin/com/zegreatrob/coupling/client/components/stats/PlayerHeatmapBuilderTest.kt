package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.HeatmapCell
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.test.Test

class PlayerHeatmapBuilderTest {

    @Test
    fun changingHeatmapDataUpdatesDescriptionsMarkersAndRemovesOldCells() = asyncSetup(object {
        val players = listOf(stubPlayer(), stubPlayer())
        val initial = listOf(
            listOf(HeatmapCell(null), HeatmapCell(1.0, "Before", true)),
            listOf(HeatmapCell(1.0), HeatmapCell(null)),
        )
        val updated = listOf(listOf(HeatmapCell(7.0, "Updated")))
        lateinit var rendered: Result
        var initialOutline: String? = null
    }) {
        rendered = render { PlayerHeatmap(players, initial) }
        val cell = screen.findByRole("img", RoleOptions(name = "Before"))
        initialOutline = (cell as HTMLElement).style.outline
    } exercise {
        rendered.rerender(PlayerHeatmap.create(players.take(1), updated))
    } verify {
        initialOutline.assertIsEqualTo("2px solid #000")
        val cell = screen.findByRole("img", RoleOptions(name = "Updated"))
        cell.getAttribute("title").assertIsEqualTo("Updated")
        (cell as HTMLElement).style.outline.assertIsEqualTo("none")
        screen.queryByRole("img", RoleOptions(name = "Before")).assertIsEqualTo(null)
        rendered.baseElement.querySelectorAll("[data-heatmap] > div").length.assertIsEqualTo(1)
    }

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
