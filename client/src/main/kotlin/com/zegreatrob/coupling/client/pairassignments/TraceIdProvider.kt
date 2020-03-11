package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.TraceIdSyntax

interface NullTraceIdProvider : TraceIdSyntax {
    override val traceId get() = null
}