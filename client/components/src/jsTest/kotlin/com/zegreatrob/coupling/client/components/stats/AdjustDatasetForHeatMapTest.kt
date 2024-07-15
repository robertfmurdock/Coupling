package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.mobOf
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class AdjustDatasetForHeatMapTest {

    @Test
    fun willIncludeAllPlayersGiven() = setup(object {
        val data = mapOf(
            pairOf(stubPlayer().copy(name = "solo")) to emptyList<Contribution>(),
            pairOf(stubPlayer().copy(name = "pair1"), stubPlayer().copy(name = "pair2")) to emptyList<Contribution>(),
            mobOf(
                stubPlayer().copy(name = "mob1"),
                stubPlayer().copy(name = "mob2"),
                stubPlayer().copy(name = "mob3"),
                stubPlayer().copy(name = "mob4"),
            ) to emptyList<Contribution>(),
        )
    }) exercise {
        adjustDatasetForHeatMap(data)
    } verify { result ->
        result.keys.flatten()
            .assertIsEqualTo(data.keys.flatten())
    }
}
