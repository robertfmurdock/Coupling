package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.mobOf
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

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
}
