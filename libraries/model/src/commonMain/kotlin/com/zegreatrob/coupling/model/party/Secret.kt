package com.zegreatrob.coupling.model.party

import kotlinx.datetime.Instant
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class Secret(
    val id: SecretId,
    val description: String,
    val createdTimestamp: Instant,
    val lastUsedTimestamp: Instant?,
)

data class SecretId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = SecretId(NotBlankString.create(Uuid.random().toString()))
    }
}

fun SecretId(value: String): SecretId? = value.toNotBlankString().getOrNull()?.let(::SecretId)

data class SecretUsed(val partyId: PartyId, val secretId: SecretId, val lastUsedTimestamp: Instant)
