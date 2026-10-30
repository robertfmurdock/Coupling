package com.zegreatrob.coupling.repository.dynamo

import kotlin.time.Instant

fun interface RevisionIdGenerator {
    fun next(timestamp: Instant): String
}

class MonotonicUlidRevisionIdGenerator : RevisionIdGenerator {
    private val nextUlid = monotonicFactory()

    override fun next(timestamp: Instant) = nextUlid(timestamp.toEpochMilliseconds().toDouble())
}
