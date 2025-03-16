package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Duration.Companion.days

class SdkPairsTest {

    @Test
    fun willShowAllCurrentPairCombinations() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(4)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, emptyList())
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { players() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.players?.map(PartyRecord<Player>::data)?.map(PartyElement<Player>::element) }
            .assertIsEqualTo(
                listOf(
                    listOf(players[0], players[1]),
                    listOf(players[0], players[2]),
                    listOf(players[0], players[3]),
                    listOf(players[1], players[2]),
                    listOf(players[1], players[3]),
                    listOf(players[2], players[3]),
                ).plus(players.map { listOf(it) }),
            )
    }

    @Test
    fun willIncludeMobsFromContributionHistoryAndNotRepeatKnownPairs() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(4)
        val mob = players.shuffled().take(3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, emptyList())
        with(sdk()) {
            fire(
                SaveContributionCommand(
                    partyId = party.id,
                    contributionList = listOf(
                        ContributionInput(
                            contributionId = ContributionId.new(),
                            participantEmails = mob.map { it.email }.toSet(),
                            commitCount = null,
                            name = null,
                        ),
                    ),
                ),
            )
            fire(
                SaveContributionCommand(
                    partyId = party.id,
                    contributionList = listOf(
                        ContributionInput(
                            contributionId = ContributionId.new(),
                            participantEmails = mob.take(1).map { it.email }.toSet(),
                            commitCount = null,
                            name = null,
                        ),
                    ),
                ),
            )
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { players() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.players?.map(PartyRecord<Player>::data)?.map(PartyElement<Player>::element) }
            ?.last()
            .assertIsEqualTo(mob)
    }

    @Test
    fun willIncludeMobsFromContributionHistoryViaAdditionalEmailsIgnoringCase() = asyncSetup(object {
        val party = stubPartyDetails()
        val player1 = stubPlayer().copy(additionalEmails = setOf("excellent.continuousexcellence.io"))
        val player2 = stubPlayer().copy(additionalEmails = setOf("awesome.continuousexcellence.io"))
        val player3 = stubPlayer().copy(additionalEmails = setOf("cool.continuousexcellence.io"))
        val players = listOf(player1, player2, player3, stubPlayer(), stubPlayer())
        val mob = listOf(player1, player2, player3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, emptyList())
        with(sdk()) {
            fire(
                SaveContributionCommand(
                    partyId = party.id,
                    contributionList = listOf(
                        ContributionInput(
                            contributionId = ContributionId.new(),
                            participantEmails = mob.map { it.additionalEmails.first().uppercase() }.toSet(),
                            commitCount = null,
                            name = null,
                        ),
                    ),
                ),
            )
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { players() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.players?.map(PartyRecord<Player>::data)?.map(PartyElement<Player>::element) }
            ?.last()
            .assertIsEqualTo(mob)
    }

    @Test
    fun willCountNumberOfTimesPairHasOccurred() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
        )
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { count() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.count }
            .assertIsEqualTo(
                listOf(3, 1, 1, 0, 0, 0),
            )
    }

    @Test
    fun willReturnPairAssignmentRecordsForPairList() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pair12 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()))
        val pair02 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()))
        val pair01_1 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_2 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_3 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pairAssignmentDocs = listOf(pair01_1, pair12, pair01_2, pair02, pair01_3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { pairAssignmentHistory { details() } } } })
    } verify { result ->
        result?.party?.pairs?.map {
            it.pairAssignmentHistory?.mapNotNull(PairAssignment::details)
                ?.map(PartyRecord<PairAssignmentDocument>::data)
                ?.map(PartyElement<PairAssignmentDocument>::element)
        }
            .assertIsEqualTo(
                listOf(
                    listOf(pair01_1, pair01_2, pair01_3).sortedByDescending { it.date },
                    listOf(pair02),
                    listOf(pair12),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )
    }

    @Test
    fun willReturnPairAssignmentRecordsForPair() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pair12 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()))
        val pair02 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()))
        val pair01_1 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_2 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_3 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pairAssignmentDocs = listOf(pair01_1, pair12, pair01_2, pair02, pair01_3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) { pair(players[0].id, players[1].id) { pairAssignmentHistory { details() } } }
            },
        )
    } verify { result ->
        result?.party?.pair?.pairAssignmentHistory
            ?.mapNotNull(PairAssignment::details)
            ?.map(PartyRecord<PairAssignmentDocument>::data)
            ?.map(PartyElement<PairAssignmentDocument>::element)
            .assertIsEqualTo(
                listOf(pair01_1, pair01_2, pair01_3).sortedByDescending { it.date },
            )
    }

    @Test
    fun willReturnPairAssignmentRecordsForPairWithAlternateIdOrder() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pair12 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()))
        val pair02 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()))
        val pair01_1 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_2 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_3 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pairAssignmentDocs = listOf(pair01_1, pair12, pair01_2, pair02, pair01_3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) { pair(players[1].id, players[0].id) { players() } }
            },
        )
    } verify { result ->
        result?.party?.pair?.players
            ?.map { it.data.element.id }
            ?.toSet()
            .assertIsEqualTo(
                setOf(players[0].id, players[1].id),
            )
    }

    @Test
    fun willSupportPartialPairAssignmentQueries() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pair12 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()))
        val pair02 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()))
        val pair01_1 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_2 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pair01_3 = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()))
        val pairAssignmentDocs = listOf(pair01_1, pair12, pair01_2, pair02, pair01_3)
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { pairAssignmentHistory { date() } } } })
    } verify { result ->
        result?.party?.pairs?.map { it.pairAssignmentHistory?.mapNotNull(PairAssignment::date) }
            .assertIsEqualTo(
                listOf(
                    listOf(pair01_1, pair01_2, pair01_3).map { it.date }.sortedDescending(),
                    listOf(pair02.date),
                    listOf(pair12.date),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )
    }

    @Test
    fun willCalculatePairRecentTimesPaired() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
        )
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { recentTimesPaired() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.recentTimesPaired }
            .assertIsEqualTo(
                listOf(4, 0, 0, null, null, null),
            )
    }

    @Test
    fun willCalculatePairRecentTimesPairedOverTime() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
        )
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { pairAssignmentHistory { recentTimesPaired() } } } })
    } verify { result ->
        result?.party?.pairs?.map { it.pairAssignmentHistory?.map(PairAssignment::recentTimesPaired) }
            .assertIsEqualTo(
                listOf(
                    listOf(4, 3, 2, 1),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )
    }

    @Test
    fun willShowSpinsSinceLastPaired() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val now = Clock.System.now()
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(
                date = now.minus(5.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(4.days),
                pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(3.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(2.days),
                pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(1.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
        )
    }) {
        savePartyStateWithFixedPlayerOrder(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { spinsSinceLastPaired() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.spinsSinceLastPaired }
            .assertIsEqualTo(
                listOf(0, 1, 3, null, null, null),
            )
    }
}
