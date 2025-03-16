package com.zegreatrob.coupling.model.party

import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class SecretId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = SecretId(NotBlankString.Companion.create(Uuid.Companion.random().toString()))
    }
}

fun SecretId(value: String): SecretId? = value.toNotBlankString().getOrNull()?.let(::SecretId)
