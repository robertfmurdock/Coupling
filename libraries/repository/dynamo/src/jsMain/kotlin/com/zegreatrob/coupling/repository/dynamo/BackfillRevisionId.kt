package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.createHash
import kotlin.time.Instant

internal fun sha256(value: String): String = createHash("sha256").update(value).digest("hex")

internal fun backfillRevisionId(timestamp: Instant, sourceIdentity: String): String {
    val millis = timestamp.toEpochMilliseconds()
    require(millis in 0..281474976710655L) { "Source timestamp is outside the ULID range" }
    val time = (9 downTo 0).map { shift -> ULID_ALPHABET[((millis shr (shift * 5)) and 31).toInt()] }.joinToString("")
    val bits = sha256(sourceIdentity).take(20).map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")
    return time + bits.chunked(5).map { ULID_ALPHABET[it.toInt(2)] }.joinToString("")
}

private const val ULID_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
