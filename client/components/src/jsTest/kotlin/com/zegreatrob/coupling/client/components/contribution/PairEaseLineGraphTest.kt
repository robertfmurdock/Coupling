package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import js.globals.globalThis
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class PairEaseLineGraphTest {
    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object : ScopeMint() {
        val window = ContributionWindow.All
        val data = listOf(
            Pair(
                pairOf(stubPlayer()),
                ContributionReport(
                    partyId = stubPartyId(),
                    count = 0,
                    contributors = emptyList(),
                    contributions = listOf(
                        partyRecord(
                            stubPartyId(),
                            modifyingUserEmail = "-".toNotBlankString().getOrThrow(),
                            data = stubContribution().copy(ease = null),
                        ),
                    ),
                ),
            ),
        )
    }) exercise {
        render {
            PairEaseLineGraph(data, window)
        }
    } verify {
        screen.findByText("No ease data available for this period.")
    }

    @Test
    fun whenDataIsAvailableWillNotShowIndication() = asyncSetup(object : ScopeMint() {
        val window = ContributionWindow.All
        val data = listOf(
            pairOf(stubPlayer()) to report(stubContribution().copy(ease = null)),
            pairOf(stubPlayer()) to report(stubContribution().copy(ease = 2)),
        )
    }) {
        globalThis["ResizeObserver"] = ResizeObserver::class.js
    } exercise {
        render {
            PairEaseLineGraph(data, window)
        }
    } verify {
        screen.findByTestId("coupling-responsive-line")
    }

    @Test
    fun whenNoPairsAreSelectedShowIndication() = asyncSetup(object : ScopeMint() {
        val window = ContributionWindow.All
    }) exercise {
        render {
            PairEaseLineGraph(emptyList(), window)
        }
    } verify {
        screen.findByText("No pairs are selected.")
    }
}

private fun report(data: Contribution): ContributionReport = ContributionReport(
    partyId = stubPartyId(),
    count = 0,
    contributors = emptyList(),
    contributions = listOf(
        partyRecord(
            stubPartyId(),
            modifyingUserEmail = "-".toNotBlankString().getOrThrow(),
            data = data,
        ),
    ),
)

@Suppress("unused")
class ResizeObserver {
    @JsName("observe")
    fun observer() {
    }

    @JsName("disconnect")
    fun disconnect() {
    }
}
