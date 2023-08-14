package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Suppress("unused")
class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsAction.Dispatcher {
        val party = PartyDetails(PartyId("LOL"), PairingRule.LongestTime)

        fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
            .map { number -> makePlayer("$number") }

        private fun makePlayer(id: String) = Player(id = id, avatarType = null)

        private fun List<CouplingPair>.assertMatch(expected: List<CouplingPair>) {
            assertIsEqualTo(
                expected,
                "------WE EXPECT\n${expected.describe()}\n------RESULTS\n${this.describe()}\n-----END\n",
            )
        }

        private fun List<CouplingPair>.describe() = map { it.map { player -> player.id } }
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

    class WillCalculateTheMedianSpinTime {

        companion object {
            private fun pairAssignmentDocument(dateTime: Instant) =
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId(""),
                    date = dateTime,
                    pairs = stubPinnedPairs(),
                    null,
                )

            private fun stubPinnedPairs() = notEmptyListOf(
                PinnedCouplingPair(notEmptyListOf(stubPlayer().withPins(emptyList())), emptySet()),
            )
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
                pairAssignmentDocument(dateTime(2017, 2, 17)),
                pairAssignmentDocument(dateTime(2017, 2, 16)),
                pairAssignmentDocument(dateTime(2017, 2, 15)),
                pairAssignmentDocument(dateTime(2017, 2, 14)),
                pairAssignmentDocument(dateTime(2017, 2, 13)),
                pairAssignmentDocument(dateTime(2017, 2, 12)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(1.days, "Got ${result.medianSpinDuration?.inWholeDays} days")
        }

        @Test
        fun whenTwoDaySpinsWithOutliersWillReturn2Days() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(dateTime(2017, 2, 17)),
                pairAssignmentDocument(dateTime(2017, 2, 12)),
                pairAssignmentDocument(dateTime(2017, 2, 10)),
                pairAssignmentDocument(dateTime(2017, 2, 8)),
                pairAssignmentDocument(dateTime(2017, 2, 6)),
                pairAssignmentDocument(dateTime(2017, 2, 4)),
                pairAssignmentDocument(dateTime(2017, 2, 3)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.inWholeDays} days")
        }

        @Test
        fun whenOneInstanceOfMedianAndVariablePatternWillFindMedianCorrectly() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(dateTime(2017, 2, 20)),
                pairAssignmentDocument(dateTime(2017, 2, 17)),
                pairAssignmentDocument(dateTime(2017, 2, 15)),
                pairAssignmentDocument(dateTime(2017, 2, 14)),
                pairAssignmentDocument(dateTime(2017, 2, 13)),
                pairAssignmentDocument(dateTime(2017, 2, 10)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.days, "Got ${result.medianSpinDuration?.inWholeDays} days")
        }

        @Test
        fun withOneHistoryEntryWillReturnNull() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId(""),
                    date = dateTime(2017, 2, 17),
                    pairs = stubPinnedPairs(),
                    null,
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
                pairAssignmentDocument(dateTime(2017, 2, 20, 21)),
                pairAssignmentDocument(dateTime(2017, 2, 20, 19)),
                pairAssignmentDocument(dateTime(2017, 2, 20, 18)),
                pairAssignmentDocument(dateTime(2017, 2, 20, 13)),
                pairAssignmentDocument(dateTime(2017, 2, 20, 12)),
                pairAssignmentDocument(dateTime(2017, 2, 20, 9)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.hours, "Got ${result.medianSpinDuration?.inWholeHours} hours")
        }

        @Test
        fun whenMedianIsInBetweenUnitsWillStillBeAccurate() = setup(object {
            val players = emptyList<Player>()
            val history = listOf(
                pairAssignmentDocument(dateTime(2017, 2, 20, 21)),
                pairAssignmentDocument(dateTime(2017, 2, 17, 19)),
                pairAssignmentDocument(dateTime(2017, 2, 15, 7)),
                pairAssignmentDocument(dateTime(2017, 2, 14, 13)),
                pairAssignmentDocument(dateTime(2017, 2, 13, 12)),
                pairAssignmentDocument(dateTime(2017, 2, 10, 9)),
            )
        }) exercise {
            perform(ComposeStatisticsAction(party, players, history))
        } verify { result ->
            result.medianSpinDuration.assertIsEqualTo(2.5.days, "Got ${result.medianSpinDuration?.inWholeDays} days")
        }
    }
}

expect fun loadJsonPartySetup(fileResource: String): PartySetup
expect inline fun <reified T> loadResource(fileResource: String): T

data class PartySetup(val party: PartyDetails, val players: List<Player>, val history: List<PairAssignmentDocument>)

private fun dateTime(year: Int, month: Int, day: Int, hour: Int = 0) =
    LocalDateTime(year, month, day, hour, 0, 0).toInstant(TimeZone.currentSystemDefault())
