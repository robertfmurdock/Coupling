package com.zegreatrob.coupling.action.heatmap

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.stats.heatmap.CalculatePairHeatAction
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class CalculatePairHeatActionTest {

    companion object :
        CalculatePairHeatAction.Dispatcher,
        AssignPinsAction.Dispatcher {
        private suspend fun NotEmptyList<CouplingPair>.buildHistoryByRepeating(repetitions: Int) = (0 until repetitions)
            .map { pairAssignmentDocument() }

        suspend fun NotEmptyList<CouplingPair>.pairAssignmentDocument() =
            PairAssignmentDocument(
                id = PairAssignmentDocumentId(""),
                date = LocalDateTime(2016, 3, 1, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                pairs = perform(AssignPinsAction(this, emptyList(), emptyList())),
            )
    }

    @Test
    fun willReturnZeroWhenPairHasNeverOccurred() = setup(object {
        val pair = pairOf(Player(id = "bob", avatarType = null), Player(id = "fred", avatarType = null))
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 60
        val action = CalculatePairHeatAction(pair, history, rotationPeriod)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(0.toDouble())
    }

    @Test
    fun willReturnOneWhenPairHasOccurredButDifferentPositions() = asyncSetup(object {
        val player1 = Player(id = "bob", avatarType = null)
        val player2 = Player(id = "fred", avatarType = null)
        val pair = pairOf(player1, player2)
        lateinit var history: List<PairAssignmentDocument>
        val rotationPeriod = 60
    }) {
        history = listOf(
            notEmptyListOf(pairOf(player2, player1)).pairAssignmentDocument(),
        )
    } exercise {
        perform(CalculatePairHeatAction(pair, history, rotationPeriod))
    } verify { result ->
        result.assertIsEqualTo(1.0)
    }

    class WhenThereIsOnlyOnePossiblePair {

        private suspend fun makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs: Int): CalculatePairHeatAction {
            val pair = pairOf(Player(id = "bob", avatarType = null), Player(id = "fred", avatarType = null))
            val rotationPeriod = 1
            val history = notEmptyListOf(pair).buildHistoryByRepeating(numberOfHistoryDocs)
            return CalculatePairHeatAction(pair, history, rotationPeriod)
        }

        @Test
        fun willReturn1WhenThePairHasOnePairing() = asyncSetup(object {
        }) exercise {
            perform(makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 1))
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturnTwoAndHalfWhenThePairHasTwoConsecutivePairings() = asyncSetup(object {
        }) exercise {
            perform(makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 2))
        } verify { result ->
            result.assertIsEqualTo(2.5)
        }

        @Test
        fun willReturnFourAndHalfWhenThePairHasThreeConsecutivePairings() = asyncSetup(object {
        }) exercise {
            perform(makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 3))
        } verify { result ->
            result.assertIsEqualTo(4.5)
        }

        @Test
        fun willReturnSevenWhenThePairHasFourConsecutivePairings() = asyncSetup(object {
        }) exercise {
            perform(makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 4))
        } verify { result ->
            result.assertIsEqualTo(7.toDouble())
        }

        @Test
        fun willReturnTenWhenThePairHasFiveConsecutivePairings() = asyncSetup(object {
        }) exercise {
            perform(makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 5))
        } verify { result ->
            result.assertIsEqualTo(10.toDouble())
        }
    }

    class WithThreePlayers {

        companion object {
            const val rotationPeriod = 3
            val player1 = Player(id = "bob", avatarType = null)
            val player2 = Player(id = "fred", avatarType = null)
            val player3 = Player(id = "latisha", avatarType = null)
            val pair = pairOf(player1, player2)
        }

        @Test
        fun willReturn1WithOnePairingInFullRotation() = asyncSetup(object {
            val expectedPairing = notEmptyListOf(pair, pairOf(player3))
            val alternatePairing1 = notEmptyListOf(pairOf(player1, player3), pairOf(player2))
            val alternatePairing2 = notEmptyListOf(pairOf(player2, player3), pairOf(player1))

            lateinit var history: List<PairAssignmentDocument>
        }) {
            history = listOf(
                alternatePairing1,
                expectedPairing,
                alternatePairing2,
            ).map { it.pairAssignmentDocument() }
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturn0WithLastPairingIsOlderThanFiveRotations() = asyncSetup(object {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            val expectedPairing = notEmptyListOf(pair, pairOf(player3))
            lateinit var history: List<PairAssignmentDocument>
        }) {
            history = notEmptyListOf(
                pairOf(
                    player2,
                    player3,
                ),
                pairOf(player1),
            )
                .buildHistoryByRepeating(intervalsUntilCooling)
                .plus(expectedPairing.pairAssignmentDocument())
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(0.0)
        }

        @Test
        fun willNotGoHigherThanTenWhenPairingMoreThanOncePerRotation() = asyncSetup(object {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            lateinit var expectedPairing: List<PairAssignmentDocument>
            lateinit var history: List<PairAssignmentDocument>
        }) {
            expectedPairing = notEmptyListOf(pair, pairOf(player3))
                .buildHistoryByRepeating(rotationHeatWindow + 1)
            history = notEmptyListOf(pairOf(player2, player3), pairOf(player1))
                .buildHistoryByRepeating(intervalsUntilCooling - expectedPairing.size)
                .plus(expectedPairing)
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(10.0)
        }
    }

    class WithFivePlayers {
        companion object {
            const val rotationPeriod = 5
            val player1 = Player(id = "bob", avatarType = null)
            val player2 = Player(id = "fred", avatarType = null)
            val pair = pairOf(player1, player2)
            val player3 = Player(id = "latisha", avatarType = null)
            val player4 = Player(id = "jane", avatarType = null)
            val player5 = Player(id = "fievel", avatarType = null)
        }

        @Test
        fun willReturn1WhenLastPairingIsAlmostOlderThanFiveRotations() = asyncSetup(object {
            lateinit var expectedPairing: PairAssignmentDocument
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            lateinit var history: List<PairAssignmentDocument>
        }) {
            expectedPairing = notEmptyListOf(pair, pairOf(player3)).pairAssignmentDocument()
            history = notEmptyListOf(
                pairOf(player2, player3),
                pairOf(player1, player4),
                pairOf(player5),
            )
                .buildHistoryByRepeating(intervalsUntilCooling - 1)
                .plus(expectedPairing)
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturn7WhenSkippingOneRotationOutOfFive() = asyncSetup(object {
            val assignmentsWithoutIntendedPair = notEmptyListOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            lateinit var history: List<PairAssignmentDocument>
        }) {
            val intervalWithIntendedPair = notEmptyListOf(pair, pairOf(player3, player4)).pairAssignmentDocument()
            val otherIntervals: List<PairAssignmentDocument> =
                assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation: List<PairAssignmentDocument> =
                assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod)
            history = goodRotation + absenteeRotation + goodRotation + goodRotation + goodRotation
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(7.0)
        }

        @Test
        fun willReturnTwoAndHalfWhenSkippingThreeRotationOutOfFive() = asyncSetup(object {
            val assignmentsWithoutIntendedPair = notEmptyListOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            lateinit var history: List<PairAssignmentDocument>
        }) {
            val intervalWithIntendedPair = notEmptyListOf(pair, pairOf(player3, player4)).pairAssignmentDocument()
            val otherIntervals = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod)
            history = goodRotation + absenteeRotation + absenteeRotation + goodRotation + absenteeRotation
        } exercise {
            perform(CalculatePairHeatAction(pair, history, rotationPeriod))
        } verify { result ->
            result.assertIsEqualTo(2.5)
        }
    }
}
