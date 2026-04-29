package com.zegreatrob.coupling.sdk

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun Instant.isWithinOneSecondOfNow() {
    val now = Clock.System.now()
    isWithinWindow(
        earliest = now - 1.25.seconds,
        latest = now + 1.25.seconds,
    )
}

fun Instant.isWithinWindow(earliest: Instant, latest: Instant) {
    isWithinWindow(
        earliest = earliest,
        latest = latest,
        tolerance = 250.milliseconds,
    )
}

fun Instant.isWithinWindow(earliest: Instant, latest: Instant, tolerance: Duration) {
    val allowedEarliest = earliest - tolerance
    val allowedLatest = latest + tolerance
    (this >= allowedEarliest && this <= allowedLatest)
        .assertIsEqualTo(
            true,
            "timestamp $this was not within [$allowedEarliest, $allowedLatest] (base window [$earliest, $latest], tolerance $tolerance)",
        )
}
