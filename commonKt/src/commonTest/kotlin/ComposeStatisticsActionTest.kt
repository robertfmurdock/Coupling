@file:Suppress("unused")

import kotlin.test.Test

class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsActionDispatcher {
        val tribe = KtTribe("LOL", PairingRule.LongestTime)

        fun makePlayers(tribe: KtTribe, numberOfPlayers: Int) = (1..numberOfPlayers)
                .map { number -> makePlayer(tribe, "$number") }

        fun makePlayer(tribe: KtTribe, id: String) = Player(_id = id, tribe = tribe.id)

        private fun List<CouplingPair>.checkPairs(expected: List<CouplingPair>) {
            assertIsEqualTo(
                    expected,
                    "------WE EXPECT\n${expected.describe()}\n------RESULTS\n${this.describe()}\n-----END\n"
            )
        }

        private fun List<CouplingPair>.describe() = map { it.asArray().map { player -> player._id } }
                .joinToString(", ").let { "[ $it ]" }
    }

    class WillIncludeTheFullRotationNumber {

        companion object {
            val history = emptyList<HistoryDocument>()

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
                val history = emptyList<HistoryDocument>()
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
                val expected = listOf(
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
                pairReports.map { it.pair }
                        .checkPairs(expected)
            }
        }


    }
}