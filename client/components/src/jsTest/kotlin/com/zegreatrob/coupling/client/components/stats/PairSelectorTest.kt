package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import org.w3c.dom.HTMLInputElement
import kotlin.test.Test

class PairSelectorTest {

    @Test
    fun onClickWillOutputSelectedPair() = asyncSetup(object {
        val players = (1..4).map { stubPlayer() }
        val pairs = listOf(
            pairOf(players[0], players[1]),
            pairOf(players[0], players[2]),
            pairOf(players[0], players[3]),
        )
        val actor = UserEvent.setup()
        var selectedPairs: List<CouplingPair> = emptyList()
        val firstPair = pairs[0]
    }) {
        render {
            PairSelector(
                pairs = pairs,
                selectedPairs = selectedPairs,
                onSelectionChange = { newSelectedPairs -> selectedPairs = newSelectedPairs },
            )
        }
    } exercise {
        val pairName = firstPair.pairName
        actor.click(screen.findByRole("checkbox", RoleOptions(pairName)))
    } verify {
        selectedPairs.assertIsEqualTo(listOf(firstPair))
    }

    @Test
    fun whenAlreadySelectedClickWillDeselect() = asyncSetup(object {
        val players = (1..4).map { stubPlayer() }
        val pairs = listOf(
            pairOf(players[0], players[1]),
            pairOf(players[0], players[2]),
            pairOf(players[0], players[3]),
        )
        val actor = UserEvent.setup()
        val firstPair = pairs[0]
        var selectedPairs: List<CouplingPair> = listOf(firstPair)
    }) {
        render {
            PairSelector(
                pairs = pairs,
                selectedPairs = selectedPairs,
                onSelectionChange = { newSelectedPairs -> selectedPairs = newSelectedPairs },
            )
        }
    } exercise {
        actor.click(screen.findByRole("checkbox", RoleOptions(firstPair.pairName)))
    } verify {
        selectedPairs.assertIsEqualTo(emptyList())
    }

    @Test
    fun moreThanOnePairCanBeSelected() = asyncSetup(object {
        val players = (1..4).map { stubPlayer() }
        val pairs = listOf(
            pairOf(players[0], players[1]),
            pairOf(players[0], players[2]),
            pairOf(players[0], players[3]),
        )
        val actor = UserEvent.setup()
        val firstPair = pairs[0]
        val thirdPair = pairs[2]
        var selectedPairs: List<CouplingPair> = listOf(thirdPair)
    }) {
        render {
            PairSelector(
                pairs = pairs,
                selectedPairs = selectedPairs,
                onSelectionChange = { newSelectedPairs -> selectedPairs = newSelectedPairs },
            )
        }
    } exercise {
        actor.click(screen.findByRole("checkbox", RoleOptions(firstPair.pairName)))
    } verify {
        selectedPairs.assertIsEqualTo(listOf(firstPair, thirdPair))
    }

    @Test
    fun showsInitialSelections() = asyncSetup(object {
        val players = (1..4).map { stubPlayer() }
        val pairs = listOf(
            pairOf(players[0], players[1]),
            pairOf(players[0], players[2]),
            pairOf(players[0], players[3]),
        )
        val firstPair = pairs[0]
        val thirdPair = pairs[2]
        var selectedPairs: List<CouplingPair> = listOf(thirdPair)
    }) exercise {
        render {
            PairSelector(
                pairs = pairs,
                selectedPairs = selectedPairs,
                onSelectionChange = { newSelectedPairs -> selectedPairs = newSelectedPairs },
            )
        }
    } verify {
        (screen.findByRole("checkbox", RoleOptions(firstPair.pairName)) as HTMLInputElement).checked
            .assertIsEqualTo(false)
        (screen.findByRole("checkbox", RoleOptions(thirdPair.pairName)) as HTMLInputElement).checked
            .assertIsEqualTo(true)
    }
}
