package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import korlibs.time.DateTime
import kotlin.test.Test

class GameExamplesTest {

    companion object :
        RunGameAction.Dispatcher,
        FindNewPairsAction.Dispatcher,
        NextPlayerAction.Dispatcher,
        CreatePairCandidateReportAction.Dispatcher,
        CreatePairCandidateReportsAction.Dispatcher,
        DispatchingActionExecutor<Companion>,
        Wheel {
        override val wheel = this
        override val actionDispatcher = this
        override val execute = this
    }

    class WithUniformBadgesAndLongestTimeRule {

        companion object {
            val party = Party(
                id = PartyId("JLA"),
                pairingRule = PairingRule.LongestTime,
            )

            val bruce = Player(id = "1", badge = 0, name = "Batman", avatarType = null)
            val hal = Player(id = "2", badge = 0, name = "Green Lantern", avatarType = null)
            val barry = Player(id = "3", badge = 0, name = "Flash", avatarType = null)
            val john = Player(id = "4", badge = 0, name = "Martian Manhunter", avatarType = null)
            val clark = Player(id = "5", badge = 0, name = "Superman", avatarType = null)
            val diana = Player(id = "6", badge = 0, name = "Wonder Woman", avatarType = null)

            val allPlayers = listOf(
                clark,
                bruce,
                diana,
                hal,
                barry,
                john,
            )
        }

        @Test
        fun worksWithNoHistory() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val party = Party(
                PartyId("Best party ever"),
                PairingRule.LongestTime,
            )
        }) exercise {
            perform(RunGameAction(allPlayers, emptyList(), history, party))
        } verify { result ->
            result.pairs.map { pair -> pair.players.size.assertIsEqualTo(2); pair.players }
                .flatten()
                .size
                .assertIsEqualTo(allPlayers.size)
        }

        @Test
        fun worksWithAnOddNumberOfPlayersAndNoHistory() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val party = Party(
                PartyId("Best party ever"),
                PairingRule.LongestTime,
            )
        }) exercise {
            perform(RunGameAction(listOf(clark, bruce, diana), emptyList(), history, party))
        } verify { result ->
            result.pairs.size.assertIsEqualTo(2)
        }

        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = setup(object {
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 10),
                    pairs = listOf(pairOf(bruce, clark)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 9),
                    pairs = listOf(pairOf(bruce, diana)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 8),
                    pairs = listOf(pairOf(bruce, hal)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 7),
                    pairs = listOf(pairOf(bruce, barry)).withNoPins(),
                ),
            )
        }) exercise {
            perform(RunGameAction(allPlayers, emptyList(), history, party))
        } verify { result ->
            result.pairs.contains(pairOf(bruce, john).toPinnedPair())
        }
    }

    class WithDifferentBadgesAndLongestPairRule {
        companion object {
            val party = Party(
                id = PartyId("JLA"),
                pairingRule = PairingRule.LongestTime,
            )

            val bruce = Player(id = "1", badge = 0, name = "Batman", avatarType = null)
            val hal = Player(id = "2", badge = 1, name = "Green Lantern", avatarType = null)
            val barry = Player(id = "3", badge = 0, name = "Flash", avatarType = null)
            val john = Player(id = "4", badge = 1, name = "Martian Manhunter", avatarType = null)
            val clark = Player(id = "5", badge = 0, name = "Superman", avatarType = null)
            val diana = Player(id = "6", badge = 1, name = "Wonder Woman", avatarType = null)

            val allPlayers = listOf(clark, bruce, diana, hal, barry, john)
        }

        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = setup(object {
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 10),
                    pairs = listOf(pairOf(bruce, clark)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 9),
                    pairs = listOf(pairOf(bruce, diana)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 8),
                    pairs = listOf(pairOf(bruce, hal)).withNoPins(),
                ),
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(2014, 1, 7),
                    pairs = listOf(pairOf(bruce, barry)).withNoPins(),
                ),
            )
        }) exercise {
            perform(RunGameAction(allPlayers, emptyList(), history, party))
        } verify { result ->
            result.pairs.contains(pairOf(bruce, john).toPinnedPair())
        }
    }

    @Test
    fun willNotGetStuckWhenPairingPeopleWithDifferentBadges() = setup(object {
        val party = Party(
            PartyId("Avengers"),
            PairingRule.PreferDifferentBadge,
        )
        val kamala = Player(badge = 0, name = "Ms. Marvel", avatarType = null)
        val logan = Player(badge = 1, name = "Wolverine", avatarType = null)
        val steve = Player(badge = 1, name = "Captain America", avatarType = null)
        val thor = Player(badge = 1, name = "Thor", avatarType = null)
        val allPlayers = listOf(kamala, logan, steve, thor)

        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2014, 1, 10),
                pairs = listOf(pairOf(kamala, thor)).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2014, 1, 9),
                pairs = listOf(pairOf(kamala, steve)).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2014, 1, 8),
                pairs = listOf(pairOf(kamala, logan)).withNoPins(),
            ),
        )
    }) exercise {
        perform(RunGameAction(allPlayers, emptyList(), history, party))
    } verify { result ->
        result.pairs.contains(pairOf(kamala, logan).toPinnedPair())
    }
}

private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }
