package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.medianSpinDuration
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
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

class MedianSpinDurationTest {

    companion object {
        private fun pairAssignmentDocument(dateTime: Instant) = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = dateTime,
            pairs = stubPinnedPairs(),
            null,
        )

        private fun stubPinnedPairs() = notEmptyListOf(
            PinnedCouplingPair(notEmptyListOf(stubPlayer().withPins(emptyList())), emptySet()),
        )

        private fun dateTime(year: Int, month: Int, day: Int, hour: Int = 0) = LocalDateTime(year, month, day, hour, 0, 0).toInstant(TimeZone.currentSystemDefault())
    }

    @Test
    fun whenThereIsNoHistoryWillReturnNotApplicable() = setup(object {
        val history = emptyList<PairAssignmentDocument>()
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun whenThereAreDailySpinsWillReturn1Day() = setup(object {
        val history = listOf(
            pairAssignmentDocument(dateTime(2017, 2, 17)),
            pairAssignmentDocument(dateTime(2017, 2, 16)),
            pairAssignmentDocument(dateTime(2017, 2, 15)),
            pairAssignmentDocument(dateTime(2017, 2, 14)),
            pairAssignmentDocument(dateTime(2017, 2, 13)),
            pairAssignmentDocument(dateTime(2017, 2, 12)),
        )
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(1.days, "Got ${result?.inWholeDays} days")
    }

    @Test
    fun whenTwoDaySpinsWithOutliersWillReturn2Days() = setup(object {
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
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(2.days, "Got ${result?.inWholeDays} days")
    }

    @Test
    fun whenOneInstanceOfMedianAndVariablePatternWillFindMedianCorrectly() = setup(object {
        val history = listOf(
            pairAssignmentDocument(dateTime(2017, 2, 20)),
            pairAssignmentDocument(dateTime(2017, 2, 17)),
            pairAssignmentDocument(dateTime(2017, 2, 15)),
            pairAssignmentDocument(dateTime(2017, 2, 14)),
            pairAssignmentDocument(dateTime(2017, 2, 13)),
            pairAssignmentDocument(dateTime(2017, 2, 10)),
        )
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(2.days, "Got ${result?.inWholeDays} days")
    }

    @Test
    fun withOneHistoryEntryWillReturnNull() = setup(object {
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId.new(),
                date = dateTime(2017, 2, 17),
                pairs = stubPinnedPairs(),
                null,
            ),
        )
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun worksWithHourDifferencesAsWell() = setup(object {
        val history = listOf(
            pairAssignmentDocument(dateTime(2017, 2, 20, 21)),
            pairAssignmentDocument(dateTime(2017, 2, 20, 19)),
            pairAssignmentDocument(dateTime(2017, 2, 20, 18)),
            pairAssignmentDocument(dateTime(2017, 2, 20, 13)),
            pairAssignmentDocument(dateTime(2017, 2, 20, 12)),
            pairAssignmentDocument(dateTime(2017, 2, 20, 9)),
        )
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(2.hours, "Got ${result?.inWholeHours} hours")
    }

    @Test
    fun whenMedianIsInBetweenUnitsWillStillBeAccurate() = setup(object {
        val history = listOf(
            pairAssignmentDocument(dateTime(2017, 2, 20, 21)),
            pairAssignmentDocument(dateTime(2017, 2, 17, 19)),
            pairAssignmentDocument(dateTime(2017, 2, 15, 7)),
            pairAssignmentDocument(dateTime(2017, 2, 14, 13)),
            pairAssignmentDocument(dateTime(2017, 2, 13, 12)),
            pairAssignmentDocument(dateTime(2017, 2, 10, 9)),
        )
    }) exercise {
        history.medianSpinDuration()
    } verify { result ->
        result.assertIsEqualTo(2.5.days, "Got ${result?.inWholeDays} days")
    }
}
