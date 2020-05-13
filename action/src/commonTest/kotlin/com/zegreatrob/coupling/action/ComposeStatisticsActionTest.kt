package com.zegreatrob.coupling.action

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

@Suppress("unused")
class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsActionDispatcher {
        override val traceId = uuid4()
        val tribe = Tribe(TribeId("LOL"), PairingRule.LongestTime)

        fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
            .map { number -> makePlayer("$number") }

        private fun makePlayer(id: String) = Player(id = id)

        private fun List<CouplingPair>.assertMatch(expected: List<CouplingPair>) {
            assertIsEqualTo(
                expected,
                "------WE EXPECT\n${expected.describe()}\n------RESULTS\n${this.describe()}\n-----END\n"
            )
        }

        private fun List<CouplingPair>.describe() = map { it.asArray().map { player -> player.id } }
            .joinToString(", ").let { "[ $it ]" }
    }

    class WillIncludeTheFullRotationNumber {

        companion object {
            private val history = emptyList<PairAssignmentDocument>()

            fun composeStatisticsAction(players: List<Player>) = ComposeStatisticsAction(tribe, players, history)
        }

        @Test
        fun whenGivenOnePlayerWillReturnOne() = setup(object {
            val players = makePlayers(1)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenTwoPlayersWillReturnOne() = setup(object {
            val players = makePlayers(2)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenThreePlayersWillReturnThree() = setup(object {
            val players = makePlayers(3)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenFourPlayersWillReturnThree() = setup(object {
            val players = makePlayers(4)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenSevenPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(7)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(7)
        }

        @Test
        fun whenGivenEightPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(8)
        }) exercise {
            perform(composeStatisticsAction(players))
        } verifySuccess { (spinsUntilFullRotation) ->
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
                perform(ComposeStatisticsAction(tribe, players, history))
            } verifySuccess { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withOnePlayersNoPairReportsWillBeCreated() = setup(object {
                val players = makePlayers(1)
            }) exercise {
                perform(ComposeStatisticsAction(tribe, players, history))
            } verifySuccess { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withTwoPlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(2)
            }) exercise {
                perform(ComposeStatisticsAction(tribe, players, history))
            } verifySuccess { (_, pairReports) ->
                pairReports.assertIsEqualTo(
                    listOf(
                        PairReport(CouplingPair.Double(players[0], players[1]), NeverPaired)
                    )
                )
            }

            @Test
            fun withFivePlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(5)
            }) exercise {
                perform(ComposeStatisticsAction(tribe, players, history))
            } verifySuccess { (_, pairReports) ->
                val (player1, player2, player3, player4, player5) = players
                pairReports.map { it.pair }
                    .assertMatch(
                        listOf(
                            CouplingPair.Double(player1, player2),
                            CouplingPair.Double(player1, player3),
                            CouplingPair.Double(player1, player4),
                            CouplingPair.Double(player1, player5),

                            CouplingPair.Double(player2, player3),
                            CouplingPair.Double(player2, player4),
                            CouplingPair.Double(player2, player5),
                            CouplingPair.Double(player3, player4),
                            CouplingPair.Double(player3, player5),
                            CouplingPair.Double(player4, player5)
                        )
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
                                    emptyList()
                                ), player3.withPins(emptyList())
                            ),
                            emptyList()
                        ),
                        PinnedCouplingPair(
                            listOf(
                                player2.withPins(
                                    emptyList()
                                ), player4.withPins(emptyList())
                            ),
                            emptyList()
                        )
                    )
                ),
                pairAssignmentDocument(
                    listOf(
                        PinnedCouplingPair(
                            listOf(
                                player1.withPins(
                                    emptyList()
                                ), player2.withPins(emptyList())
                            ),
                            emptyList()
                        ),
                        PinnedCouplingPair(
                            listOf(
                                player3.withPins(
                                    emptyList()
                                ), player4.withPins(emptyList())
                            ),
                            emptyList()
                        )
                    )
                )
            )

            private fun pairAssignmentDocument(pairs: List<PinnedCouplingPair>) =
                PairAssignmentDocument(date = stubDate, pairs = pairs)
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { (_, pairReports) ->
            pairReports.map { it.timeSinceLastPair }
                .assertIsEqualTo(
                    listOf(
                        NeverPaired,
                        NeverPaired,
                        TimeResultValue(1),
                        TimeResultValue(1),
                        TimeResultValue(0),
                        TimeResultValue(0)
                    )
                )
            pairReports.map { it.pair }
                .assertMatch(
                    listOf(
                        CouplingPair.Double(player1, player4),
                        CouplingPair.Double(player2, player3),
                        CouplingPair.Double(player1, player2),
                        CouplingPair.Double(player3, player4),
                        CouplingPair.Double(player1, player3),
                        CouplingPair.Double(player2, player4)
                    )
                )
        }

        @Test
        fun stillSortsCorrectlyWithLargeRealisticHistory() =
            setup(loadJsonTribeSetup("realistic-sort-test-data/inputs.json")) {
            } exercise {
                perform(ComposeStatisticsAction(tribe, players, history))
            } verifySuccess { result ->
                val expectedTimesResults = loadResource<Array<Int>>("realistic-sort-test-data/expectResults.json")
                    .map { TimeResultValue(it) }
                result.pairReports.map { it.timeSinceLastPair }
                    .assertIsEqualTo(expectedTimesResults)
            }

    }

    class WillCalculateTheMedianSpinTime {

        companion object {
            private fun pairAssignmentDocument(dateTime: DateTime) =
                PairAssignmentDocument(
                    date = dateTime,
                    pairs = emptyList()
                )
        }

        @Test
        fun whenThereIsNoHistoryWillReturnNotApplicable() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val players = emptyList<Player>()
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
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
                pairAssignmentDocument(DateTime(2017, 2, 12))
            )
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
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
                pairAssignmentDocument(DateTime(2017, 2, 3))
            )
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
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
                pairAssignmentDocument(DateTime(2017, 2, 10))
            )
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.days} days")
        }

        @Test
        fun withOneHistoryEntryWillReturnNull() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                PairAssignmentDocument(
                    date = DateTime(2017, 2, 17),
                    pairs = emptyList()
                )
            )
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
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
                pairAssignmentDocument(DateTime(2017, 2, 20, 9))
            )
        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
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
                pairAssignmentDocument(DateTime(2017, 2, 10, 9))
            )

        }) exercise {
            perform(ComposeStatisticsAction(tribe, players, history))
        } verifySuccess { result ->
            result.medianSpinDuration.assertIsEqualTo(2.5.days, "Got ${result.medianSpinDuration?.days} days")
        }

    }

}

expect fun loadJsonTribeSetup(fileResource: String): TribeSetup
expect inline fun <reified T> loadResource(fileResource: String): T

data class TribeSetup(val tribe: Tribe, val players: List<Player>, val history: List<PairAssignmentDocument>)