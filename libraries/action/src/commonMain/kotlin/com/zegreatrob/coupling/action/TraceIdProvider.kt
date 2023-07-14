package com.zegreatrob.coupling.action

import com.benasher44.uuid.Uuid

interface TraceIdProvider {
    val traceId: Uuid
}
