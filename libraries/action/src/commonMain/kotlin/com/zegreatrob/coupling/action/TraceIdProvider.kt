package com.zegreatrob.coupling.action

import kotlin.uuid.Uuid

interface TraceIdProvider {
    val traceId: Uuid
}
