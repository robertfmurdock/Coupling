package com.zegreatrob.coupling.sdk

import com.zegreatrob.minassert.assertIsEqualTo
import korlibs.time.DateTime

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
