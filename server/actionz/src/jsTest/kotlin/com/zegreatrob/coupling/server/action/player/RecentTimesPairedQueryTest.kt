package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.stubmodel.record
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentId
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class RecentTimesPairedQueryTest {

    companion object {
        private fun NotEmptyList<CouplingPair>.buildHistoryByRepeating(repetitions: Int) = (0 until repetitions)
            .map { pairAssignmentDocument(index = it) }

        fun NotEmptyList<CouplingPair>.pairAssignmentDocument(index: Int = 0) = PairAssignmentDocument(
            id = stubPairAssignmentId(),
            date = LocalDateTime(2016, 3, 1, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
                .plus(index.minutes),
            pairs = withPins(),
            null,
        )
    }

    @Test
    fun alwaysReturnsNullForSolos() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
        val partyId = stubPartyId()
        val player1 = stubPlayer().copy(name = "bob")
        val player2 = stubPlayer().copy(name = "fred")
        override val playerRepository = PlayerListGet { listOf(player1, player2).map { record(partyId, it) } }
        override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { emptyList() }
    }) exercise {
        perform(RecentTimesPairedQuery(partyId, pairOf(player2), null))
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun willReturnZeroWhenPairHasNeverOccurred() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
        val partyId = stubPartyId()
        val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
        val action = RecentTimesPairedQuery(partyId, pair, null)
        override val playerRepository = PlayerListGet { stubPlayers(61).map { record(partyId, it) } }
        override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { emptyList() }
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(0.toDouble())
    }

    @Test
    fun willReturnOneWhenPairHasOccurredButDifferentPositions() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
        val player1 = stubPlayer().copy(name = "bob")
        val player2 = stubPlayer().copy(name = "fred")
        val pair = pairOf(player1, player2)
        var history = listOf(
            notEmptyListOf(pairOf(player2, player1)).pairAssignmentDocument(),
        )
        override val playerRepository = PlayerListGet { stubPlayers(61).map { record(partyId, it) } }
        override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
            history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
        }
        val partyId = stubPartyId()
    }) exercise {
        perform(RecentTimesPairedQuery(partyId, pair, null))
    } verify { result ->
        result.assertIsEqualTo(1.0)
    }

    class WhenThereIsOnlyOnePossiblePair {

        @Test
        fun whenThePairHasOnePairingCountsCorrectly() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val rotationPeriod = 1
            val history = notEmptyListOf(pair).buildHistoryByRepeating(rotationPeriod)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet { pair.asArray().map { record(partyId, it) } }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(1)
        }

        @Test
        fun whenThePairHasTwoConsecutivePairingsShowsCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val rotationPeriod = 2
            val history = notEmptyListOf(pair).buildHistoryByRepeating(rotationPeriod)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet {
                pair.asArray().plus(stubPlayer()).map { record(partyId, it) }
            }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(2)
        }

        @Test
        fun whenThePairHasThreeConsecutivePairingsShowsCount() = asyncSetup(object :
            RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val rotationPeriod = 3
            val history = notEmptyListOf(pair).buildHistoryByRepeating(rotationPeriod)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet { stubPlayers(4).map { record(partyId, it) } }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(3)
        }

        @Test
        fun whenThePairHasFourConsecutivePairingsShowsCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val history = notEmptyListOf(pair).buildHistoryByRepeating(4)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet { stubPlayers(5).map { record(partyId, it) } }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(4)
        }

        @Test
        fun whenLimitedWithFourConsecutivePairingsWillReturnCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val history = notEmptyListOf(pair).buildHistoryByRepeating(4)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet { stubPlayers(5).map { record(partyId, it) } }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, history.maxByOrNull { it.date }!!.id))
        } verify { result ->
            result.assertIsEqualTo(4)
        }

        @Test
        fun whenThePairHasFiveConsecutivePairingsWillReturnCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val pair = pairOf(stubPlayer().copy(name = "bob"), stubPlayer().copy(name = "fred"))
            val rotationPeriod = 5
            val history = notEmptyListOf(pair).buildHistoryByRepeating(rotationPeriod)
            val partyId = stubPartyId()
            override val playerRepository = PlayerListGet { stubPlayers(5).map { record(partyId, it) } }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(5)
        }
    }

    class WithThreePlayers {

        companion object {
            const val ROTATION_PERIOD = 3
            val player1 = stubPlayer().copy(name = "bob")
            val player2 = stubPlayer().copy(name = "fred")
            val player3 = stubPlayer().copy(name = "latisha")
            private val partyId = stubPartyId()
            val pair = pairOf(player1, player2)
            val playerRecords = listOf(player1, player2, player3).map { record(partyId, it) }
        }

        @Test
        fun willReturn1WithOnePairingInFullRotation() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val expectedPairing = notEmptyListOf(pair, pairOf(player3))
            val alternatePairing1 = notEmptyListOf(pairOf(player1, player3), pairOf(player2))
            val alternatePairing2 = notEmptyListOf(pairOf(player2, player3), pairOf(player1))

            val history = listOf(
                alternatePairing1,
                expectedPairing,
                alternatePairing2,
            ).mapIndexed { index, pairing -> pairing.pairAssignmentDocument(index) }
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturn0WithLastPairingIsOlderThanFiveRotations() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = ROTATION_PERIOD * rotationHeatWindow
            val expectedPairing = notEmptyListOf(pair, pairOf(player3))
            val history = notEmptyListOf(
                pairOf(
                    player2,
                    player3,
                ),
                pairOf(player1),
            )
                .buildHistoryByRepeating(intervalsUntilCooling)
                .plus(expectedPairing.pairAssignmentDocument(-1))
                .shuffled()
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(0.0)
        }

        @Test
        fun whenManyPairingsInWindowShowsCountCorrectly() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = ROTATION_PERIOD * rotationHeatWindow
            val expectedPairing = notEmptyListOf(pair, pairOf(player3))
                .buildHistoryByRepeating(rotationHeatWindow + 1)
            val history = notEmptyListOf(pairOf(player2, player3), pairOf(player1))
                .buildHistoryByRepeating(intervalsUntilCooling - expectedPairing.size)
                .plus(expectedPairing)
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(6)
        }
    }

    class WithFivePlayers {
        companion object {
            const val ROTATION_PERIOD = 5
            private val partyId = stubPartyId()
            val player1 = stubPlayer().copy(name = "bob")
            val player2 = stubPlayer().copy(name = "fred")
            val pair = pairOf(player1, player2)
            val player3 = stubPlayer().copy(name = "latisha")
            val player4 = stubPlayer().copy(name = "jane")
            val player5 = stubPlayer().copy(name = "fievel")
            val playerRecords = listOf(player1, player2, player3, player4, player5).map { record(partyId, it) }
        }

        @Test
        fun willReturn1WhenLastPairingIsAlmostOlderThanFiveRotations() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = ROTATION_PERIOD * rotationHeatWindow
            val expectedPairing = notEmptyListOf(pair, pairOf(player3)).pairAssignmentDocument(3)
            val history = notEmptyListOf(
                pairOf(player2, player3),
                pairOf(player1, player4),
                pairOf(player5),
            )
                .buildHistoryByRepeating(intervalsUntilCooling - 1)
                .plus(expectedPairing)
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(1)
        }

        @Test
        fun whenSkippingOneRotationOutOfFiveReturnsCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val assignmentsWithoutIntendedPair = notEmptyListOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            val intervalWithIntendedPair = notEmptyListOf(pair, pairOf(player3, player4)).pairAssignmentDocument(4)
            val otherIntervals: List<PairAssignmentDocument> =
                assignmentsWithoutIntendedPair.buildHistoryByRepeating(ROTATION_PERIOD - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation: List<PairAssignmentDocument> =
                assignmentsWithoutIntendedPair.buildHistoryByRepeating(ROTATION_PERIOD)
            val history = goodRotation + absenteeRotation + goodRotation + goodRotation + goodRotation
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(4)
        }

        @Test
        fun whenSkippingThreeRotationOutOfFiveWillReturnCount() = asyncSetup(object : RecentTimesPairedQuery.Dispatcher {
            val assignmentsWithoutIntendedPair = notEmptyListOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            val intervalWithIntendedPair = notEmptyListOf(pair, pairOf(player3, player4)).pairAssignmentDocument(4)
            val otherIntervals = assignmentsWithoutIntendedPair.buildHistoryByRepeating(ROTATION_PERIOD - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation = assignmentsWithoutIntendedPair.buildHistoryByRepeating(ROTATION_PERIOD)
            val history = goodRotation + absenteeRotation + absenteeRotation + goodRotation + absenteeRotation
            override val playerRepository = PlayerListGet { playerRecords }
            override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { partyId ->
                history.map { Record(PartyElement(partyId, it), "test", false, Instant.DISTANT_PAST) }
            }
        }) exercise {
            perform(RecentTimesPairedQuery(stubPartyId(), pair, null))
        } verify { result ->
            result.assertIsEqualTo(2)
        }
    }
}
