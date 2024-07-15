package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.mobOf
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.Duration.Companion.days

class AdjustDatasetForHeatMapTest {

    @Test
    fun willIncludeAllPlayersGiven() = setup(object {
        val data = mapOf<CouplingPair, List<Contribution>>(
            pairOf(stubPlayer().copy(name = "solo")) to emptyList(),
            pairOf(stubPlayer().copy(name = "pair1"), stubPlayer().copy(name = "pair2")) to emptyList(),
            mobOf(
                stubPlayer().copy(name = "mob1"),
                stubPlayer().copy(name = "mob2"),
                stubPlayer().copy(name = "mob3"),
                stubPlayer().copy(name = "mob4"),
            ) to emptyList(),
        )
    }) exercise {
        adjustDatasetForHeatMap(data)
    } verify { result ->
        result.keys.flatten().toSet()
            .assertIsEqualTo(data.keys.flatten().toSet())
    }

    @Test
    fun willFoldMobContributionsIncludingWholePairIntoPairContributions() = setup(object {
        val pair = pairOf(stubPlayer().copy(name = "pair1"), stubPlayer().copy(name = "pair2"))
        val data = mapOf(
            pair to listOf(stubContribution()),
            mobOf(
                stubPlayer().copy(name = "mob1"),
                stubPlayer().copy(name = "mob2"),
                stubPlayer().copy(name = "mob3"),
                more = pair.asArray(),
            ) to listOf(stubContribution(), stubContribution()),
        )
    }) exercise {
        adjustDatasetForHeatMap(data)
    } verify { result ->
        result[pair.toSet()]?.toSet()
            .assertIsEqualTo(result.values.flatten().toSet())
    }

    @Test
    fun evenWhenPairHasNoContributionsWillFoldMobContributionsIncludingWholePairIntoPairContributions() = setup(object {
        val pair = pairOf(stubPlayer().copy(name = "pair1"), stubPlayer().copy(name = "pair2"))
        val data = mapOf<CouplingPair, _>(
            mobOf(
                stubPlayer().copy(name = "mob1"),
                stubPlayer().copy(name = "mob2"),
                stubPlayer().copy(name = "mob3"),
                more = pair.asArray(),
            ) to listOf(stubContribution(), stubContribution()),
        )
    }) exercise {
        adjustDatasetForHeatMap(data)
    } verify { result ->
        result[pair.toSet()]?.toSet()
            .assertIsEqualTo(result.values.flatten().toSet())
    }

    @Test
    fun willNotFoldMobContributionsIncludingSoloIntoSoloContributions() = setup(object {
        val solo = pairOf(stubPlayer().copy(name = "pair1"))
        val data = mapOf(
            solo to listOf(stubContribution()),
            mobOf(
                stubPlayer().copy(name = "mob1"),
                stubPlayer().copy(name = "mob2"),
                stubPlayer().copy(name = "mob3"),
                more = solo.asArray(),
            ) to listOf(stubContribution(), stubContribution()),
        )
    }) exercise {
        adjustDatasetForHeatMap(data)
    } verify { result ->
        result[solo.toSet()]?.toSet()
            .assertIsEqualTo(data[solo]?.toSet())
    }

    @Test
    fun givenOneWeekHottestValueWillBeBasedOnIdealIntegrationTimeForOnePairContinuously() = setup(object {
        val window = JsonContributionWindow.Week
        val workdaysPerWeek = 5
        val excellentIntegrationsPerDay = 4

        val emptyList: Map<Set<Player>, List<Contribution>> = emptyMap()
    }) exercise {
        emptyList.toNivoHeatmapSettings(window)
    } verify { (max) ->
        max.assertIsEqualTo(workdaysPerWeek * excellentIntegrationsPerDay)
    }

    @Test
    fun givenOneMonthHottestValueWillBeBasedOnIdealIntegrationTimeForOnePairContinuously() = setup(object {
        val window = JsonContributionWindow.Month
        val workdaysPerWeek = 5
        val excellentIntegrationsPerDay = 4
        val weeksInMonth = 4

        val emptyList: Map<Set<Player>, List<Contribution>> = emptyMap()
    }) exercise {
        emptyList.toNivoHeatmapSettings(window)
    } verify { (max) ->
        max.assertIsEqualTo(workdaysPerWeek * excellentIntegrationsPerDay * weeksInMonth)
    }

    @Test
    fun givenOneQuarterHottestValueWillBeBasedOnIdealIntegrationTimeForOnePairContinuously() = setup(object {
        val window = JsonContributionWindow.Quarter
        val workdaysPerWeek = 5
        val excellentIntegrationsPerDay = 4
        val weeksInQuarter = 13

        val emptyList: Map<Set<Player>, List<Contribution>> = emptyMap()
    }) exercise {
        emptyList.toNivoHeatmapSettings(window)
    } verify { (max) ->
        max.assertIsEqualTo(workdaysPerWeek * excellentIntegrationsPerDay * weeksInQuarter)
    }

    @Test
    fun givenOneYearHottestValueWillBeBasedOnIdealIntegrationTimeForOnePairContinuously() = setup(object {
        val window = JsonContributionWindow.Year
        val workdaysPerWeek = 5
        val excellentIntegrationsPerDay = 4
        val weeksInYear = 52

        val emptyList: Map<Set<Player>, List<Contribution>> = emptyMap()
    }) exercise {
        emptyList.toNivoHeatmapSettings(window)
    } verify { (max) ->
        max.assertIsEqualTo(workdaysPerWeek * excellentIntegrationsPerDay * weeksInYear)
    }

    @Test
    fun givenAllTimeHottestValueWillBeBasedOnIdealIntegrationTimeForOnePairContinuously() = setup(object {
        val window = JsonContributionWindow.All
        val workdaysPerWeek = 5
        val excellentIntegrationsPerDay = 4
        val weeksInThreeYears = 52 * 3

        val emptyList: Map<Set<Player>, List<Contribution>> = mapOf(
            setOf(stubPlayer()) to listOf(stubContribution().copy(dateTime = Clock.System.now() - (365 * 3).days)),
        )
    }) exercise {
        emptyList.toNivoHeatmapSettings(window)
    } verify { (max) ->
        max.assertIsEqualTo(workdaysPerWeek * excellentIntegrationsPerDay * weeksInThreeYears)
    }
}
