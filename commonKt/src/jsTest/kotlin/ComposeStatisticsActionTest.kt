@file:Suppress("unused")

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import kotlin.js.Json
import kotlin.test.Test

class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsActionDispatcher {
        val tribe = KtTribe("LOL", PairingRule.LongestTime)

        fun makePlayers(tribe: KtTribe, numberOfPlayers: Int) = (1..numberOfPlayers)
                .map { number -> makePlayer(tribe, "$number") }

        private fun makePlayer(tribe: KtTribe, id: String) = Player(id = id, tribe = tribe.id)

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

            fun composeStatisticsAction(players: List<Player>) =
                    ComposeStatisticsAction(tribe, players, history)
        }

        @Test
        fun whenGivenOnePlayerWillReturnOne() = setup(object {
            val players = makePlayers(tribe, 1)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenTwoPlayersWillReturnOne() = setup(object {
            val players = makePlayers(tribe, 2)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(1)
        }

        @Test
        fun whenGivenThreePlayersWillReturnThree() = setup(object {
            val players = makePlayers(tribe, 3)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenFourPlayersWillReturnThree() = setup(object {
            val players = makePlayers(tribe, 4)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(3)
        }

        @Test
        fun whenGivenSevenPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(tribe, 7)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
        } verify { (spinsUntilFullRotation) ->
            spinsUntilFullRotation.assertIsEqualTo(7)
        }

        @Test
        fun whenGivenEightPlayersWillReturnSeven() = setup(object {
            val players = makePlayers(tribe, 8)
        }) exercise {
            composeStatisticsAction(players)
                    .perform()
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
                val players = makePlayers(tribe, 0)
            }) exercise {
                ComposeStatisticsAction(tribe, players, history)
                        .perform()
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withOnePlayersNoPairReportsWillBeCreated() = setup(object {
                val players = makePlayers(tribe, 1)
            }) exercise {
                ComposeStatisticsAction(tribe, players, history)
                        .perform()
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(emptyList())
            }

            @Test
            fun withTwoPlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(tribe, 2)
            }) exercise {
                ComposeStatisticsAction(tribe, players, history)
                        .perform()
            } verify { (_, pairReports) ->
                pairReports.assertIsEqualTo(
                        listOf(
                                PairReport(CouplingPair.Double(players[0], players[1]), NeverPaired)
                        )
                )
            }

            @Test
            fun withFivePlayersOnePairReportWillBeCreated() = setup(object {
                val players = makePlayers(tribe, 5)
            }) exercise {
                ComposeStatisticsAction(tribe, players, history)
                        .perform()
            } verify { (_, pairReports) ->
                val (player1, player2, player3, player4, player5) = players
                pairReports.map { it.pair }
                        .assertMatch(listOf(
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
                        ))
            }
        }

        @Test
        fun withFourPlayersThePairReportsAreOrderedByLongestTimeSinceLastPairing() = setup(object {
            val players = makePlayers(tribe, 4)
            val player1 = players[0]
            val player2 = players[1]
            val player3 = players[2]
            val player4 = players[3]
            val stubDate = DateTime.now()
            val history = listOf(
                    pairAssignmentDocument(listOf(
                            PinnedCouplingPair(listOf(player1.withPins(emptyList()), player3.withPins(emptyList()))),
                            PinnedCouplingPair(listOf(player2.withPins(emptyList()), player4.withPins(emptyList())))
                    )),
                    pairAssignmentDocument(listOf(
                            PinnedCouplingPair(listOf(player1.withPins(emptyList()), player2.withPins(emptyList()))),
                            PinnedCouplingPair(listOf(player3.withPins(emptyList()), player4.withPins(emptyList())))
                    ))
            )

            private fun pairAssignmentDocument(pairs: List<PinnedCouplingPair>) =
                    PairAssignmentDocument(stubDate, pairs, tribe.id)
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
        } verify { (_, pairReports) ->
            pairReports.map { it.timeSinceLastPair }
                    .assertIsEqualTo(listOf(
                            NeverPaired,
                            NeverPaired,
                            TimeResultValue(1),
                            TimeResultValue(1),
                            TimeResultValue(0),
                            TimeResultValue(0)
                    ))
            pairReports.map { it.pair }
                    .assertMatch(listOf(
                            CouplingPair.Double(player1, player4),
                            CouplingPair.Double(player2, player3),
                            CouplingPair.Double(player1, player2),
                            CouplingPair.Double(player3, player4),
                            CouplingPair.Double(player1, player3),
                            CouplingPair.Double(player2, player4)
                    ))
        }

        @Test
        fun stillSortsCorrectlyWithLargeRealisticHistory() = setup(loadResource<Json>("realistic-sort-test-data/inputs.json").let {
            object {
                val tribe = it["tribe"].unsafeCast<Json>().toTribe()
                val players = it["players"].unsafeCast<Array<Json>>().map { player -> player.toPlayer() }
                val history = it["history"].unsafeCast<Array<Json>>().map { record -> record.toPairAssignmentDocument() }
            }
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
        } verify { result ->
            val expectedTimesResults = loadResource<Array<Int>>("realistic-sort-test-data/expectResults.json")
                    .map { TimeResultValue(it) }
            result.pairReports.map { it.timeSinceLastPair }
                    .assertIsEqualTo(expectedTimesResults)
        }
    }

    class WillCalculateTheMedianSpinTime {

        companion object {
            private fun pairAssignmentDocument(dateTime: DateTime) = PairAssignmentDocument(
                    dateTime,
                    emptyList(),
                    tribe.id
            )
        }

        @Test
        fun whenThereIsNoHistoryWillReturnNotApplicable() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val players = emptyList<Player>()
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
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
                    pairAssignmentDocument(DateTime(2017, 2, 12))
            )
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
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
                    pairAssignmentDocument(DateTime(2017, 2, 3))
            )
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
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
                    pairAssignmentDocument(DateTime(2017, 2, 10))
            )
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.days} days")
        }

        @Test
        fun withOneHistoryEntryWillReturnNull() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                    PairAssignmentDocument(DateTime(2017, 2, 17), emptyList(), tribe.id)
            )
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
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
                    pairAssignmentDocument(DateTime(2017, 2, 20, 9))
            )
        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
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
                    pairAssignmentDocument(DateTime(2017, 2, 10, 9))
            )

        }) exercise {
            ComposeStatisticsAction(tribe, players, history)
                    .perform()
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.5.days, "Got ${result.medianSpinDuration?.days} days")
        }

    }

}