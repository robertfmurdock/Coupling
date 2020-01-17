package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class GameTest {

    @Test
    fun willTakeThePlayersGivenAndUseThoseForPairing() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(id = TribeId(uuid4().toString()), name = "test", pairingRule = PairingRule.LongestTime)
            val players = listOf(
                Player(name = "dude1"),
                Player(name = "dude2")
            )
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            sdk.requestSpin(tribe.id, players)
        } verifyAsync { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(players.map { it.withPins(emptyList()) }))
            )
        }
    }

    @Test
    fun givenTheTribeRuleIsPreferDifferentBadgeThenPairsWillComply() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(id = TribeId(uuid4().toString()), pairingRule = PairingRule.PreferDifferentBadge)
            val players = fourPlayersTwoDefaultTwoAlternate()
            val history = listOf(
                PairAssignmentDocument(
                    DateTime(2014, 1, 10), listOf(
                        pairOf(players[0], players[2]).withPins(),
                        pairOf(players[1], players[3]).withPins()
                    )
                ), PairAssignmentDocument(
                    DateTime(2014, 1, 9), listOf(
                        pairOf(players[0], players[3]).withPins(),
                        pairOf(players[1], players[2]).withPins()
                    )
                )
            )
        }) {
            setupScenario(sdk, tribe, players, history)
        } exerciseAsync {
            sdk.requestSpin(tribe.id, players)
        } verifyAsync { result ->
            result.pairs.assertIsEqualTo(
                listOf(
                    pairOf(players[0], players[3]).withPins(),
                    pairOf(players[1], players[2]).withPins()
                )
            )
        }
    }

    @Test
    fun givenTheLongestPairRuleItWillIgnoreBadges() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(id = TribeId(uuid4().toString()), pairingRule = PairingRule.LongestTime)
            val players = fourPlayersTwoDefaultTwoAlternate()
            val history = listOf(
                PairAssignmentDocument(
                    DateTime(2014, 2, 10), listOf(
                        pairOf(players[0], players[3]).withPins(),
                        pairOf(players[1], players[2]).withPins()
                    )
                ), PairAssignmentDocument(
                    DateTime(2014, 2, 9), listOf(
                        pairOf(players[0], players[2]).withPins(),
                        pairOf(players[1], players[3]).withPins()
                    )
                )
            )
        }) {
            setupScenario(sdk, tribe, players, history)
        } exerciseAsync {
            sdk.requestSpin(tribe.id, players)
        } verifyAsync { result ->
            result.pairs.assertIsEqualTo(
                listOf(
                    pairOf(players[0], players[1]).withPins(),
                    pairOf(players[2], players[3]).withPins()
                )
            )
        }
    }

    @Test
    fun whenAPinExistsWillAssignOnePinToPair() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(id = TribeId(uuid4().toString()), pairingRule = PairingRule.LongestTime)
            val players = listOf(
                Player(id = monk.id().toString(), name = "One", badge = Badge.Default.value)
            )
            val pin = Pin(_id = monk.id().toString(), name = "super test pin")
        }) {
            setupScenario(sdk, tribe, players, pins = listOf(pin))
        } exerciseAsync {
            sdk.requestSpin(tribe.id, players)
        } verifyAsync { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(listOf(players[0].withPins()), listOf(pin)))
            )
        }
    }

    private fun fourPlayersTwoDefaultTwoAlternate() = listOf(
        Player(id = monk.id().toString(), name = "One", badge = Badge.Default.value),
        Player(id = monk.id().toString(), name = "Two", badge = Badge.Default.value),
        Player(id = monk.id().toString(), name = "Three", badge = Badge.Alternate.value),
        Player(id = monk.id().toString(), name = "Four", badge = Badge.Alternate.value)
    )

    private suspend fun setupScenario(
        sdk: AuthorizedSdk,
        tribe: Tribe,
        players: List<Player> = emptyList(),
        history: List<PairAssignmentDocument> = emptyList(),
        pins: List<Pin> = emptyList()
    ) = coroutineScope {
        sdk.save(tribe)
        players.forEach { launch { sdk.save(TribeIdPlayer(tribe.id, it)) } }
        history.forEach { launch { sdk.save(TribeIdPairAssignmentDocument(tribe.id, it)) } }
        pins.forEach { launch { sdk.save(TribeIdPin(tribe.id, it)) } }
    }

}