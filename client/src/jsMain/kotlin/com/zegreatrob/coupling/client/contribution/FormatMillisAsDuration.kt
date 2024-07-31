package com.zegreatrob.coupling.client.contribution

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val formatMillisAsDuration: (Number) -> String = { value ->
    (Duration.ZERO + value.toLong().milliseconds).toString()
}
