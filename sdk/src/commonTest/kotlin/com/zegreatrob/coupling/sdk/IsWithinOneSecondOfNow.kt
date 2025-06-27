package com.zegreatrob.coupling.sdk

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.Instant

fun Instant.isWithinOneSecondOfNow() {
    val timeSpan = Clock.System.now() - this
    (timeSpan.toDouble(DurationUnit.SECONDS) < 1)
        .assertIsEqualTo(true, "timespan was not within 1 second - instead was $timeSpan")
}
