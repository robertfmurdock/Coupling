package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.forEach
import com.zegreatrob.coupling.model.get
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.uuid.Uuid

class SpinTest {

    @Test
    fun willTakeThePlayersGivenAndUseThoseForPairing() = asyncSetup.with({ context ->
        object : SdkContext by context {
            val party = PartyDetails(
                id = PartyId(Uuid.random().toString()),
                pairingRule = PairingRule.LongestTime,
                name = "commonTest",
            )
            val players = notEmptyListOf(
                stubPlayer().copy(name = "dude1"),
                stubPlayer().copy(name = "dude2"),
            )
        }
    }) {
        coroutineScope {
            sdk.fire(SavePartyCommand(party))
            players.forEach { launch { sdk.fire(SavePlayerCommand(party.id, it)) } }
        }
    } exercise {
        sdk.fire(SpinCommand(party.id, players.map { it.id }, emptyList()))
    } verifyAnd { result ->
        result.assertIsEqualTo(SpinCommand.Result.Success)
        queryCurrentPairs(party.id, sdk)
            ?.pairs
            .assertIsEqualTo(
                notEmptyListOf(PinnedCouplingPair(players.map { it.withPins(emptyList()) })),
            )
    } teardown {
        sdk.fire(DeletePartyCommand(party.id))
    }

    @Test
    fun givenThePartyRuleIsPreferDifferentBadgeThenPairsWillComply() = asyncSetup.with({
        object : SdkContext by it {
            val party = PartyDetails(
                id = PartyId(Uuid.random().toString()),
                pairingRule = PairingRule.PreferDifferentBadge,
            )
            val players = fourPlayersTwoDefaultTwoAlternate()
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${Uuid.random()}"),
                    date = dateTime(2014, 1, 10),
                    pairs = notEmptyListOf(
                        pairOf(players[0], players[2]).withPins(),
                        pairOf(players[1], players[3]).withPins(),
                    ),
                    null,
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${Uuid.random()}"),
                    date = dateTime(2014, 1, 9),
                    pairs = notEmptyListOf(
                        pairOf(players[0], players[3]).withPins(),
                        pairOf(players[1], players[2]).withPins(),
                    ),
                    null,
                ),
            )
        }
    }) {
        setupScenario(sdk, party, players, history)
    } exercise {
        sdk.fire(SpinCommand(party.id, players.map { it.id }, emptyList()))
    } verifyAnd { result ->
        result.assertIsEqualTo(SpinCommand.Result.Success)
        queryCurrentPairs(party.id, sdk)
            ?.pairs
            .assertIsEqualTo(
                notEmptyListOf(
                    pairOf(players[0], players[3]).withPins(),
                    pairOf(players[1], players[2]).withPins(),
                ),
            )
    } teardown {
        sdk.fire(DeletePartyCommand(party.id))
    }

    private fun dateTime(year: Int, month: Int, day: Int) = LocalDateTime(year, month, day, 0, 0, 0)
        .toInstant(TimeZone.UTC)

    @Test
    fun givenTheLongestPairRuleItWillIgnoreBadges() = asyncSetup(object : ScopeMint() {
        val sdk = setupScope.async { sdk() }
        val party = PartyDetails(id = PartyId(Uuid.random().toString()), pairingRule = PairingRule.LongestTime)
        val players = fourPlayersTwoDefaultTwoAlternate()
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${Uuid.random()}"),
                date = dateTime(2013, 2, 10),
                pairs = notEmptyListOf(
                    pairOf(players[0], players[3]).withPins(),
                    pairOf(players[1], players[2]).withPins(),
                ),
                null,
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${Uuid.random()}"),
                date = dateTime(2013, 2, 9),
                pairs = notEmptyListOf(
                    pairOf(players[0], players[2]).withPins(),
                    pairOf(players[1], players[3]).withPins(),
                ),
                null,
            ),
        )
    }) {
        setupScenario(sdk.await(), party, players, history)
    } exercise {
        sdk.await().fire(SpinCommand(party.id, players.map { it.id }, emptyList()))
    } verifyAnd { result ->
        result.assertIsEqualTo(SpinCommand.Result.Success)
        queryCurrentPairs(party.id, sdk.await())
            ?.pairs
            .assertIsEqualTo(
                notEmptyListOf(
                    pairOf(players[0], players[1]).withPins(),
                    pairOf(players[2], players[3]).withPins(),
                ),
            )
    } teardown {
        sdk().fire(DeletePartyCommand(party.id))
    }

    class WhenPinExists {

        private val pinExistsSetup
            get() = { context: SdkContext ->
                object : SdkContext by context {
                    val party = stubPartyDetails()
                    val players = notEmptyListOf(stubPlayer())
                    val pin = stubPin()
                }
            }

        @Test
        fun whenAPinExistsWillAssignOnePinToPair() = asyncSetup.with({ pinExistsSetup(it) }) {
            setupScenario(sdk, party, players, pins = listOf(pin))
        } exercise {
            sdk.fire(SpinCommand(party.id, players.map { it.id }, listOf(pin.id.toString())))
        } verifyAnd { result ->
            result.assertIsEqualTo(SpinCommand.Result.Success)
            queryCurrentPairs(party.id, sdk)
                ?.pairs
                .assertIsEqualTo(
                    notEmptyListOf(PinnedCouplingPair(notEmptyListOf(players[0].withPins()), setOf(pin))),
                )
        } teardown {
            sdk.fire(DeletePartyCommand(party.id))
        }

        @Test
        fun whenAPinExistsButIsDeselectedWillNotAssign() = asyncSetup.with({ pinExistsSetup(it) }) {
            setupScenario(sdk, party, players, pins = listOf(pin))
        } exercise {
            sdk.fire(SpinCommand(party.id, players.map { it.id }, emptyList()))
        } verifyAnd { result ->
            result.assertIsEqualTo(SpinCommand.Result.Success)
            queryCurrentPairs(party.id, sdk)
                ?.pairs
                .assertIsEqualTo(
                    notEmptyListOf(PinnedCouplingPair(notEmptyListOf(players[0].withPins()), emptySet())),
                )
        } teardown {
            sdk.fire(DeletePartyCommand(party.id))
        }
    }

    private fun fourPlayersTwoDefaultTwoAlternate() = notEmptyListOf(
        stubPlayer().copy(badge = Badge.Default.value, name = "One"),
        stubPlayer().copy(badge = Badge.Default.value, name = "Two"),
        stubPlayer().copy(badge = Badge.Alternate.value, name = "Three"),
        stubPlayer().copy(badge = Badge.Alternate.value, name = "Four"),
    )

    companion object {
        private suspend fun setupScenario(
            sdk: ActionCannon<CouplingSdkDispatcher>,
            party: PartyDetails,
            players: NotEmptyList<Player>,
            history: List<PairAssignmentDocument> = emptyList(),
            pins: List<Pin> = emptyList(),
        ) = coroutineScope {
            with(sdk) {
                fire(SavePartyCommand(party))
                players.forEach { launch { fire(SavePlayerCommand(party.id, it)) } }
                history.forEach { launch { sdk.fire(SavePairAssignmentsCommand(party.id, it)) } }
                pins.forEach { launch { sdk.fire(SavePinCommand(party.id, it)) } }
            }
        }
    }
}

private suspend fun queryCurrentPairs(partyId: PartyId, sdk: ActionCannon<CouplingSdkDispatcher>) = sdk.fire(graphQuery { party(partyId) { currentPairAssignments() } })
    ?.party
    ?.currentPairAssignmentDocument
    ?.element
