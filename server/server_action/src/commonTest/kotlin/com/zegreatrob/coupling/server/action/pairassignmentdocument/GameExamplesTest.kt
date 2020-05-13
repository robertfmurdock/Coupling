package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.testaction.PassthroughCommandExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class GameExamplesTest {

    companion object : RunGameActionDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        PassthroughCommandExecutor<CreatePairCandidateReportsActionDispatcher>,
        Wheel {
        override val wheel = this
        override val actionDispatcher = this
        override val executor = this
    }

    class WithUniformBadgesAndLongestTimeRule {

        companion object {
            val tribe = Tribe(
                id = TribeId("JLA"),
                pairingRule = PairingRule.LongestTime
            )

            val bruce = Player(id = "1", name = "Batman", badge = 0)
            val hal = Player(id = "2", name = "Green Lantern", badge = 0)
            val barry = Player(id = "3", name = "Flash", badge = 0)
            val john = Player(id = "4", name = "Martian Manhunter", badge = 0)
            val clark = Player(id = "5", name = "Superman", badge = 0)
            val diana = Player(id = "6", name = "Wonder Woman", badge = 0)

            val allPlayers = listOf(
                clark,
                bruce,
                diana,
                hal,
                barry,
                john
            )
        }

        @Test
        fun worksWithNoHistory() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val tribe = Tribe(
                TribeId("Best tribe ever"),
                PairingRule.LongestTime
            )
        }) exercise {
            RunGameAction(allPlayers, emptyList(), history, tribe)
                .perform()
        } verify { result ->
            result.pairs.map { pair -> pair.players.size.assertIsEqualTo(2); pair.players }
                .flatten()
                .size
                .assertIsEqualTo(allPlayers.size)
        }

        @Test
        fun worksWithAnOddNumberOfPlayersAndNoHistory() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val tribe = Tribe(
                TribeId("Best tribe ever"),
                PairingRule.LongestTime
            )
        }) exercise {
            RunGameAction(listOf(clark, bruce, diana), emptyList(), history, tribe)
                .perform()
        } verify { result ->
            result.pairs.size.assertIsEqualTo(2)
        }


        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = setup(object {
            val history = listOf(
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 10),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            clark
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 9),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            diana
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 8),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            hal
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 7),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            barry
                        )
                    ).withNoPins()
                )
            )
        }) exercise {
            RunGameAction(allPlayers, emptyList(), history, tribe)
                .perform()
        } verify { result ->
            result.pairs.contains(CouplingPair.Double(bruce, john).toPinnedPair())
        }
    }

    class WithDifferentBadgesAndLongestPairRule {
        companion object {
            val tribe = Tribe(
                id = TribeId("JLA"),
                pairingRule = PairingRule.LongestTime
            )

            val bruce = Player(id = "1", name = "Batman", badge = 0)
            val hal = Player(id = "2", name = "Green Lantern", badge = 1)
            val barry = Player(id = "3", name = "Flash", badge = 0)
            val john = Player(id = "4", name = "Martian Manhunter", badge = 1)
            val clark = Player(id = "5", name = "Superman", badge = 0)
            val diana = Player(id = "6", name = "Wonder Woman", badge = 1)

            val allPlayers = listOf(clark, bruce, diana, hal, barry, john)
        }

        @Test
        fun willAlwaysPairSomeoneWhoHasPairedWithEveryoneButOnePersonWithThatPerson() = setup(object {
            val history = listOf(
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 10),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            clark
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 9),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            diana
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 8),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            hal
                        )
                    ).withNoPins()
                ),
                PairAssignmentDocument(
                    date = DateTime(2014, 1, 7),
                    pairs = listOf(
                        CouplingPair.Double(
                            bruce,
                            barry
                        )
                    ).withNoPins()
                )
            )
        }) exercise {
            RunGameAction(allPlayers, emptyList(), history, tribe)
                .perform()
        } verify { result ->
            result.pairs.contains(CouplingPair.Double(bruce, john).toPinnedPair())
        }

    }

    @Test
    fun willNotGetStuckWhenPairingPeopleWithDifferentBadges() = setup(object {
        val tribe = Tribe(
            TribeId("Avengers"),
            PairingRule.PreferDifferentBadge
        )
        val kamala = Player(name = "Ms. Marvel", badge = 0)
        val logan = Player(name = "Wolverine", badge = 1)
        val steve = Player(name = "Captain America", badge = 1)
        val thor = Player(name = "Thor", badge = 1)
        val allPlayers = listOf(kamala, logan, steve, thor)

        val history = listOf(
            PairAssignmentDocument(
                date = DateTime(2014, 1, 10),
                pairs = listOf(
                    CouplingPair.Double(
                        kamala,
                        thor
                    )
                ).withNoPins()
            ),
            PairAssignmentDocument(
                date = DateTime(2014, 1, 9),
                pairs = listOf(
                    CouplingPair.Double(
                        kamala,
                        steve
                    )
                ).withNoPins()
            ),
            PairAssignmentDocument(
                date = DateTime(2014, 1, 8),
                pairs = listOf(
                    CouplingPair.Double(
                        kamala,
                        logan
                    )
                ).withNoPins()
            )
        )
    }) exercise {
        RunGameAction(allPlayers, emptyList(), history, tribe)
            .perform()
    } verify { result ->
        result.pairs.contains(CouplingPair.Double(kamala, logan).toPinnedPair())
    }
}

private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() =
    PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }
