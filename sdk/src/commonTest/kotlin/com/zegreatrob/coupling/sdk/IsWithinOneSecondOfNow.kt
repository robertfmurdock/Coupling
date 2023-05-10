package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.minassert.assertIsEqualTo

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
