package com.zegreatrob.coupling.action.heatmap

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsActionDispatcher
import com.zegreatrob.coupling.action.stats.heatmap.CalculatePairHeatAction
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import korlibs.time.DateTime
import kotlin.test.Test

class CalculatePairHeatActionTest {

    companion object :
        CalculatePairHeatAction.Dispatcher,
        AssignPinsActionDispatcher {
        private fun List<CouplingPair>.buildHistoryByRepeating(repetitions: Int) = (0 until repetitions)
            .map { pairAssignmentDocument() }

        fun List<CouplingPair>.pairAssignmentDocument() =
            PairAssignmentDocument(
                id = PairAssignmentDocumentId(""),
                date = DateTime(2016, 3, 1),
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
    fun willReturnOneWhenPairHasOccurredButDifferentPositions() = setup(object {
        private val player1 = Player(id = "bob", avatarType = null)
        private val player2 = Player(id = "fred", avatarType = null)
        val pair = pairOf(player1, player2)
        val history = listOf(
            listOf(pairOf(player2, player1)).pairAssignmentDocument(),
        )
        val rotationPeriod = 60
        val action = CalculatePairHeatAction(pair, history, rotationPeriod)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(1.0)
    }

    class WhenThereIsOnlyOnePossiblePair {

        private fun makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs: Int): CalculatePairHeatAction {
            val pair = pairOf(Player(id = "bob", avatarType = null), Player(id = "fred", avatarType = null))
            val rotationPeriod = 1
            val history = listOf(pair).buildHistoryByRepeating(numberOfHistoryDocs)
            return CalculatePairHeatAction(pair, history, rotationPeriod)
        }

        @Test
        fun willReturn1WhenThePairHasOnePairing() = setup(object {
            val action = makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 1)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturnTwoAndHalfWhenThePairHasTwoConsecutivePairings() = setup(object {
            val action = makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 2)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(2.5)
        }

        @Test
        fun willReturnFourAndHalfWhenThePairHasThreeConsecutivePairings() = setup(object {
            val action = makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 3)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(4.5)
        }

        @Test
        fun willReturnSevenWhenThePairHasFourConsecutivePairings() = setup(object {
            val action = makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 4)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(7.toDouble())
        }

        @Test
        fun willReturnTenWhenThePairHasFiveConsecutivePairings() = setup(object {
            val action = makeActionWithMultipleSpinsOfSamePair(numberOfHistoryDocs = 5)
        }) exercise {
            perform(action)
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
        fun willReturn1WithOnePairingInFullRotation() = setup(object {
            val expectedPairing = listOf(pair, pairOf(player3))
            val alternatePairing1 = listOf(pairOf(player1, player3), pairOf(player2))
            val alternatePairing2 = listOf(pairOf(player2, player3), pairOf(player1))

            val history = listOf(
                alternatePairing1,
                expectedPairing,
                alternatePairing2,
            ).map { it.pairAssignmentDocument() }
            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturn0WithLastPairingIsOlderThanFiveRotations() = setup(object {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            val expectedPairing = listOf(pair, pairOf(player3))
            val history = listOf(pairOf(player2, player3), pairOf(player1))
                .buildHistoryByRepeating(intervalsUntilCooling)
                .plus(expectedPairing.pairAssignmentDocument())

            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(0.0)
        }

        @Test
        fun willNotGoHigherThanTenWhenPairingMoreThanOncePerRotation() = setup(object {
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            val expectedPairing = listOf(pair, pairOf(player3))
                .buildHistoryByRepeating(rotationHeatWindow + 1)
            val history = listOf(pairOf(player2, player3), pairOf(player1))
                .buildHistoryByRepeating(intervalsUntilCooling - expectedPairing.size)
                .plus(expectedPairing)

            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
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
        fun willReturn1WhenLastPairingIsAlmostOlderThanFiveRotations() = setup(object {
            val expectedPairing = listOf(pair, pairOf(player3)).pairAssignmentDocument()
            val rotationHeatWindow = 5
            val intervalsUntilCooling = rotationPeriod * rotationHeatWindow
            val history = listOf(
                pairOf(player2, player3),
                pairOf(player1, player4),
                pairOf(player5),
            )
                .buildHistoryByRepeating(intervalsUntilCooling - 1)
                .plus(expectedPairing)

            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(1.0)
        }

        @Test
        fun willReturn7WhenSkippingOneRotationOutOfFive() = setup(object {
            val intervalWithIntendedPair = listOf(pair, pairOf(player3, player4)).pairAssignmentDocument()
            val assignmentsWithoutIntendedPair = listOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            val otherIntervals = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod)

            val history = goodRotation + absenteeRotation + goodRotation + goodRotation + goodRotation

            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(7.0)
        }

        @Test
        fun willReturnTwoAndHalfWhenSkippingThreeRotationOutOfFive() = setup(object {
            val intervalWithIntendedPair = listOf(pair, pairOf(player3, player4)).pairAssignmentDocument()
            val assignmentsWithoutIntendedPair = listOf(
                pairOf(player1, player3),
                pairOf(player3, player4),
            )
            val otherIntervals = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod - 1)

            val goodRotation = otherIntervals + intervalWithIntendedPair
            val absenteeRotation = assignmentsWithoutIntendedPair.buildHistoryByRepeating(rotationPeriod)

            val history = goodRotation + absenteeRotation + absenteeRotation + goodRotation + absenteeRotation

            val action = CalculatePairHeatAction(pair, history, rotationPeriod)
        }) exercise {
            perform(action)
        } verify { result ->
            result.assertIsEqualTo(2.5)
        }
    }
}
