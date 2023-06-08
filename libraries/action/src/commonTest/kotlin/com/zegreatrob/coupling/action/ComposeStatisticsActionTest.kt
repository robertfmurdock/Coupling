package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import korlibs.time.DateTime
import korlibs.time.days
import korlibs.time.hours
import kotlin.test.Test

@Suppress("unused")
class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsAction.Dispatcher {
        val party = Party(PartyId("LOL"), PairingRule.LongestTime)

        fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
            .map { number -> makePlayer("$number") }

        private fun makePlayer(id: String) = Player(id = id, avatarType = null)

        private fun List<CouplingPair>.assertMatch(expected: List<CouplingPair>) {
            assertIsEqualTo(
                expected,
                "------WE EXPECT\n${expected.describe()}\n------RESULTS\n${this.describe()}\n-----END\n",
            )
        }

        private fun List<CouplingPair>.describe() = map { it.asArray().map { player -> player.id } }
            .joinToString(", ").let { "[ $it ]" }
    }

    class WillIncludeTheFullRotationNumber {

        companion object {
            private val history = emptyList<PairAssignmentDocument>()

            fun composeStatisticsAction(players: List<Player>) = ComposeStatisticsAction(party, players, history)
        }

        @Test
        fun whenGivenOnePlayerWillReturnOne() = setup(object {
            val players = makePlayers(1)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenTwoPlayersWillReturnOne() = setup(object {
            val players = makePlayers(2)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenThreePlayersWillReturnThree() = setup(object {
            val players = makePlayers(3)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenFourPlayersWillReturnThree() = setup(object {
            val players = makePlayers(4)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenSevenPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(7)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(7)
        }

        @Test
        fun whenGivenEightPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(8)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(7)
        }
    }

    class WillGeneratePairReports {

        class WithNoHistory {
            companion object {
                val history = emptyList<PairAssignmentDocument>()
            }

            @Test
            fun withNoPlayersNoPairReportsWillBeCreated() = setup(object {
                val players = makePlayers(0)
            }) exercise {
                perform(ComposeStatisticsAction(party, players, history))
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withOnePlayersNoPairReportsWillBeCreated() = setup(object {
                val players = makePlayers(1)
            }) exercise {
                perform(ComposeStatisticsAction(party, players, history))
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withTwoPlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(2)
            }) exercise {
                perform(ComposeStatisticsAction(party, players, history))
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(
                    listOf(
                        PairReport(pairOf(players[0], players[1]), NeverPaired),
                    ),
                )
            }

            @Test
            fun withFivePlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(5)
            }) exercise {
                perform(ComposeStatisticsAction(party, players, history))
            } verify { (_, pairReports) ->
                val (player1, player2, player3, player4, player5) = players
                pairReports.map { it.pair }
                    .assertMatch(
                        listOf(
                            pairOf(player1, player2),
                            pairOf(player1, player3),
                            pairOf(player1, player4),
                            pairOf(player1, player5),

                            pairOf(player2, player3),
                            pairOf(player2, player4),
                            pairOf(player2, player5),
                            pairOf(player3, player4),
                            pairOf(player3, player5),
                            pairOf(player4, player5),
                        ),
                    )
            }
        }

        @Test
        fun withFourPlayersThePairReportsAreOrderedByLongestTimeSinceLastPairing() = setup(object {
            val players = makePlayers(4)
            val player1 = players[0]
            val player2 = players[1]
            val player3 = players[2]
            val player4 = players[3]
            val stubDate = DateTime.now()
            val history = listOf(
                pairAssignmentDocument(
                    listOf(
                        PinnedCouplingPair(
                            listOf(
                                player1.withPins(
                                    emptyList(),
                                ),
                                player3.withPins(emptyList()),
                            ),
                            emptySet(),
                        ),
                        PinnedCouplingPair(
                            listOf(
                                player2.withPins(
                                    emptyList(),
                                ),
                                player4.withPins(emptyList()),
                            ),
                            emptySet(),
                        ),
                    ),
                ),
                pairAssignmentDocument(
                    listOf(
                        PinnedCouplingPair(
                            listOf(
                                player1.withPins(
                                    emptyList(),
                                ),
                                player2.withPins(emptyList()),
                            ),
                            emptySet(),
                        ),
                        PinnedCouplingPair(
                            listOf(
                                player3.withPins(
                                    emptyList(),
                                ),
                                player4.withPins(emptyList()),
                            ),
                            emptySet(),
                        ),
                    ),
                ),
            )

            private fun pairAssignmentDocument(pairs: List<PinnedCouplingPair>) =
                PairAssignmentDocument(date = stubDate, pairs = pairs, id = PairAssignmentDocumentId(""))
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { (_, pairReports) ->
            pairReports.map { it.timeSinceLastPair }
                .assertIsEqualTo(
                    listOf(
                        NeverPaired,
                        NeverPaired,
                        TimeResultValue(1),
                        TimeResultValue(1),
                        TimeResultValue(0),
                        TimeResultValue(0),
                    ),
                )
            pairReports.map { it.pair }
                .assertMatch(
                    listOf(
                        pairOf(player1, player4),
                        pairOf(player2, player3),
                        pairOf(player1, player2),
                        pairOf(player3, player4),
                        pairOf(player1, player3),
                        pairOf(player2, player4),
                    ),
                )
        }

        @Test
        fun stillSortsCorrectlyWithLargeRealisticHistory() =
            setup(loadJsonPartySetup("realistic-sort-test-data/inputs.json")) {
            } exercise {
                perform(ComposeStatisticsAction(party, players, history))
            } verify { result ->
                val expectedTimesResults = loadResource<Array<Int>>("realistic-sort-test-data/expectResults.json")
                    .map { TimeResultValue(it) }
                result.pairReports.map { it.timeSinceLastPair }
                    .assertIsEqualTo(expectedTimesResults)
            }
    }

    class WillCalculateTheMedianSpinTime {

        companion object {
            private fun pairAssignmentDocument(dateTime: DateTime) =
                PairAssignmentDocument(id = PairAssignmentDocumentId(""), date = dateTime, pairs = emptyList())
        }

        @Test
        fun whenThereIsNoHistoryWillReturnNotApplicable() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val players = emptyList<Player>()
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(null)
        }

        @Test
        fun whenThereAreDailySpinsWillReturn1Day() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(DateTime(2017, 2, 17)),
                pairAssignmentDocument(DateTime(2017, 2, 16)),
                pairAssignmentDocument(DateTime(2017, 2, 15)),
                pairAssignmentDocument(DateTime(2017, 2, 14)),
                pairAssignmentDocument(DateTime(2017, 2, 13)),
                pairAssignmentDocument(DateTime(2017, 2, 12)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(1.days, "Got ${result.medianSpinDuration?.days} days")
        }

        @Test
        fun whenTwoDaySpinsWithOutliersWillReturn2Days() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(DateTime(2017, 2, 17)),
                pairAssignmentDocument(DateTime(2017, 2, 12)),
                pairAssignmentDocument(DateTime(2017, 2, 10)),
                pairAssignmentDocument(DateTime(2017, 2, 8)),
                pairAssignmentDocument(DateTime(2017, 2, 6)),
                pairAssignmentDocument(DateTime(2017, 2, 4)),
                pairAssignmentDocument(DateTime(2017, 2, 3)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.days} days")
        }

        @Test
        fun whenOneInstanceOfMedianAndVariablePatternWillFindMedianCorrectly() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(DateTime(2017, 2, 20)),
                pairAssignmentDocument(DateTime(2017, 2, 17)),
                pairAssignmentDocument(DateTime(2017, 2, 15)),
                pairAssignmentDocument(DateTime(2017, 2, 14)),
                pairAssignmentDocument(DateTime(2017, 2, 13)),
                pairAssignmentDocument(DateTime(2017, 2, 10)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.days} days")
        }

        @Test
        fun withOneHistoryEntryWillReturnNull() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                PairAssignmentDocument(
                    date = DateTime(2017, 2, 17),
                    id = PairAssignmentDocumentId(""),
                    pairs = emptyList(),
                ),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(null)
        }

        @Test
        fun worksWithHourDifferencesAsWell() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(DateTime(2017, 2, 20, 21)),
                pairAssignmentDocument(DateTime(2017, 2, 20, 19)),
                pairAssignmentDocument(DateTime(2017, 2, 20, 18)),
                pairAssignmentDocument(DateTime(2017, 2, 20, 13)),
                pairAssignmentDocument(DateTime(2017, 2, 20, 12)),
                pairAssignmentDocument(DateTime(2017, 2, 20, 9)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.hours, "Got ${result.medianSpinDuration?.hours} hours")
        }

        @Test
        fun whenMedianIsInBetweenUnitsWillStillBeAccurate() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(DateTime(2017, 2, 20, 21)),
                pairAssignmentDocument(DateTime(2017, 2, 17, 19)),
                pairAssignmentDocument(DateTime(2017, 2, 15, 7)),
                pairAssignmentDocument(DateTime(2017, 2, 14, 13)),
                pairAssignmentDocument(DateTime(2017, 2, 13, 12)),
                pairAssignmentDocument(DateTime(2017, 2, 10, 9)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.5.days, "Got ${result.medianSpinDuration?.days} days")
        }
    }
}

expect fun loadJsonPartySetup(fileResource: String): PartySetup
expect inline fun <reified T> loadResource(fileResource: String): T

data class PartySetup(val party: Party, val players: List<Player>, val history: List<PairAssignmentDocument>)