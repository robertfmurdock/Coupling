package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.RequestSpinAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class SpinTest {

    @Test
    fun willTakeThePlayersGivenAndUseThoseForPairing() = sdkSetup.with({ context ->
        object : SdkContext by context {
            val party = Party(id = PartyId(uuid4().toString()), name = "commonTest", pairingRule = PairingRule.LongestTime)
            val players = listOf(
                Player(name = "dude1"),
                Player(name = "dude2")
            )
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        sdk.perform(RequestSpinAction(party.id, players, emptyList()))
    } verifyAnd { result ->
        result.pairs.assertIsEqualTo(
            listOf(PinnedCouplingPair(players.map { it.withPins(emptyList()) }))
        )
    } teardown {
        sdk.partyRepository.deleteIt(party.id)
    }

    @Test
    fun givenThePartyRuleIsPreferDifferentBadgeThenPairsWillComply() = sdkSetup.with({
        object : SdkContext by it {
            val party = Party(id = PartyId(uuid4().toString()), pairingRule = PairingRule.PreferDifferentBadge)
            val players = fourPlayersTwoDefaultTwoAlternate()
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 1, 10),
                    pairs = listOf(
                        pairOf(players[0], players[2]).withPins(),
                        pairOf(players[1], players[3]).withPins()
                    )
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 1, 9),
                    pairs = listOf(
                        pairOf(players[0], players[3]).withPins(),
                        pairOf(players[1], players[2]).withPins()
                    )
                )
            )
        }
    }) {
        setupScenario(sdk, party, players, history)
    } exercise {
        sdk.perform(RequestSpinAction(party.id, players, emptyList()))
    } verifyAnd { result ->
        result.pairs.assertIsEqualTo(
            listOf(
                pairOf(players[0], players[3]).withPins(),
                pairOf(players[1], players[2]).withPins()
            )
        )
    } teardown {
        sdk.partyRepository.deleteIt(party.id)
    }

    @Test
    fun givenTheLongestPairRuleItWillIgnoreBadges() = asyncSetup(object : ScopeMint() {
        val sdk = setupScope.async { authorizedSdk() }
        val party = Party(id = PartyId(uuid4().toString()), pairingRule = PairingRule.LongestTime)
        val players = fourPlayersTwoDefaultTwoAlternate()
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 2, 10),
                pairs = listOf(
                    pairOf(players[0], players[3]).withPins(),
                    pairOf(players[1], players[2]).withPins()
                )
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime(2014, 2, 9),
                pairs = listOf(
                    pairOf(players[0], players[2]).withPins(),
                    pairOf(players[1], players[3]).withPins()
                )
            )
        )
    }) {
        setupScenario(sdk.await(), party, players, history)
    } exercise {
        sdk.await().perform(RequestSpinAction(party.id, players, emptyList()))
    } verifyAnd { result ->
        result.pairs.assertIsEqualTo(
            listOf(
                pairOf(players[0], players[1]).withPins(),
                pairOf(players[2], players[3]).withPins()
            )
        )
    } teardown {
        sdk.await().partyRepository.deleteIt(party.id)
    }

    class WhenPinExists {

        private val pinExistsSetup
            get() = { context: SdkContext ->
                object : SdkContext by context {
                    val party = stubParty()
                    val players = listOf(stubPlayer())
                    val pin = stubPin()
                }
            }

        @Test
        fun whenAPinExistsWillAssignOnePinToPair() = sdkSetup.with({ pinExistsSetup(it) }) {
            setupScenario(sdk, party, players, pins = listOf(pin))
        } exercise {
            sdk.perform(RequestSpinAction(party.id, players, listOf(pin)))
        } verifyAnd { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(listOf(players[0].withPins()), setOf(pin)))
            )
        } teardown {
            sdk.partyRepository.deleteIt(party.id)
        }
        @Test
        fun whenAPinExistsButIsDeselectedWillNotAssign() = sdkSetup.with({ pinExistsSetup(it) }) {
            setupScenario(sdk, party, players, pins = listOf(pin))
        } exercise {
            sdk.perform(RequestSpinAction(party.id, players, emptyList()))
        } verifyAnd { result ->
            result.pairs.assertIsEqualTo(
                listOf(PinnedCouplingPair(listOf(players[0].withPins()), emptySet()))
            )
        } teardown {
            sdk.partyRepository.deleteIt(party.id)
        }
    }

    private fun fourPlayersTwoDefaultTwoAlternate() = listOf(
        Player(id = uuid4().toString(), name = "One", badge = Badge.Default.value),
        Player(id = uuid4().toString(), name = "Two", badge = Badge.Default.value),
        Player(id = uuid4().toString(), name = "Three", badge = Badge.Alternate.value),
        Player(id = uuid4().toString(), name = "Four", badge = Badge.Alternate.value)
    )

    companion object {
        private suspend fun setupScenario(
            sdk: Sdk,
            party: Party,
            players: List<Player> = emptyList(),
            history: List<PairAssignmentDocument> = emptyList(),
            pins: List<Pin> = emptyList()
        ) = coroutineScope {
            sdk.partyRepository.save(party)
            party.id.with(players).forEach { launch { sdk.playerRepository.save(it) } }
            party.id.with(history).forEach { launch { sdk.pairAssignmentDocumentRepository.save(it) } }
            party.id.with(pins).forEach { launch { sdk.pinRepository.save(it) } }
        }
    }
}
