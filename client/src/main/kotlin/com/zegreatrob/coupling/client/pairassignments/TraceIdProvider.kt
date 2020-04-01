package com.zegreatrob.coupling.client.pairassignments

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdSyntax

interface NullTraceIdProvider : TraceIdSyntax {
    override val traceId: Uuid? get() = null
}