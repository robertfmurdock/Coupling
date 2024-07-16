package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Wheel
import com.zegreatrob.coupling.model.flatMap
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class GameExamplesTest {

    companion object :
        CreatePairCandidateReportListAction.Dispatcher<Companion>,
        com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction.Dispatcher<Companion>,
        FindNewPairsAction.Dispatcher<Companion>,
        NextPlayerAction.Dispatcher<Companion>,
        AssignPinsAction.Dispatcher,
        CreatePairCandidateReportAction.Dispatcher,
        Wheel {
        override val wheel = this
        override val cannon = ActionCannon(this)
    }

    class WithUniformBadgesAndLongestTimeRule {

        companion object {
            val party = PartyDetails(
                id = PartyId("JLA"),
                pairingRule = PairingRule.LongestTime,
            )

            val bruce = defaultPlayer.copy(id = "1", badge = 0, name = "Batman")
            val hal = defaultPlayer.copy(id = "2", badge = 0, name = "Green Lantern")
            val barry = defaultPlayer.copy(id = "3", badge = 0, name = "Flash")
            val john = defaultPlayer.copy(id = "4", badge = 0, name = "Martian Manhunter")
            val clark = defaultPlayer.copy(id = "5", badge = 0, name = "Superman")
            val diana = defaultPlayer.copy(id = "6", badge = 0, name = "Wonder Woman")

            val allPlayers = notEmptyListOf(
                clark,
                bruce,
                diana,
                hal,
                barry,
                john,
            )
        }

        @Test
        fun worksWithNoHistory() = asyncSetup(object {
            val history = emptyList<PairAssignmentDocument>()
            val party = PartyDetails(
                PartyId("Best party ever"),
                PairingRule.LongestTime,
            )
        }) exercise {
            perform(
                com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction(
                    party,
                    allPlayers,
                    emptyList(),
                    history,
                ),
            )
        } verify { result ->
            result.pairs.map { pair ->
                pair.players.size.toInt().assertIsEqualTo(2)
                pair.pinnedPlayers
            }
                .flatMap(NotEmptyList<PinnedPlayer>::toList)
                .size
                .assertIsEqualTo(allPlayers.size.toInt())
        }

        @Test
        fun worksWithAnOddNumberOfPlayersAndNoHistory() = asyncSetup(object {
            val history = emptyList<PairAssignmentDocument>()
            val party = PartyDetails(
                PartyId("Best party ever"),
                PairingRule.LongestTime,
            )
        }) exercise {
            perform(
                com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction(
                    party,
                    notEmptyListOf(clark, bruce, diana),
                    emptyList(),
                    history,
                ),
            )
        } verify { result ->
            result.pairs.size.toInt().assertIsEqualTo(2)
        }

        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = asyncSetup(object {
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 10),
                    pairs = notEmptyListOf(pairOf(bruce, clark)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 9),
                    pairs = notEmptyListOf(pairOf(bruce, diana)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 8),
                    pairs = notEmptyListOf(pairOf(bruce, hal)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 7),
                    pairs = notEmptyListOf(pairOf(bruce, barry)).withNoPins(),
                ),
            )
        }) exercise {
            perform(
                com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction(
                    party,
                    allPlayers,
                    emptyList(),
                    history,
                ),
            )
        } verify { result ->
            result.pairs.toList().contains(pairOf(bruce, john).toPinnedPair())
        }
    }

    class WithDifferentBadgesAndLongestPairRule {
        companion object {
            val party = PartyDetails(
                id = PartyId("JLA"),
                pairingRule = PairingRule.LongestTime,
            )

            val bruce = defaultPlayer.copy(id = "1", badge = 0, name = "Batman")
            val hal = defaultPlayer.copy(id = "2", badge = 1, name = "Green Lantern")
            val barry = defaultPlayer.copy(id = "3", badge = 0, name = "Flash")
            val john = defaultPlayer.copy(id = "4", badge = 1, name = "Martian Manhunter")
            val clark = defaultPlayer.copy(id = "5", badge = 0, name = "Superman")
            val diana = defaultPlayer.copy(id = "6", badge = 1, name = "Wonder Woman")

            val allPlayers = notEmptyListOf(clark, bruce, diana, hal, barry, john)
        }

        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = asyncSetup(object {
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 10),
                    pairs = notEmptyListOf(pairOf(bruce, clark)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 9),
                    pairs = notEmptyListOf(pairOf(bruce, diana)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 8),
                    pairs = notEmptyListOf(pairOf(bruce, hal)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = dateTime(2014, 1, 7),
                    pairs = notEmptyListOf(pairOf(bruce, barry)).withNoPins(),
                ),
            )
        }) exercise {
            perform(
                com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction(
                    party,
                    allPlayers,
                    emptyList(),
                    history,
                ),
            )
        } verify { result ->
            result.pairs.toList().contains(pairOf(bruce, john).toPinnedPair())
        }
    }

    @Test
    fun willNotGetStuckWhenPairingPeopleWithDifferentBadges() = asyncSetup(object {
        val party = PartyDetails(
            PartyId("Avengers"),
            PairingRule.PreferDifferentBadge,
        )
        val kamala = defaultPlayer.copy(badge = 0, name = "Ms. Marvel")
        val logan = defaultPlayer.copy(badge = 1, name = "Wolverine")
        val steve = defaultPlayer.copy(badge = 1, name = "Captain America")
        val thor = defaultPlayer.copy(badge = 1, name = "Thor")
        val allPlayers = notEmptyListOf(kamala, logan, steve, thor)

        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = dateTime(2014, 1, 10),
                pairs = notEmptyListOf(pairOf(kamala, thor)).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = dateTime(2014, 1, 9),
                pairs = notEmptyListOf(pairOf(kamala, steve)).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = dateTime(2014, 1, 8),
                pairs = notEmptyListOf(pairOf(kamala, logan)).withNoPins(),
            ),
        )
    }) exercise {
        perform(
            com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction(
                party,
                allPlayers,
                emptyList(),
                history,
            ),
        )
    } verify { result ->
        result.pairs.toList().contains(pairOf(kamala, logan).toPinnedPair())
    }
}

private fun NotEmptyList<CouplingPair>.withNoPins() = map(CouplingPair::toPinnedPair)

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = toNotEmptyList().map { player -> player.withPins(emptyList()) }

private fun dateTime(year: Int, month: Int, day: Int) =
    LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
