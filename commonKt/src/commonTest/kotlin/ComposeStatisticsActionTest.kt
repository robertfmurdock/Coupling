@file:Suppress("unused")

import kotlin.test.Test

class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsActionDispatcher {

        fun makePlayers(tribe: KtTribe, numberOfPlayers: Int) = (1..numberOfPlayers)
                .map { number -> makePlayer(tribe, "$number") }

        fun makePlayer(tribe: KtTribe, id: String) = Player(_id = id, tribe = tribe.id)
    }

    class WillIncludeTheFullRotationNumber {

        companion object {
            val tribe = KtTribe("LOL", PairingRule.LongestTime)
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


}