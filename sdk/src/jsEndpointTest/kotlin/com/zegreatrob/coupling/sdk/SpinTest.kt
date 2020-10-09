package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class SpinTest {

    @Test
    fun willTakeThePlayersGivenAndUseThoseForPairing() = asyncSetup(sdkContext { context ->
        object : SdkContext by context {
            val tribe = Tribe(id = TribeId(uuid4().toString()), name = "test", pairingRule = PairingRule.LongestTime)
            val players = listOf(
                Player(name = "dude1"),
                Player(name = "dude2")
            )
        }
    }) {
        sdk.save(tribe)
    } exercise {
        sdk.requestSpin(tribe.id, players, emptyList())
    } verify { result ->
        result.pairs.assertIsEqualTo(
            listOf(PinnedCouplingPair(players.map { it.withPins(emptyList()) }))
        )
    }

    @Test
    fun givenTheTribeRuleIsPreferDifferentBadgeThenPairsWillComply() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = Tribe(id = TribeId(uuid4().toString()), pairingRule = PairingRule.PreferDifferentBadge)
            val players = fourPlayersTwoDefaultTwoAlternate()
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 1, 10), pairs = listOf(
                        pairOf(players[0], players[2]).withPins(),
                        pairOf(players[1], players[3]).withPins()
                    )
                ), PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 1, 9), pairs = listOf(
                        pairOf(players[0], players[3]).withPins(),
                        pairOf(players[1], players[2]).withPins()
                    )
                )
            )
        }
    }) {
        setupScenario(sdk, tribe, players, history)
    } exercise {
        sdk.requestSpin(tribe.id, players, emptyList())
    } verify { result ->
        result.pairs.assertIsEqualTo(
            listOf(
                pairOf(players[0], players[3]).withPins(),
                pairOf(players[1], players[2]).withPins()
            )
        )
    }

    @Test
    fun givenTheLongestPairRuleItWillIgnoreBadges() = asyncSetup(object : ScopeMint() {
        val sdk = setupScope.async { authorizedSdk(username = "eT-user-${uuid4()}") }
        val tribe = Tribe(id = TribeId(uuid4().toString()), pairingRule = PairingRule.LongestTime)
        val players = fourPlayersTwoDefaultTwoAlternate()
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 2, 10), pairs = listOf(
                    pairOf(players[0], players[3]).withPins(),
                    pairOf(players[1], players[2]).withPins()
                )
            ), PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 2, 9), pairs = listOf(
                    pairOf(players[0], players[2]).withPins(),
                    pairOf(players[1], players[3]).withPins()
                )
            )
        )
    }) {
        setupScenario(sdk.await(), tribe, players, history)
    } exercise {
        sdk.await().requestSpin(tribe.id, players, emptyList())
    } verify { result ->
        result.pairs.assertIsEqualTo(
            listOf(
                pairOf(players[0], players[1]).withPins(),
                pairOf(players[2], players[3]).withPins()
            )
        )
    }

    class WhenPinExists {

        private val pinExistsSetup
            get() = { context: SdkContext ->
                object : SdkContext by context {
                    val tribe = stubTribe()
                    val players = listOf(stubPlayer())
                    val pin = stubPin()
                }
            }

        @Test
        fun whenAPinExistsWillAssignOnePinToPair() = asyncSetup(sdkContext(pinExistsSetup)) {
            setupScenario(sdk, tribe, players, pins = listOf(pin))
        } exercise {
            sdk.requestSpin(tribe.id, players, listOf(pin))
        } verify { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(listOf(players[0].withPins()), listOf(pin)))
            )
        }

        @Test
        fun whenAPinExistsButIsDeselectedWillNotAssign() = asyncSetup(sdkContext(pinExistsSetup)) {
            setupScenario(sdk, tribe, players, pins = listOf(pin))
        } exercise {
            sdk.requestSpin(tribe.id, players, emptyList())
        } verify { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(listOf(players[0].withPins()), emptyList()))
            )
        }
    }

    private fun fourPlayersTwoDefaultTwoAlternate() = listOf(
        Player(id = monk.id().toString(), name = "One", badge = Badge.Default.value),
        Player(id = monk.id().toString(), name = "Two", badge = Badge.Default.value),
        Player(id = monk.id().toString(), name = "Three", badge = Badge.Alternate.value),
        Player(id = monk.id().toString(), name = "Four", badge = Badge.Alternate.value)
    )

    companion object {
        private suspend fun setupScenario(
            sdk: Sdk,
            tribe: Tribe,
            players: List<Player> = emptyList(),
            history: List<PairAssignmentDocument> = emptyList(),
            pins: List<Pin> = emptyList()
        ) = coroutineScope {
            sdk.save(tribe)
            tribe.id.with(players).forEach { launch { sdk.save(it) } }
            tribe.id.with(history).forEach { launch { sdk.save(it) } }
            tribe.id.with(pins).forEach { launch { sdk.save(it) } }
        }

    }

}